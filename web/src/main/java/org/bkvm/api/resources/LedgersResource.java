/*
 * Licensed to Diennea S.r.l. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Diennea S.r.l. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.bkvm.api.resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.bkvm.auth.UserRole;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.bookkeeper.BookkeeperManagerException;
import org.bkvm.cache.Cluster;
import org.bkvm.cache.Ledger;
import org.bkvm.config.ServerConfiguration;

@Path("ledger")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class LedgersResource extends AbstractBookkeeperResource {

    @Data
    @AllArgsConstructor
    public static final class GetLedgersResult {

        private List<LedgerBean> ledgers;
        private long totalLedgers;
        private long totalSize;
    }

    @GET
    @Secured
    @PermitAll
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public GetLedgersResult getLedgers(@QueryParam("term") String term,
                                       @QueryParam("bookie") String bookieId,
                                       @QueryParam("cluster") String clusterId,
                                       @QueryParam("ledgerIds") String ledgerIds,
                                       @QueryParam("minLength") String minLength,
                                       @QueryParam("maxLength") String maxLength,
                                       @QueryParam("minAge") String minAge,
                                       @QueryParam("page") int page,
                                       @QueryParam("size") int size
    ) throws Exception {
        List<Long> searchLedgerIds = null;
        if (ledgerIds != null && !ledgerIds.trim().isEmpty()) {
            try {
                searchLedgerIds = convertParamLongList(ledgerIds);
            } catch (NumberFormatException ex) {
                searchLedgerIds = new ArrayList<>();
            }
        }

        List<Map.Entry<Integer, Long>> resultLedgerIds = getBookkeeperManager()
                .searchLedgers(term,
                        bookieId, convertParamInt(clusterId),
                        searchLedgerIds, convertParamInt(minLength),
                        convertParamInt(maxLength), convertParamInt(minAge)
                );

        List<LedgerBean> allLedgers = new ArrayList<>();
        long totalSize = 0;
        for (Map.Entry<Integer, Long> lId : resultLedgerIds) {
            long id = lId.getValue();
            int ledgerClusterId = lId.getKey();
            LedgerBean bean = getLedgerMetadata(ledgerClusterId, id);
            if (bean != null) {
                allLedgers.add(bean);
                totalSize += bean.getLength();
            }
        }

        List<LedgerBean> ledgers = filterLedgers(allLedgers, page, size);
        return new GetLedgersResult(ledgers, allLedgers.size(), totalSize);
    }

    private List<LedgerBean> filterLedgers(List<LedgerBean> ledgers, int offset, int limit) {
        int skipIndex = (offset - 1) * limit;
        return ledgers.stream()
                .skip(skipIndex)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GET
    @Secured
    @PermitAll
    @Path("metadata/{clusterId}/{ledgerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LedgerBean getLedgerMetadata(@PathParam("clusterId") int clusterId, @PathParam("ledgerId") long ledgerId) throws Exception {
        Ledger ledger = getBookkeeperManager().getLedger(clusterId, ledgerId);
        LedgerMetadata ledgerMetadata = getBookkeeperManager().getLedgerMetadata(ledger);
        Cluster cluster = getBookkeeperManager().getCluster(clusterId);
        String descriptionPattern = getBookkeeperManager().getConfigStore().getProperty(ServerConfiguration.PROPERTY_BK_METADATA_DESCRIPTION, ServerConfiguration.PROPERTY_BK_METADATA_DESCRIPTION_DEFAULT);
        return convertLedgerBean(ledger.getClusterId(), cluster.getName(), ledgerId, ledgerMetadata, ledger, descriptionPattern);
    }

    private LedgerBean convertLedgerBean(int clusterId, String clusterName, long ledgerId, LedgerMetadata ledgerMetadata, Ledger ledger, String descriptionPattern) throws BookkeeperManagerException {
        LedgerBean b = new LedgerBean();
        b.setClusterId(clusterId);
        b.setClusterName(clusterName);
        b.setId(ledgerId);
        b.applyLedgerMetadata(ledgerMetadata, descriptionPattern);
        b.setAge(ledger.getAge());
        b.setEnsembleSize(ledgerMetadata.getEnsembleSize());
        b.setWriteQuorumSize(ledgerMetadata.getWriteQuorumSize());
        b.setAckQuorumSize(ledgerMetadata.getAckQuorumSize());
        b.setLastEntryId(ledgerMetadata.getLastEntryId());
        b.setLength(ledgerMetadata.getLength());
        b.setPassword(ledgerMetadata.hasPassword() ? new String(ledgerMetadata.getPassword(), StandardCharsets.UTF_8) : "");
        b.setDigestType(ledgerMetadata.getDigestType() + "");
        b.setCtime(ledgerMetadata.getCtime());
        b.setClosed(ledgerMetadata.isClosed());
        b.setState(ledgerMetadata.getState() + "");
        b.setMetadataFormatVersion(ledgerMetadata.getMetadataFormatVersion());
        b.setEnsembles(new HashMap<>(BookkeeperManager.buildEnsembleMap(ledgerMetadata)));
        return b;
    }

    private Integer convertParamInt(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException err) {
            return null;
        }
    }

    /**
     * Split a string into a Long list. If any value is invalid throws NumberFormatException
     *
     * @param s
     * @return List
     * @throws NumberFormatException
     */
    private static List<Long> convertParamLongList(String s) throws NumberFormatException {
        List<Long> res = new ArrayList<>();
        if (s != null && !s.trim().isEmpty()) {
            for (String _s : s.split(",")) {
                _s = _s.trim();
                if (!_s.isEmpty()) {
                    Long l = Long.parseLong(_s);
                    res.add(l);
                } else {
                    throw new NumberFormatException("Empty string.");
                }
            }
        }
        return res;
    }

    @Data
    public static final class LedgerBean {

        private int clusterId;
        private String clusterName;
        private long id;
        private String description;
        private Map<String, String> metadata = new HashMap<>();
        private long age;
        private int ensembleSize;
        private int writeQuorumSize;
        private int ackQuorumSize;
        private long lastEntryId;
        private long length;
        private String password;
        private String digestType;
        private long ctime;
        private boolean closed;
        private String state;
        private int metadataFormatVersion;
        private Map<Long, List<String>> ensembles;

        private void setMetadataValue(String key, byte[] value) {
            String svalue;
            try {
                svalue = new String(value, StandardCharsets.UTF_8);
            } catch (Throwable t) {
                svalue = Arrays.toString(value);
            }
            this.metadata.put(key, svalue);
        }

        public void applyLedgerMetadata(LedgerMetadata metadata, String descriptionPattern) {
            Map<String, byte[]> customMetadata = metadata.getCustomMetadata();
            for (Map.Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
                setMetadataValue(currentCustomMetadata.getKey(), currentCustomMetadata.getValue());

            }
            String[] specialMetadataNames = descriptionPattern != null ? descriptionPattern.toLowerCase().split("\\,") : new String[]{};
            boolean descriptionFound = false;

            // descriptionPattern=tablespacename,pulsar/managed-ledger,application
            // pickup the first metadata name that matches
            for (String metadataField : specialMetadataNames) {
                for (Map.Entry<String, String> currentCustomMetadata : this.metadata.entrySet()) {
                    if (!descriptionFound) {
                        if (currentCustomMetadata.getKey().contains(metadataField)) {
                            this.description = currentCustomMetadata.getValue();
                            descriptionFound = true;
                            break;
                        }
                    }
                }
            }
            if (!descriptionFound) {
                description = "";
            }
        }
    }

}
