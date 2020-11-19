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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

/**
 * A record in this table means that a ledger is placed over a Bookie
 *
 * @author eolivelli
 */
@Data
@Entity(name = "ledger_bookie")
public class LedgerBookie implements Serializable {

    @Column(columnDefinition = "long")
    @Id
    private long ledgerId;

    @Column(columnDefinition = "int")
    @Id
    private int clusterId;

    @Column(columnDefinition = "string")
    @Id
    private String bookieId;

    public LedgerBookie() {
    }

    public LedgerBookie(long ledgerId, String bookieId, int clusterId) {
        this.ledgerId = ledgerId;
        this.bookieId = bookieId;
        this.clusterId = clusterId;
    }

}
