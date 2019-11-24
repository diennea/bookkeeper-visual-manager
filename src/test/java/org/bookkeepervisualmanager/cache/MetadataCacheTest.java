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

import static org.junit.Assert.assertEquals;
import herddb.jdbc.HerdDBEmbeddedDataSource;
import java.util.List;
import org.junit.Test;

public class MetadataCacheTest {

    @Test
    public void test() {
        try (HerdDBEmbeddedDataSource datasource = new HerdDBEmbeddedDataSource();) {
            datasource.setUrl("jdbc:herddb:local");
            try (MetadataCache metadataCache = new MetadataCache(datasource)) {
                Ledger ledger = new Ledger(1, 1024, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                metadataCache.updateLedger(ledger);
                List<Ledger> ledgers = metadataCache.listLedgers();
                assertEquals(1, ledgers.size());
                assertEquals(1024, ledgers.get(0).getSize());

                // UPDATE, just the size
                Ledger ledger2 = new Ledger(1, 2048, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                metadataCache.updateLedger(ledger2);
                List<Ledger> ledgers2 = metadataCache.listLedgers();
                assertEquals(1, ledgers2.size());
                assertEquals(2048, ledgers2.get(0).getSize());

                System.out.println("ledgers: " + ledgers2);

            }
        }
    }

}
