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
package org.bookkeepervisualmanager.cache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Represent basic ledger metadata
 *
 * @author eolivelli
 */
@Entity(name = "ledger")
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class Ledger implements Serializable {

    @Column(columnDefinition = "long")
    @Id
    private long ledgerId;

    @Column(columnDefinition = "long")
    private long size;

    @Column(columnDefinition = "timestamp")
    private java.sql.Timestamp ctime;

    @Column(columnDefinition = "timestamp")
    private java.sql.Timestamp scanTime;

    @Column(columnDefinition = "string")
    private String serializedMetadata;

    public Ledger() {
    }

    public Ledger(long ledgerId, long size, Timestamp ctime, Timestamp scanTime, String serializedMetadata) {
        this.ledgerId = ledgerId;
        this.size = size;
        this.ctime = ctime;
        this.scanTime = scanTime;
        this.serializedMetadata = serializedMetadata;
    }

    public long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Timestamp getCtime() {
        return ctime;
    }

    public void setCtime(Timestamp ctime) {
        this.ctime = ctime;
    }

    public Timestamp getScanTime() {
        return scanTime;
    }

    public void setScanTime(Timestamp scanTime) {
        this.scanTime = scanTime;
    }

    public String getSerializedMetadata() {
        return serializedMetadata;
    }

    public void setSerializedMetadata(String serializedMetadata) {
        this.serializedMetadata = serializedMetadata;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (this.ledgerId ^ (this.ledgerId >>> 32));
        hash = 29 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 29 * hash + Objects.hashCode(this.ctime);
        hash = 29 * hash + Objects.hashCode(this.scanTime);
        hash = 29 * hash + Objects.hashCode(this.serializedMetadata);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ledger other = (Ledger) obj;
        if (this.ledgerId != other.ledgerId) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (!Objects.equals(this.serializedMetadata, other.serializedMetadata)) {
            return false;
        }
        if (!Objects.equals(this.ctime, other.ctime)) {
            return false;
        }
        if (!Objects.equals(this.scanTime, other.scanTime)) {
            return false;
        }
        return true;
    }

}
