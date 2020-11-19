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
package org.bkvm.cache;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Custom ledger metadata, expanded to a table in order to perform queries
 *
 * @author eolivelli
 */
@Data
@EqualsAndHashCode
@Entity(name = "ledger_metadata")
public class LedgerMetadataEntry implements Serializable {

    @Id
    @Column(columnDefinition = "long")
    private long ledgerId;

    @Id
    @Column(columnDefinition = "int")
    private int clusterId;

    @Id
    @Column(columnDefinition = "string")
    private String entryName;

    @Column(columnDefinition = "string")
    private String entryValue;

    public LedgerMetadataEntry() {
    }

    public LedgerMetadataEntry(long ledgerId, int clusterId, String entryName, String entryValue) {
        this.ledgerId = ledgerId;
        this.clusterId = clusterId;
        this.entryName = entryName;
        this.entryValue = entryValue;
    }

    @Override
    public String toString() {
        return "LedgerMetadataEntry{" + "ledgerId=" + ledgerId + ", clusterId=" + clusterId + ", entryName=" + entryName;
    }
}
