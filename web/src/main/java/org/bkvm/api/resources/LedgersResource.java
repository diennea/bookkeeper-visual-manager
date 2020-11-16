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
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.bookkeeper.BookkeeperManagerException;
import org.bkvm.cache.Ledger;
import org.bkvm.config.ServerConfiguration;

@Path("ledger")
public class LedgersResource extends AbstractBookkeeperResource {

    public static final class GetLedgersResult {
        private List<LedgerBean> ledgers;
        private long totalSize;

        public List<LedgerBean> getLedgers() {
            return ledgers;
        }

        public void setLedgers(List<LedgerBean> ledgers) {
            this.ledgers = ledgers;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        @Override
        public String toString() {
            return "GetLedgersResult{" + "ledgers=" + ledgers + ", totalSize=" + totalSize + '}';
        }

    }

    @GET
    @Secured
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public GetLedgersResult getLedgers(@QueryParam("term") String term,
                                       @QueryParam("bookie") String bookie,
                                       @QueryParam("ledgerIds") String ledgerIds,
                                       @QueryParam("minLength") String minLength,
                                       @QueryParam("maxLength") String maxLength,
                                       @QueryParam("minAge") String minAge
    ) throws Exception {
        List<Long> _ledgersIds = null;
        if (ledgerIds != null && !ledgerIds.trim().isEmpty()) {
            try {
                _ledgersIds = convertParamLongList(ledgerIds);
            } catch (NumberFormatException ex) {
                _ledgersIds = new ArrayList<>();
            }
        }
        List<Long> ids = getBookkeeperManager().searchLedgers(term, bookie, _ledgersIds, convertParamInt(minLength),
                convertParamInt(maxLength), convertParamInt(minAge));
        List<LedgerBean> res = new ArrayList<>();
        long totalSize = 0;
        for (long id : ids) {
            LedgerBean bean = getLedgerMetadata(id);
            if (bean != null) {
                res.add(bean);
                totalSize += bean.getLength();
            }
        }
        GetLedgersResult r = new GetLedgersResult();
        r.setLedgers(res);
        r.setTotalSize(totalSize);
        return r;
    }

    @GET
    @Secured
    @Path("metadata/{ledgerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LedgerBean getLedgerMetadata(@PathParam("ledgerId") long ledgerId) throws Exception {
        Ledger ledger = getBookkeeperManager().getLedger(ledgerId);
        LedgerMetadata ledgerMetadata = getBookkeeperManager().getLedgerMetadata(ledger);
        String descriptionPattern = getBookkeeperManager().getConfigStore().getProperty(ServerConfiguration.PROPERTY_BK_METADATA_DESCRIPTION, ServerConfiguration.PROPERTY_BK_METADATA_DESCRIPTION_DEFAULT);
        return convertLedgerBean(ledgerId, ledgerMetadata, ledger, descriptionPattern);
    }

    private LedgerBean convertLedgerBean(long ledgerId, LedgerMetadata ledgerMetadata, Ledger ledger, String descriptionPattern) throws BookkeeperManagerException {
        LedgerBean b = new LedgerBean();
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

}
