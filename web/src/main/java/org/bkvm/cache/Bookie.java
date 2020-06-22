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
package org.bkvm.cache;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author eolivelli
 */
@Entity(name = "bookie")
public class Bookie implements Serializable {

    public static final int STATE_DOWN = 0;
    public static final int STATE_AVAILABLE = 1;
    public static final int STATE_READONLY = 2;

    @Column(columnDefinition = "string")
    @Id
    private String bookieId;
    
    @Column(columnDefinition = "string")
    private String clusterName;

    @Column(columnDefinition = "string")
    private String description;

    @Column(columnDefinition = "int")
    private int state;

    @Column(columnDefinition = "timestamp")
    private java.sql.Timestamp scanTime;

    @Column(columnDefinition = "long")
    private long freeDiskspace;

    @Column(columnDefinition = "long")
    private long totalDiskspace;

    public Bookie() {
    }

    public Bookie(String bookieId, String description, int state, Timestamp scanTime, long freeDiskspace, long totalDiskspace) {
        this.bookieId = bookieId;
        this.description = description;
        this.state = state;
        this.scanTime = scanTime;
        this.freeDiskspace = freeDiskspace;
        this.totalDiskspace = totalDiskspace;
    }

    public String getBookieId() {
        return bookieId;
    }

    public void setBookieId(String bookieId) {
        this.bookieId = bookieId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Timestamp getScanTime() {
        return scanTime;
    }

    public void setScanTime(Timestamp scanTime) {
        this.scanTime = scanTime;
    }

    public long getFreeDiskspace() {
        return freeDiskspace;
    }

    public void setFreeDiskspace(long freeDiskspace) {
        this.freeDiskspace = freeDiskspace;
    }

    public long getTotalDiskspace() {
        return totalDiskspace;
    }

    public void setTotalDiskspace(long totalDiskspace) {
        this.totalDiskspace = totalDiskspace;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.bookieId);
        hash = 59 * hash + this.state;
        hash = 59 * hash + Objects.hashCode(this.scanTime);
        hash = 59 * hash + (int) (this.freeDiskspace ^ (this.freeDiskspace >>> 32));
        hash = 59 * hash + (int) (this.totalDiskspace ^ (this.totalDiskspace >>> 32));
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
        final Bookie other = (Bookie) obj;
        if (this.state != other.state) {
            return false;
        }
        if (this.freeDiskspace != other.freeDiskspace) {
            return false;
        }
        if (this.totalDiskspace != other.totalDiskspace) {
            return false;
        }
        if (!Objects.equals(this.bookieId, other.bookieId)) {
            return false;
        }
        if (!Objects.equals(this.scanTime, other.scanTime)) {
            return false;
        }
        return true;
    }


}
