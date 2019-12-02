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
package org.bookkeepervisualmanager.bookkeeper;

import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT;
import static org.bookkeepervisualmanager.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BKException.BKNoSuchLedgerExistsOnMetadataServerException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.meta.LedgerMetadataSerDe;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.bookkeepervisualmanager.cache.Ledger;
import org.bookkeepervisualmanager.cache.LedgerBookie;
import org.bookkeepervisualmanager.cache.LedgerMetadataEntry;
import org.bookkeepervisualmanager.cache.MetadataCache;
import org.bookkeepervisualmanager.config.ConfigurationNotValidException;
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

    private final BookKeeper bkClient;
    private final BookKeeperAdmin bkAdmin;
    private final MetadataCache metadataCache;
    private final LedgerMetadataSerDe serDe = new LedgerMetadataSerDe();
    private final ExecutorService refreshThread;

    public enum RefreshStatus {
        IDLE,
        WORKING
    }
    private volatile long lastMetadataCacheRefresh;
    private volatile AtomicReference<RefreshStatus> refreshStatus = new AtomicReference<>(RefreshStatus.IDLE);

    public BookkeeperManager(ConfigurationStore configStore, MetadataCache metadataCache) throws BookkeeperException {
        try {
            this.refreshThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "bk-visual-manager-cache-refresh");
                    t.setDaemon(true);
                    return t;
                }
            });
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

            this.metadataCache = metadataCache;
        } catch (IOException | InterruptedException | BKException | ConfigurationNotValidException t) {
            throw new BookkeeperException(t);
        }
    }

    public static final class RefreshCacheWorkerStatus {

        private final RefreshStatus status;
        private final long lastMetadataCacheRefresh;

        public RefreshCacheWorkerStatus(RefreshStatus status, long lastMetadataCacheRefresh) {
            this.status = status;
            this.lastMetadataCacheRefresh = lastMetadataCacheRefresh;
        }

        public RefreshStatus getStatus() {
            return status;
        }

        public long getLastMetadataCacheRefresh() {
            return lastMetadataCacheRefresh;
        }

    }

    public RefreshCacheWorkerStatus refreshMetadataCache() throws BookkeeperException {
        if (refreshStatus.compareAndSet(RefreshStatus.IDLE, RefreshStatus.WORKING)) {
            this.refreshThread.submit(() -> {
                doRefreshMetadataCache();
            });
        }
        return getRefreshWorkerStatus();
    }

    public RefreshCacheWorkerStatus getRefreshWorkerStatus() {
        return new RefreshCacheWorkerStatus(refreshStatus.get(), lastMetadataCacheRefresh);
    }

    private void doRefreshMetadataCache() {
        LOG.info("Refreshing Metadata Cache");
        try {
            Iterable<Long> ledgersIds = bkAdmin.listLedgers();
            for (long ledgerId : ledgersIds) {
                LedgerMetadata ledgerMetadata = readLedgerMetadata(ledgerId);
                if (ledgerMetadata == null) {
                    // ledger disappeared
                    metadataCache.deleteLedger(ledgerId);
                    return;
                }
                Ledger ledger = new Ledger(ledgerId,
                        ledgerMetadata.getLength(),
                        new java.sql.Timestamp(ledgerMetadata.getCtime()),
                        new java.sql.Timestamp(System.currentTimeMillis()),
                        Base64.getEncoder().encodeToString(serDe.serialize(ledgerMetadata)));
                List<LedgerMetadataEntry> metadataEntries = new ArrayList<>();
                ledgerMetadata.getCustomMetadata().forEach((n, v) -> {
                    metadataEntries.add(new LedgerMetadataEntry(ledgerId, n, new String(v, StandardCharsets.UTF_8)));
                });
                List<LedgerBookie> bookies = new ArrayList<>();
                Set<String> bookieAddresses = buildBookieList(ledgerMetadata);
                bookieAddresses.forEach(s -> {
                    bookies.add(new LedgerBookie(ledgerId, s));
                });
                LOG.log(Level.INFO, "Updating ledeger {0} metadata", ledgerId);
                metadataCache.updateLedger(ledger, bookies, metadataEntries);
            }

            lastMetadataCacheRefresh = System.currentTimeMillis();
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Cannot refresh metadata", e);
        } finally {
             refreshStatus.compareAndSet(RefreshStatus.WORKING, RefreshStatus.IDLE);
        }
    }

    public static Set<String> buildBookieList(LedgerMetadata ledgerMetadata) {
        Set<String> bookieAddresses = new HashSet<>();
        ledgerMetadata.getAllEnsembles().values().forEach(bookieList -> {
            bookieList.forEach(bookieAddress -> {
                bookieAddresses.add(bookieAddress.toString());
            });
        });
        return bookieAddresses;
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
            if (refreshThread != null) {
                refreshThread.shutdown();
            }
        } catch (Throwable t) {
            throw new BookkeeperException(t);
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
        return metadataCache.getLedgersForBookie(bookieId);
    }

    public LedgerMetadata getLedgerMetadata(long ledgerId) throws BookkeeperException {
        Ledger ledger = metadataCache.getLedgerMetadata(ledgerId);
        return convertLedgerMetadata(ledger);
    }

    private LedgerMetadata convertLedgerMetadata(Ledger ledger) throws BookkeeperException {
        try {
            if (ledger == null) {
                return null;
            }
            return serDe.parseConfig(
                    Base64.getDecoder().decode(ledger.getSerializedMetadata()),
                    Optional.of(ledger.getCtime().getTime()));
        } catch (IOException ex) {
            throw new BookkeeperException(ex);
        }
    }

    private LedgerMetadata readLedgerMetadata(long ledgerId) throws BookkeeperException {
        AtomicReference<LedgerMetadata> ledgerMetadata = new AtomicReference<>();
        try {
            FutureUtils.result(getLedgerManager().readLedgerMetadata(ledgerId).whenComplete((metadata, exception) -> {
                if (exception == null) {
                    ledgerMetadata.set(metadata.getValue());
                }
            }));
            return ledgerMetadata.get();
        } catch (BKNoSuchLedgerExistsOnMetadataServerException e) {
            return null;
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
        return metadataCache
                .listLedgers()
                .stream()
                .map(Ledger::getLedgerId)
                .collect(Collectors.toList());

    }

    public List<Long> searchLedgers(String term, String bookie) throws BookkeeperException {
        return metadataCache
                .searchLedgers(term, bookie)
                .stream()
                .map(Ledger::getLedgerId)
                .collect(Collectors.toList());

    }

    public Collection<BookieSocketAddress> getAllBookies() throws BookkeeperException {
        try {
            return bkAdmin.getAllBookies();
        } catch (Throwable e) {
            throw new BookkeeperException(e);
        }
    }

}
