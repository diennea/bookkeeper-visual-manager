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
package org.bookkeepervisualmanager.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import herddb.jdbc.HerdDBEmbeddedDataSource;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class MetadataCacheTest {

    @Test
    public void test() {
        try (HerdDBEmbeddedDataSource datasource = new HerdDBEmbeddedDataSource();) {
            datasource.setUrl("jdbc:herddb:local");
            try (MetadataCache metadataCache = new MetadataCache(datasource)) {
                Ledger ledger = new Ledger(1, 1024, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                List<LedgerBookie> lb = new ArrayList<>();
                lb.add(new LedgerBookie(1, "localhost:1234"));
                lb.add(new LedgerBookie(1, "localhost:1235"));
                List<LedgerMetadataEntry> entries = new ArrayList<>();
                entries.add(new LedgerMetadataEntry(1, "application", "pulsar"));
                entries.add(new LedgerMetadataEntry(1, "component", "foo"));
                entries.add(new LedgerMetadataEntry(1, "other", "foo"));
                metadataCache.updateLedger(ledger, lb, entries);
                List<Ledger> ledgers = metadataCache.listLedgers();
                assertEquals(1, ledgers.size());
                assertEquals(1024, ledgers.get(0).getSize());
                List<Long> ledgersInBookie = metadataCache.getLedgersForBookie("localhost:1234");
                assertEquals(1, ledgersInBookie.size());

                List<Ledger> ledgersByMeta = metadataCache.searchLedgers("foo", null);
                assertEquals(1, ledgersByMeta.size());
                List<Ledger> ledgersByBookie = metadataCache.searchLedgers(null, "localhost:1234");
                assertEquals(1, ledgersByBookie.size());
                List<Ledger> ledgersByBookieAndMeta = metadataCache.searchLedgers("pulsar", "localhost:1234");
                assertEquals(1, ledgersByBookieAndMeta.size());

                // UPDATE, just the size
                Ledger ledger2 = new Ledger(1, 2048, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                metadataCache.updateLedger(ledger2, lb, entries);
                List<Ledger> ledgers2 = metadataCache.listLedgers();
                assertEquals(1, ledgers2.size());
                assertEquals(2048, ledgers2.get(0).getSize());

                List<LedgerBookie> mappings = metadataCache.getBookieForLedger(ledger2.getLedgerId());
                System.out.println("mappings:" + mappings);
                assertEquals(2, mappings.size());

                assertEquals(1, metadataCache.getLedgersForBookie("localhost:1234").size());
                assertEquals(1, metadataCache.getLedgersForBookie("localhost:1235").size());
                assertEquals(0, metadataCache.getLedgersForBookie("localhost:1236").size());

                // UPDATE, re-replication moved data to another bookie
                Ledger ledger2rewritten = new Ledger(1, 2048, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                List<LedgerBookie> lb2 = new ArrayList<>();
                lb2.add(new LedgerBookie(1, "localhost:1234"));
                lb2.add(new LedgerBookie(1, "localhost:1236"));
                metadataCache.updateLedger(ledger2rewritten, lb2, entries);

                assertEquals(1, metadataCache.getLedgersForBookie("localhost:1234").size());
                assertEquals(0, metadataCache.getLedgersForBookie("localhost:1235").size());
                assertEquals(1, metadataCache.getLedgersForBookie("localhost:1236").size());

                System.out.println("ledgers: " + ledgers2);

                // TESTS OVER BOOKIES
                Bookie bookie = new Bookie("bookie:123", "desc", Bookie.STATE_AVAILABLE, new java.sql.Timestamp(System.currentTimeMillis()), 123, 234);
                // insert
                metadataCache.updateBookie(bookie);

                List<Bookie> bookies1 = metadataCache.listBookies();
                assertEquals(bookie, bookies1.get(0));

                Bookie lookup = metadataCache.getBookie(bookie.getBookieId());
                assertEquals(bookie, lookup);

                Bookie bookie2 = new Bookie("bookie:123", "desc", Bookie.STATE_DOWN, new java.sql.Timestamp(System.currentTimeMillis()), 123, 234);
                // update
                metadataCache.updateBookie(bookie2);
                lookup = metadataCache.getBookie("bookie:123");
                assertEquals(bookie2, lookup);

                List<Bookie> bookies = metadataCache.listBookies();
                assertEquals(lookup, bookies.get(0));

                metadataCache.deleteBookie("bookie:123");
                assertTrue(metadataCache.listBookies().isEmpty());
                assertNull(metadataCache.getBookie("bookie:123"));

            }
        }
    }

}
