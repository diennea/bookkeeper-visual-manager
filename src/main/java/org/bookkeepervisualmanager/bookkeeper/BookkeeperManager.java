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
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.net.BookieSocketAddress;
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

    private BookKeeper bkClient;
    private BookKeeperAdmin bkAdmin;

    public BookkeeperManager(ConfigurationStore configStore) throws BookkeeperException {
        try {
            this.configStore = configStore;
            int zkSessionTimeout = ConfigurationStoreUtils.getInt(PROPERTY_ZOOKEEPER_SESSION_TIMEOUT,
                    PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT, this.configStore);
            int zkFirstConnectionTimeout = ConfigurationStoreUtils.getInt(PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT,
                    PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT, this.configStore);
            String zkMetadataServiceUri = this.configStore.getProperty(PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI,
                    PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT);

            this.conf = new ClientConfiguration()
                    .setZkTimeout(zkSessionTimeout)
                    .setMetadataServiceUri(zkMetadataServiceUri)
                    .setClientConnectTimeoutMillis(zkFirstConnectionTimeout);

            LOG.log(Level.INFO, "Starting bookkeeper client with connection string = {0}", zkMetadataServiceUri);
            this.bkClient = BookKeeper.forConfig(conf).build();

            LOG.log(Level.INFO, "Starting bookkeeper admin.");
            this.bkAdmin = new BookKeeperAdmin(bkClient);

        } catch (Throwable t) {
            throw new BookkeeperException(t);
        }
    }

    @Override
    public void close() throws BookkeeperException {
        try {
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
            bkClient = null;
            bkAdmin = null;
        }
    }

    public BookKeeper getBookkeeper() {
        return bkClient;
    }

    public BookKeeperAdmin getBookkeeperAdmin() {
        return bkAdmin;
    }

    public LedgerManager getLedgerManager() {
        return bkClient.getLedgerManager();
    }

    public List<Long> getLedgersForBookie(String bookieId) throws BookkeeperException {
        try {
            SortedMap<Long, LedgerMetadata> forBookie = bkAdmin.getLedgersContainBookies(
                    Set.of(new BookieSocketAddress(bookieId))
            );
            return new ArrayList<>(forBookie.keySet());
        } catch (Throwable e) {
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
        } catch (Throwable e) {
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
            return bkAdmin.getAllBookies();
        } catch (Throwable e) {
            throw new BookkeeperException(e);
        }
    }

}
