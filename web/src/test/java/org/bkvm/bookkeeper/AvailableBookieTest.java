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
package org.bkvm.bookkeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.bkvm.cache.Bookie;
import org.bkvm.utils.BookkeeperManagerTestUtils;
import org.junit.Test;

public class AvailableBookieTest extends BookkeeperManagerTestUtils {

    @Test
    public void testAvailableBookiesTwoBookies() throws Exception {
        startBookie(false, -1);

        final BookkeeperManager bookkeeperManager = getBookkeeperManager();
        long now = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        bookkeeperManager.doRefreshMetadataCache();
        long after = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        assertTrue(after != now);
        Collection<Bookie> allBookies = bookkeeperManager.getAllBookies();
        assertEquals(2, allBookies.size());
        stopOneBookie();
        bookkeeperManager.doRefreshMetadataCache();
        long afterError = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        assertTrue(afterError != after);
        allBookies = bookkeeperManager.getAllBookies();
        assertEquals(2, allBookies.size());
    }

    @Test
    public void testAvailableBookiesOneBookie() throws Exception {
        final BookkeeperManager bookkeeperManager = getBookkeeperManager();
        long now = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        bookkeeperManager.doRefreshMetadataCache();
        long after = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        assertTrue(after != now);
        Collection<Bookie> allBookies = bookkeeperManager.getAllBookies();
        assertEquals(1, allBookies.size());

        bookkeeperManager.doRefreshMetadataCache();
        long afterError = bookkeeperManager.getRefreshWorkerStatus().getLastMetadataCacheRefresh();
        assertTrue(afterError != after);
        allBookies = bookkeeperManager.getAllBookies();
        assertEquals(1, allBookies.size());
    }

}
