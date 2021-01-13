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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import herddb.jdbc.HerdDBEmbeddedDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MetadataCacheTest {

    @Test
    public void test() {
        try (HerdDBEmbeddedDataSource datasource = new HerdDBEmbeddedDataSource();) {
            datasource.setUrl("jdbc:herddb:local");
            int clusterId = 1234;
            try (MetadataCache metadataCache = new MetadataCache(datasource)) {
                Ledger ledger = new Ledger(1, clusterId, 1024, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                List<LedgerBookie> lb = new ArrayList<>();
                lb.add(new LedgerBookie(1, "localhost:1234", clusterId));
                lb.add(new LedgerBookie(1, "localhost:1235", clusterId));
                List<LedgerMetadataEntry> entries = new ArrayList<>();
                entries.add(new LedgerMetadataEntry(1, clusterId, "application", "pulsar"));
                entries.add(new LedgerMetadataEntry(1, clusterId, "component", "foo"));
                entries.add(new LedgerMetadataEntry(1, clusterId, "other", "foo"));
                entries.add(new LedgerMetadataEntry(1, clusterId, "metadataentry", "4933"));
                entries.add(new LedgerMetadataEntry(1, clusterId, "metadataentry2", "Thu Aug 22 2019 12:29:58 GMT+0200 (Central European Summer Time)"));
                metadataCache.updateLedger(ledger, lb, entries);
                List<Ledger> ledgers = metadataCache.listLedgers();
                assertEquals(1, ledgers.size());
                assertEquals(1024, ledgers.get(0).getSize());
                Ledger read = metadataCache.getLedgerMetadata(clusterId, 1);
                assertEquals(1024, read.getSize());
                List<Long> ledgersInBookie = metadataCache.getLedgersForBookie(clusterId, "localhost:1234");
                assertEquals(1, ledgersInBookie.size());

                List<Ledger> ledgersByMetaNoCluster = metadataCache.searchLedgers("foo", null, null, null);
                assertEquals(1, ledgersByMetaNoCluster.size());

                List<Ledger> ledgersByMeta = metadataCache.searchLedgers("foo", null, clusterId, null);
                assertEquals(1, ledgersByMeta.size());
                List<Ledger> ledgersByBookie = metadataCache.searchLedgers(null, "localhost:1234", clusterId, null);
                assertEquals(1, ledgersByBookie.size());
                List<Ledger> ledgersByBookieAndMeta = metadataCache.searchLedgers("pulsar", "localhost:1234", clusterId, null);
                assertEquals(1, ledgersByBookieAndMeta.size());
                List<Ledger> ledgersByBookieAndMetaAndIdOk = metadataCache.searchLedgers("pulsar", "localhost:1234", clusterId, Arrays.asList(1L));
                assertEquals(1, ledgersByBookieAndMetaAndIdOk.size());
                List<Ledger> ledgersByBookieAndMetaAndMixedId = metadataCache.searchLedgers("pulsar", "localhost:1234", clusterId, Arrays.asList(new Long[]{1L, 2L}));
                assertEquals(1, ledgersByBookieAndMetaAndMixedId.size());
                List<Ledger> ledgersByBookieAndMetaAndIdKo = metadataCache.searchLedgers("pulsar", "localhost:1234", clusterId, Arrays.asList(2L));
                assertEquals(0, ledgersByBookieAndMetaAndIdKo.size());
                List<Ledger> ledgersByBookieAndMetaAndEmtyList = metadataCache.searchLedgers("pulsar", "localhost:1234", clusterId, Collections.EMPTY_LIST);
                assertEquals(0, ledgersByBookieAndMetaAndEmtyList.size());
                List<Ledger> ledgersByKeyAndValue = metadataCache.searchLedgers("metadataentry:4933", null, clusterId, null);
                assertEquals(1, ledgersByKeyAndValue.size());
                List<Ledger> ledgersByKeyAndValue1 = metadataCache.searchLedgers("metadataentry2:Thu Aug 22 2019 12:29:58 GMT+0200 (Central European Summer Time)", null, clusterId, null);
                assertEquals(1, ledgersByKeyAndValue1.size());
                List<Ledger> ledgersByKeyAndValue2 = metadataCache.searchLedgers("metadataentry: 4933:", null, clusterId, null);
                assertEquals(0, ledgersByKeyAndValue2.size());
                List<Ledger> ledgersByKeyAndValue3 = metadataCache.searchLedgers("metadataentry:", null, clusterId, null);
                assertEquals(0, ledgersByKeyAndValue3.size());
                List<Ledger> ledgersByKeyAndValue4 = metadataCache.searchLedgers("4933", null, clusterId, null);
                assertEquals(1, ledgersByKeyAndValue4.size());

                // UPDATE, just the size
                Ledger ledger2 = new Ledger(1, clusterId, 2048, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                metadataCache.updateLedger(ledger2, lb, entries);
                List<Ledger> ledgers2 = metadataCache.listLedgers();
                assertEquals(1, ledgers2.size());
                assertEquals(2048, ledgers2.get(0).getSize());

                List<LedgerBookie> mappings = metadataCache.getBookieForLedger(clusterId, ledger2.getLedgerId());
                System.out.println("mappings:" + mappings);
                assertEquals(2, mappings.size());

                assertEquals(1, metadataCache.getLedgersForBookie(clusterId, "localhost:1234").size());
                assertEquals(1, metadataCache.getLedgersForBookie(clusterId, "localhost:1235").size());
                assertEquals(0, metadataCache.getLedgersForBookie(clusterId, "localhost:1236").size());

                // UPDATE, re-replication moved data to another bookie
                Ledger ledger2rewritten = new Ledger(1, clusterId, 2048, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()), "");
                List<LedgerBookie> lb2 = new ArrayList<>();
                lb2.add(new LedgerBookie(1, "localhost:1234", clusterId));
                lb2.add(new LedgerBookie(1, "localhost:1236", clusterId));
                metadataCache.updateLedger(ledger2rewritten, lb2, entries);

                assertEquals(1, metadataCache.getLedgersForBookie(clusterId, "localhost:1234").size());
                assertEquals(0, metadataCache.getLedgersForBookie(clusterId, "localhost:1235").size());
                assertEquals(1, metadataCache.getLedgersForBookie(clusterId, "localhost:1236").size());

                System.out.println("ledgers: " + ledgers2);

                // TESTS OVER BOOKIES
                Bookie bookie = new Bookie("bookie:123", clusterId, "desc", Bookie.STATE_AVAILABLE, new java.sql.Timestamp(System.currentTimeMillis()), 123, 234, null);
                // insert
                metadataCache.updateBookie(bookie);

                List<Bookie> bookiesNoCluster = metadataCache.listBookies();
                assertEquals(bookie, bookiesNoCluster.get(0));

                List<Bookie> bookies1 = metadataCache.listBookies(clusterId);
                assertEquals(bookie, bookies1.get(0));

                List<Bookie> bookiesBadCluster = metadataCache.listBookies(clusterId + 10000);
                assertTrue(bookiesBadCluster.isEmpty());

                Bookie lookup = metadataCache.getBookie(clusterId, bookie.getBookieId());
                assertEquals(bookie, lookup);

                Bookie bookie2 = new Bookie("bookie:123", clusterId, "desc", Bookie.STATE_DOWN, new java.sql.Timestamp(System.currentTimeMillis()), 123, 234, null);
                // update
                metadataCache.updateBookie(bookie2);
                lookup = metadataCache.getBookie(clusterId, "bookie:123");
                assertEquals(bookie2, lookup);

                List<Bookie> bookies = metadataCache.listBookies();
                assertEquals(lookup, bookies.get(0));

                metadataCache.deleteBookie(clusterId, "bookie:123");
                assertTrue(metadataCache.listBookies().isEmpty());
                assertNull(metadataCache.getBookie(clusterId, "bookie:123"));

                Cluster cluster = new Cluster(clusterId, "test", "");
                metadataCache.updateCluster(cluster);
                assertNotNull(metadataCache.getCluster(clusterId));
                metadataCache.deleteCluster(clusterId);
                assertNull(metadataCache.getCluster(clusterId));

                List<Ledger> ledgersAfterClusterDelete = metadataCache.listLedgers();
                assertEquals(0, ledgersAfterClusterDelete.size());
            }
        }
    }

}
