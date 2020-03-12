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
package org.bookkeepervisualmanager.api.resources;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperException;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;
import org.bookkeepervisualmanager.cache.Ledger;

@Path("ledger")
public class LedgersResource extends AbstractBookkeeperResource {

    @GET
    @Secured
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LedgerBean> getLedgers(@QueryParam("term") String term,
                                       @QueryParam("bookie") String bookie,
                                       @QueryParam("ledgerIds") String ledgerIds,
                                       @QueryParam("minLength") String minLength,
                                       @QueryParam("maxLength") String maxLength,
                                       @QueryParam("minAge") String minAge
    ) throws Exception {

        List<Long> ids = getBookkeeperManger().searchLedgers(term, bookie, convertParamLongList(ledgerIds), convertParamInt(minLength),
                convertParamInt(maxLength), convertParamInt(minAge));
        List<LedgerBean> res = new ArrayList<>();
        for (long id : ids) {
            LedgerBean bean = getLedgerMetadata(id);
            if (bean != null) {
                res.add(bean);
            }
        }
        return res;
    }

    @GET
    @Secured
    @Path("metadata/{ledgerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LedgerBean getLedgerMetadata(@PathParam("ledgerId") long ledgerId) throws Exception {
        Ledger ledgerMetadata = getBookkeeperManger().getLedger(ledgerId);
        return convertLedgerBean(ledgerId, ledgerMetadata);
    }

    private LedgerBean convertLedgerBean(long ledgerId, Ledger ledger) throws BookkeeperException {
        LedgerMetadata ledgerMetadata = getBookkeeperManger().getLedgerMetadata(ledger);
        LedgerBean b = new LedgerBean();
        b.setId(ledgerId);
        b.setLedgerMetadata(ledgerMetadata);
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
        b.setBookies(new ArrayList<>(BookkeeperManager.buildBookieList(ledgerMetadata)));
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
     * Split a string into a Long list. If any value is invalid, return empty list
     *
     * @param s
     * @return List
     */
    private List<Long> convertParamLongList(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        List<Long> res = new ArrayList<>();
        for (String _s : s.split(",")) {
            _s = _s.trim();
            if (!_s.isEmpty()) {
                try {
                    Long l = Long.parseLong(_s);
                    res.add(l);
                } catch (NumberFormatException ex) {
                    res.clear();
                    break;
                }
            }
        }
        return res;
    }

}
