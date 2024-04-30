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

import static org.junit.Assert.assertEquals;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.utils.BookkeeperManagerTestUtils;
import org.junit.Test;


public class LoadMetadataCacheTest extends BookkeeperManagerTestUtils {

    @Test
    public void testLoad() throws Exception {
        startBookie(false, -1);
        ClientConfiguration bkConf = new ClientConfiguration();
        bkConf.setMetadataServiceUri(getMetadataServiceUri());

        BookKeeper bk = BookKeeper.forConfig(bkConf).build();
        WriteHandle wr0 = createLedger(bk);
        WriteHandle wr = createLedger(bk);
        wr.append("test".getBytes());
        final BookkeeperManager bookkeeperManager = getBookkeeperManager();
        bookkeeperManager.doRefreshMetadataCache();
        assertEquals(2, bookkeeperManager.getAllLedgers().size());

        bk.newDeleteLedgerOp()
                .withLedgerId(wr.getId())
                .execute().get();
        bookkeeperManager.doRefreshMetadataCache();
        assertEquals(1, bookkeeperManager.getAllLedgers().size());
    }

    private static WriteHandle createLedger(BookKeeper bk) throws InterruptedException, ExecutionException {
        WriteHandle wr = bk.newCreateLedgerOp()
                .withAckQuorumSize(1)
                .withEnsembleSize(1)
                .withWriteQuorumSize(1)
                .withPassword("p".getBytes())
                .withCustomMetadata(Map.of("meta1", "value1".getBytes()))
                .execute().get();
        return wr;
    }

}