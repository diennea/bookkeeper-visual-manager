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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Custom ledger metadata, expanded to a table in order to perform queries
 *
 * @author eolivelli
 */
@Entity(name = "ledger_metadata")
@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class LedgerMetadataEntry implements Serializable {

    @Column(columnDefinition = "long")
    @Id
    private long ledgerId;

    @Column(columnDefinition = "string")
    @Id
    private String entryName;

    @Column(columnDefinition = "string")
    private String entryValue;

    public LedgerMetadataEntry() {
    }

    public LedgerMetadataEntry(long ledgerId, String entryName, String entryValue) {
        this.ledgerId = ledgerId;
        this.entryName = entryName;
        this.entryValue = entryValue;
    }

    public long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryValue() {
        return entryValue;
    }

    public void setEntryValue(String entryValue) {
        this.entryValue = entryValue;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 57 * hash + (int) (this.ledgerId ^ (this.ledgerId >>> 32));
        hash = 57 * hash + Objects.hashCode(this.entryName);
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
        final LedgerMetadataEntry other = (LedgerMetadataEntry) obj;
        if (this.ledgerId != other.ledgerId) {
            return false;
        }
        if (!Objects.equals(this.entryName, other.entryName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LedgerMetadataEntry{" + "ledgerId=" + ledgerId + ", entryName=" + entryName;
    }
}
