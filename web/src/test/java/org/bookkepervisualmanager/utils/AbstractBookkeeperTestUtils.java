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
package org.bookkepervisualmanager.utils;

import static junit.framework.Assert.fail;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SaslAuthenticated;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.proto.BookieServer;
import org.apache.bookkeeper.zookeeper.ZooKeeperClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Test class that provides a testing server able to start Zookeeper and a Bookkeeper Bookie.
 *
 * @author matteo.minardi
 */
public abstract class AbstractBookkeeperTestUtils implements AutoCloseable {

    private static final int CONNECTION_TIMEOUT = 30000;

    static {
        System.setProperty("zookeeper.admin.enableServer", "false");
        System.setProperty("zookeeper.forceSync", "no");
    }

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    LocalZookeeperServer zkServerMain;
    ZooKeeper zkServer;
    BookieServer bookie;
    Path path;

    public AbstractBookkeeperTestUtils() {
    }

    public void startZookeeper() throws Exception {
        path = folder.newFolder().toPath();
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        String dataDir = path.toAbsolutePath().toString();
        zkServerMain = new LocalZookeeperServer(getPort(), dataDir);

        zkServer = new ZooKeeper(getAddress(), CONNECTION_TIMEOUT, (e) -> {
            switch (e.getState()) {
                case SyncConnected:
                case SaslAuthenticated:
                    countDownLatch.countDown();
                    break;
            }
        });

        boolean connected = countDownLatch.await(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        if (!connected) {
            fail("Could not connect to zookeeper at " + getAddress() + " within " + 10000 + " ms");
        }

        try (ZooKeeperClient zkc = ZooKeeperClient
                .newBuilder()
                .connectString(getAddress())
                .sessionTimeoutMs(10000)
                .build()) {

            boolean rootExists = zkc.exists(getPath(), false) != null;

            if (!rootExists) {
                zkc.create(getPath(), new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
    }

    public void startBookie() throws Exception {
        startBookie(true);
    }

    public void startBookie(boolean format) throws Exception {
        if (bookie != null) {
            throw new Exception("bookie already started");
        }
        ServerConfiguration conf = new ServerConfiguration();
        conf.setBookiePort(5621);
        conf.setUseHostNameAsBookieID(true);

        Path targetDir = path.resolve("data");
        conf.setMetadataServiceUri("zk+null://" + getAddress() + "/ledgers");
        conf.setLedgerDirNames(new String[]{targetDir.toAbsolutePath().toString()});
        conf.setJournalDirName(targetDir.toAbsolutePath().toString());
        conf.setFlushInterval(10000);
        conf.setGcWaitTime(5);
        conf.setJournalFlushWhenQueueEmpty(true);
        conf.setAutoRecoveryDaemonEnabled(false);
        conf.setEnableLocalTransport(true);
        conf.setJournalSyncData(false);

        conf.setAllowLoopback(true);
        conf.setProperty("journalMaxGroupWaitMSec", 10); // default 200ms

        if (format) {
            BookKeeperAdmin.initNewCluster(conf);
            BookKeeperAdmin.format(conf, false, true);
        }

        this.bookie = new BookieServer(conf);
        this.bookie.start();
    }

    public void stopBookie() throws Exception {
        if (bookie != null) {
            bookie.shutdown();
            bookie.join();
            bookie = null;
        }
    }

    public ZooKeeper getZookeeperServer() {
        return zkServer;
    }

    public int getPort() {
        return 1282;
    }

    public String getAddress() {
        return "localhost:" + getPort();
    }

    public int getTimeout() {
        return 40000;
    }

    public String getPath() {
        return "/bvm";
    }

    @Override
    public void close() throws Exception {
        try {
            if (bookie != null) {
                bookie.shutdown();
            }
        } catch (Throwable t) {
        }
        try {
            if (zkServer != null) {
                zkServer.close();
            }
        } catch (Throwable t) {
        }
        try {
            if (zkServerMain != null) {
                zkServerMain.close();
            }
        } catch (Throwable t) {
        }
    }

}
