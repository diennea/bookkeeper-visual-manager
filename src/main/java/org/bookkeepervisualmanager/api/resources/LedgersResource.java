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
package org.bookkeepervisualmanager.api.resources;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;

@Path("ledger")
public class LedgersResource extends AbstractBookkeeperResource {

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLedgers(@QueryParam("term") String term, @QueryParam("bookie") String bookie) throws Exception {
        System.out.println("gerLedgers: "+term);        
        return getBookkeeperManger().searchLedgers(term, bookie);       
    }

    @GET
    @Path("metadata/{ledgerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LedgerBean getLedgerMetadata(@PathParam("ledgerId") long ledgerId) throws Exception {

        LedgerMetadata ledgerMetadata = getBookkeeperManger().getLedgerMetadata(ledgerId);        
        return convertLedgerBean(ledgerId, ledgerMetadata);
    }

    private static LedgerBean convertLedgerBean(long ledgerId, LedgerMetadata ledgerMetadata) throws UnsupportedEncodingException {
        LedgerBean b = new LedgerBean();
        b.setId(ledgerId);
        b.setLedgerMetadata(ledgerMetadata);

        b.setEnsembleSize(ledgerMetadata.getEnsembleSize());
        b.setWriteQuorumSize(ledgerMetadata.getWriteQuorumSize());
        b.setAckQuorumSize(ledgerMetadata.getAckQuorumSize());
        b.setLastEntryId(ledgerMetadata.getLastEntryId());
        b.setLength(ledgerMetadata.getLength());
        b.setPassword(ledgerMetadata.hasPassword() ? new String(ledgerMetadata.getPassword(), "UTF-8") : "");
        b.setDigestType(ledgerMetadata.getDigestType() + "");
        b.setCtime(ledgerMetadata.getCtime());
        b.setClosed(ledgerMetadata.isClosed());
        b.setState(ledgerMetadata.getState() + "");
        b.setMetadataFormatVersion(ledgerMetadata.getMetadataFormatVersion());
        b.setBookies(new ArrayList<>(BookkeeperManager.buildBookieList(ledgerMetadata)));
        return b;
    }

    @GET
    @Path("bookie/{bookieId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Long> getLedgersForBookie(@PathParam("bookieId") String bookieId) throws Exception {
        return getBookkeeperManger().getLedgersForBookie(bookieId);
    }

    public static final class LedgerBean implements Serializable {

        private long id;
        private Map<String, String> metadata = new HashMap<>();

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
        private List<String> bookies;

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

        public void setLedgerMetadata(LedgerMetadata metadata) throws UnsupportedEncodingException {
            Map<String, byte[]> customMetadata = metadata.getCustomMetadata();
            for (Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
                setMetadataValue(currentCustomMetadata.getKey(), currentCustomMetadata.getValue());
            }
        }

        public int getEnsembleSize() {
            return ensembleSize;
        }

        public void setEnsembleSize(int ensembleSize) {
            this.ensembleSize = ensembleSize;
        }

        public int getWriteQuorumSize() {
            return writeQuorumSize;
        }

        public void setWriteQuorumSize(int writeQuorumSize) {
            this.writeQuorumSize = writeQuorumSize;
        }

        public int getAckQuorumSize() {
            return ackQuorumSize;
        }

        public void setAckQuorumSize(int ackQuorumSize) {
            this.ackQuorumSize = ackQuorumSize;
        }

        public long getLastEntryId() {
            return lastEntryId;
        }

        public void setLastEntryId(long lastEntryId) {
            this.lastEntryId = lastEntryId;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDigestType() {
            return digestType;
        }

        public void setDigestType(String digestType) {
            this.digestType = digestType;
        }

        public long getCtime() {
            return ctime;
        }

        public void setCtime(long ctime) {
            this.ctime = ctime;
        }

        public boolean isClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public int getMetadataFormatVersion() {
            return metadataFormatVersion;
        }

        public void setMetadataFormatVersion(int metadataFormatVersion) {
            this.metadataFormatVersion = metadataFormatVersion;
        }

        public List<String> getBookies() {
            return bookies;
        }

        public void setBookies(List<String> bookies) {
            this.bookies = bookies;
        }

        @Override
        public String toString() {
            return "LedgerBean{" + "id=" + id + ", metadata=" + metadata + '}';
        }

    }

}
