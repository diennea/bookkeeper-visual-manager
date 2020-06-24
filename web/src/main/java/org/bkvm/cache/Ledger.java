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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represent basic ledger metadata
 *
 * @author eolivelli
 */
@Data
@EqualsAndHashCode
@Entity(name = "ledger")
public class Ledger implements Serializable {

    @Column(columnDefinition = "long")
    @Id
    private long ledgerId;

    @Column(columnDefinition = "int")
    private int clusterId;

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

    public long getAge() {
        return Duration.between(this.ctime.toInstant(), Instant.now()).toMinutes();
    }

    @Override
    public String toString() {
        return "Ledger{" + "ledgerId=" + ledgerId + '}';
    }
}
