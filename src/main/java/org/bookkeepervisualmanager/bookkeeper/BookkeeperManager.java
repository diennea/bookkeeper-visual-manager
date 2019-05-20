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
package org.bookkeepervisualmanager.bookkeeper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bookkeeper.client.BKException;

import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 *
 * @author matteo
 */
public class BookkeeperManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperManager.class.getName());

    public static final String BK_LEDGERS_PATH = System.getProperty("bk.ledgers.path", "/ledgers");
    public static final String ZK_SERVER = System.getProperty("zk.servers", "127.0.0.1:2181");
    public static final int ZK_TIMEOUT = 1000;

    private final ClientConfiguration conf;

    private ZooKeeper zkClient;
    private BookKeeper bkClient;
    private BookKeeperAdmin bkAdmin;

    public BookkeeperManager(String zkServers) throws BookkeeperException {
        try {
            this.conf = new ClientConfiguration();
            this.conf.setMetadataServiceUri("zk+null://" + zkServers.replace(",", ";") + BK_LEDGERS_PATH);

            LOG.log(Level.INFO, "Starting zookeeper connection with connection string = {0}", zkServers);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.zkClient = new ZooKeeper(zkServers, ZK_TIMEOUT, e -> {
                switch (e.getState()) {
                    case SyncConnected:
                        LOG.log(Level.INFO, "Zookeeper connection established.");
                        countDownLatch.countDown();
                        break;
                }
            });
            countDownLatch.await();

            LOG.log(Level.INFO, "Starting bookkeeper connection with zookeeper. "
                    + "Zookeeper connected = {0}", zkClient.getState().isConnected());
            this.bkClient = BookKeeper.forConfig(conf)
                    .zk(zkClient)
                    .build();

            LOG.log(Level.INFO, "Starting bookkeeper admin.");
            this.bkAdmin = new BookKeeperAdmin(bkClient);

        } catch (Throwable t) {
            throw new BookkeeperException(t);
        }
    }

    @Override
    public void close() throws BookkeeperException {
        try {
            if (zkClient != null) {
                LOG.log(Level.INFO, "Closing zookeeper connection");
                zkClient.close();
            }
            if (bkClient != null) {
                LOG.log(Level.INFO, "Closing bookkeeper connection");
                bkClient.close();
            }
            if (bkAdmin != null) {
                LOG.log(Level.INFO, "Closing bookkeeper admin connection");
                bkAdmin.close();
            }
        } catch (Throwable t) {
            throw new BookkeeperException(t);
        } finally {
            zkClient = null;
            bkClient = null;
            bkAdmin = null;
        }
    }

    public ZooKeeper getZookeeper() {
        return zkClient;
    }

    public BookKeeper getBookkeeper() {
        return bkClient;
    }

    public BookKeeperAdmin getBookkeeperAdmin() {
        return bkAdmin;
    }

    public SortedMap<Long, LedgerMetadata> getLedgersForBookie(String bookieId) throws BookkeeperException {
        try {
            BookieSocketAddress bookieAddress = new BookieSocketAddress(bookieId);
            Set<BookieSocketAddress> bookieSet = new HashSet<>(1);
            bookieSet.add(bookieAddress);

            return bkAdmin.getLedgersContainBookies(bookieSet);
        } catch (UnknownHostException | InterruptedException | BKException e) {
            throw new BookkeeperException(e);
        }
    }

    public Map<BookieSocketAddress, BookieInfo> getAvailableBookies() throws BookkeeperException {
        try {
            return bkClient.getBookieInfo();
        } catch (InterruptedException | BKException e) {
            throw new BookkeeperException(e);
        }
    }

    public SortedMap<Long, LedgerMetadata> getAllLedgers() throws BookkeeperException {
        try {
            final SortedMap<Long, LedgerMetadata> ledgers = new TreeMap<>();

            bkAdmin.listLedgers().forEach(lid -> {
                bkClient.getLedgerManager().readLedgerMetadata(lid)
                        .whenComplete((metadata, exception) -> {
                            if (exception != null) {
                                ledgers.put(lid, metadata.getValue());
                            }
                        });
            });

            return ledgers;
        } catch (IOException e) {
            throw new BookkeeperException(e);
        }
    }

    public Collection<BookieSocketAddress> getAllBookies() throws BookkeeperException {
        try {
            Collection<BookieSocketAddress> result = new ArrayList<>();

            Stat stat = zkClient.exists(BK_LEDGERS_PATH, true);
            if (stat == null) {
                return result;
            }
            List<String> bkCookies = zkClient.getChildren(BK_LEDGERS_PATH + "/cookies", false);
            if (bkCookies == null) {
                return result;
            }
            for (String bookieAddress : bkCookies) {
                result.add(new BookieSocketAddress(bookieAddress));
            }

            return result;
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            throw new BookkeeperException(e);
        }

    }

}
