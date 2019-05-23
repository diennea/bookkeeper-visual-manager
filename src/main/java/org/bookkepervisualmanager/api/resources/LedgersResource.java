/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package org.bookkepervisualmanager.api.resources;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.client.api.LedgerMetadata;

@Path("ledger")
public class LedgersResource extends AbstractBookkeeperResource {

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LedgerBean> getLedgers() throws Exception {
        final List<LedgerBean> ledgers = new ArrayList<>();

        Map<Long, LedgerMetadata> allLedgers = getBookkeeperManger().getAllLedgers();
        for (Entry<Long, LedgerMetadata> currentLedger : allLedgers.entrySet()) {
            LedgerBean b = new LedgerBean();

            long lid = currentLedger.getKey();
            b.setId(lid);

            LedgerMetadata ledgerMetadata = currentLedger.getValue();
            Map<String, byte[]> customMetadata = ledgerMetadata.getCustomMetadata();
            for (Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
                b.setMetadataValue(currentCustomMetadata.getKey(), currentCustomMetadata.getValue());
            }
            ledgers.add(b);
        };

        return ledgers;
    }

    @GET
    @Path("metadata/{ledgerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LedgerBean getLedgerMetadata(@PathParam("ledgerId") long ledgerId) throws Exception {

        LedgerMetadata ledgerMetadata = getBookkeeperManger().getLedgerMetadata(ledgerId);
        LedgerBean b = new LedgerBean();
        b.setId(ledgerId);

        Map<String, byte[]> customMetadata = ledgerMetadata.getCustomMetadata();
        for (Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
            b.setMetadataValue(currentCustomMetadata.getKey(), currentCustomMetadata.getValue());
        }

        return b;
    }

    @GET
    @Path("bookie/{bookieId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LedgerBean> getLedgersForBookie(@PathParam("bookieId") String bookieId) throws Exception {
        final List<LedgerBean> ledgers = new ArrayList<>();

        SortedMap<Long, LedgerMetadata> forBookie = getBookkeeperManger().getLedgersForBookie(bookieId);
        for (Entry<Long, LedgerMetadata> currentLedger : forBookie.entrySet()) {
            LedgerBean b = new LedgerBean();

            long lid = currentLedger.getKey();
            b.setId(lid);

            LedgerMetadata ledgerMetadata = currentLedger.getValue();
            Map<String, byte[]> customMetadata = ledgerMetadata.getCustomMetadata();
            for (Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
                b.setMetadataValue(currentCustomMetadata.getKey(), currentCustomMetadata.getValue());
            }
            ledgers.add(b);
        };

        return ledgers;
    }

    public final class LedgerBean implements Serializable {

        private long id;
        private Map<String, String> metadata = new HashMap<>();

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        public void setMetadataValue(String key, byte[] value) throws UnsupportedEncodingException {
            this.metadata.put(key, new String(value, "UTF-8"));
        }

        @Override
        public String toString() {
            return "LedgerBean{" + "id=" + id + ", metadata=" + metadata + '}';
        }

    }

}
