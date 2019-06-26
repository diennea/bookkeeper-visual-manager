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

import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOOKKEEPER_LEDGERS_PATH;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOOKKEEPER_LEDGERS_PATH_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SERVER;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SERVER_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.bookkeepervisualmanager.config.ConfigurationStore;
import org.bookkeepervisualmanager.config.ConfigurationStoreUtils;

/**
 *
 * @author matteo
 */
public class BookkeeperManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperManager.class.getName());

    private final ConfigurationStore configStore;
    private final ClientConfiguration conf;

    private ZooKeeper zkClient;
    private BookKeeper bkClient;
    private BookKeeperAdmin bkAdmin;

    public BookkeeperManager(ConfigurationStore configStore) throws BookkeeperException {
        try {
            this.configStore = configStore;
            String zkServers = this.configStore.getProperty(PROPERTY_ZOOKEEPER_SERVER,
                    PROPERTY_ZOOKEEPER_SERVER_DEFAULT);
            String zkMetadataServiceUri = this.configStore.getProperty(PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI,
                    PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT);

            int zkSessionTimeout = ConfigurationStoreUtils.getInt(PROPERTY_ZOOKEEPER_SESSION_TIMEOUT,
                    PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT, this.configStore);
            int zkFirstConnectionTimeout = ConfigurationStoreUtils.getInt(PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT,
                    PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT, this.configStore);

            this.conf = new ClientConfiguration();
            this.conf.setMetadataServiceUri(zkMetadataServiceUri);

            LOG.log(Level.INFO, "Starting Zookeeper first connection with connection string = {0}",
                    zkMetadataServiceUri);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.zkClient = new ZooKeeper(zkServers, zkSessionTimeout, e -> {
                switch (e.getState()) {
                    case SyncConnected:
                        LOG.log(Level.INFO, "Zookeeper connection established.");
                        countDownLatch.countDown();
                        break;
                    case Disconnected:
                        LOG.log(Level.INFO, "Zookeeper connection lost.");
                        break;
                    case AuthFailed:
                        LOG.log(Level.INFO, "Zookeeper auth failed.");
                        throw new RuntimeException("Auth failed.");
                    default:
                        break;
                }
            });
            boolean connectedBeforeTimeout = countDownLatch.await(zkFirstConnectionTimeout, TimeUnit.MILLISECONDS);
            if (!connectedBeforeTimeout) {
                LOG.log(Level.INFO, "Zookeeper first connection failed.");
                throw new BookkeeperException("First connection to " + conf.getMetadataServiceUri() + " failed.");
            }

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

    public LedgerManager getLedgerManager() {
        return bkClient.getLedgerManager();
    }

    public BookKeeperAdmin getBookkeeperAdmin() {
        return bkAdmin;
    }

    public List<Long> getLedgersForBookie(String bookieId) throws BookkeeperException {
        try {
            BookieSocketAddress bookieAddress = new BookieSocketAddress(bookieId);
            Set<BookieSocketAddress> bookieSet = new HashSet<>(1);
            bookieSet.add(bookieAddress);

            SortedMap<Long, LedgerMetadata> forBookie = bkAdmin.getLedgersContainBookies(bookieSet);
            return new ArrayList<>(forBookie.keySet());
        } catch (UnknownHostException | InterruptedException | BKException e) {
            throw new BookkeeperException(e);
        }
    }

    public LedgerMetadata getLedgerMetadata(long ledgerId) throws BookkeeperException {
        AtomicReference<LedgerMetadata> ledgerMetadata = new AtomicReference<>();
        try {
            getLedgerManager().readLedgerMetadata(ledgerId).whenComplete((metadata, exception) -> {
                if (exception == null) {
                    ledgerMetadata.set(metadata.getValue());
                }
            }).join();
            return ledgerMetadata.get();
        } catch (Throwable e) {
            throw new BookkeeperException(e);
        }
    }

    public Map<BookieSocketAddress, BookieInfo> getBookieInfo() throws BookkeeperException {
        try {
            return bkClient.getBookieInfo();
        } catch (InterruptedException | BKException e) {
            throw new BookkeeperException(e);
        }
    }

    public List<Long> getAllLedgers() throws BookkeeperException {
        try {
            List<Long> resultLedgers = new ArrayList<>();

            Iterable<Long> ledgersIds = bkAdmin.listLedgers();
            ledgersIds.forEach(resultLedgers::add);

            return resultLedgers;
        } catch (Throwable e) {
            throw new BookkeeperException(e);
        }
    }

    public Collection<BookieSocketAddress> getAllBookies() throws BookkeeperException {
        try {
            String bkLedgersPath = this.configStore.getProperty(PROPERTY_BOOKKEEPER_LEDGERS_PATH,
                    PROPERTY_BOOKKEEPER_LEDGERS_PATH_DEFAULT);

            Collection<BookieSocketAddress> result = new ArrayList<>();

            Stat stat = zkClient.exists(bkLedgersPath, true);
            if (stat == null) {
                return result;
            }
            List<String> bkCookies = zkClient.getChildren(bkLedgersPath + "/cookies", false);
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
