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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.bookkeeper.client.api.LedgerMetadata;

/**
 * Information about a Ledger.
 *
 * @author enrico.olivelli
 */
public final class LedgerBean implements Serializable {

    private long id;
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

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

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

    public void setMetadataValue(String key, byte[] value) {
        this.metadata.put(key, new String(value, StandardCharsets.UTF_8));
    }

    public void setLedgerMetadata(LedgerMetadata metadata) {
        Map<String, byte[]> customMetadata = metadata.getCustomMetadata();
        for (Map.Entry<String, byte[]> currentCustomMetadata : customMetadata.entrySet()) {
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

    public Map<Long, List<String>> getEnsembles() {
        return ensembles;
    }

    public void setEnsembles(Map<Long, List<String>> ensembles) {
        this.ensembles = ensembles;
    }

    @Override
    public String toString() {
        return "LedgerBean{" + "id=" + id + ", metadata=" + metadata + '}';
    }

}
