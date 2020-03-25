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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.discover.RegistrationClient;
import org.apache.bookkeeper.discover.ZKRegistrationClient;
import org.apache.bookkeeper.meta.LedgerLayout;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.meta.LedgerMetadataSerDe;
import org.apache.bookkeeper.meta.LedgerUnderreplicationManager;
import org.apache.bookkeeper.meta.exceptions.MetadataException;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.apache.bookkeeper.replication.AuditorElector;
import org.apache.bookkeeper.replication.ReplicationException;
import org.apache.bookkeeper.tools.cli.helpers.CommandHelpers;
import org.apache.zookeeper.KeeperException;
import org.bookkeepervisualmanager.cache.Bookie;
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
    private final Map<String, String> bookkeeperClientConfiguration;
    private volatile ClusterWideConfiguration lastClusterWideConfiguration;
    private volatile AtomicReference<RefreshStatus> refreshStatus = new AtomicReference<>(RefreshStatus.IDLE);

    public BookkeeperManager(ConfigurationStore configStore, MetadataCache metadataCache) throws BookkeeperException {
        try {
            this.refreshThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "bk-visual-manager-cache-refresh");
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler((Thread t1, Throwable e) -> {
                        e.printStackTrace();
                    });
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
                    .setClientConnectTimeoutMillis(zkFirstConnectionTimeout)
                    .setEnableDigestTypeAutodetection(true)
                    .setGetBookieInfoTimeout(1000)
                    .setClientConnectTimeoutMillis(1000);

            LOG.log(Level.INFO, "Starting bookkeeper client with connection string = {0}", zkMetadataServiceUri);
            this.bkClient = BookKeeper.forConfig(conf).build();

            Map<String, String> remainingKeys = new HashMap<>();
            this.conf.getKeys().forEachRemaining(key -> {
                remainingKeys.put(key, String.valueOf(conf.getProperty(key)));
            });
            this.bookkeeperClientConfiguration = remainingKeys;

            LOG.log(Level.INFO, "Starting bookkeeper admin.");
            this.bkAdmin = new BookKeeperAdmin(bkClient);

            this.metadataCache = metadataCache;
            this.lastClusterWideConfiguration = getClusterWideConfiguration();
        } catch (IOException | InterruptedException | BKException | ConfigurationNotValidException t) {
            throw new BookkeeperException(t);
        }
    }

    public static final class RefreshCacheWorkerStatus {

        private final RefreshStatus status;
        private final long lastMetadataCacheRefresh;
        private final Map<String, String> bookkkeeperClientConfiguration;
        private final ClusterWideConfiguration lastClusterWideConfiguration;

        public RefreshCacheWorkerStatus(RefreshStatus status, long lastMetadataCacheRefresh, Map<String, String> bookkkeeperClientConfiguration,
                                        ClusterWideConfiguration lastClusterWideConfiguration) {
            this.status = status;
            this.lastMetadataCacheRefresh = lastMetadataCacheRefresh;
            this.bookkkeeperClientConfiguration = bookkkeeperClientConfiguration;
            this.lastClusterWideConfiguration = lastClusterWideConfiguration;
        }

        public ClusterWideConfiguration getLastClusterWideConfiguration() {
            return lastClusterWideConfiguration;
        }

        public Map<String, String> getBookkkeeperClientConfiguration() {
            return bookkkeeperClientConfiguration;
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
        } else {
            LOG.log(Level.INFO, "Metadata refresh is still in progress");
        }
        return getRefreshWorkerStatus();
    }

    public RefreshCacheWorkerStatus getRefreshWorkerStatus() {
        return new RefreshCacheWorkerStatus(refreshStatus.get(), lastMetadataCacheRefresh, bookkeeperClientConfiguration, lastClusterWideConfiguration);
    }

    public void doRefreshMetadataCache() {
        LOG.info("Refreshing Metadata Cache");
        try {
            lastClusterWideConfiguration = getClusterWideConfiguration();
            final Map<BookieSocketAddress, BookieInfo> bookieInfo = bkClient.getBookieInfo();
            RegistrationClient metadataClient = bkClient.getMetadataClientDriver().getRegistrationClient();
            final Collection<BookieSocketAddress> bookiesCookie = metadataClient.getAllBookies().get().getValue();
            final Collection<BookieSocketAddress> available = metadataClient.getWritableBookies().get().getValue();
            final Collection<BookieSocketAddress> readonly = metadataClient.getReadOnlyBookies().get().getValue();
            LOG.log(Level.INFO, "all Bookies {0}", bookiesCookie);
            LOG.log(Level.INFO, "writable Bookies {0}", available);
            LOG.log(Level.INFO, "readonly Bookies {0}", readonly);
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            List<Bookie> bookiesBefore = metadataCache.listBookies();
            List<String> currentKnownBookiesOnMetadataServer = new ArrayList<>();
            for (BookieSocketAddress bookieAddress : bookiesCookie) {
                LOG.log(Level.INFO, "Discovered Bookie {0}", bookieAddress);
                Bookie b = new Bookie();
                b.setBookieId(bookieAddress.toString());
                b.setDescription(bookieAddress.toString());
                int state;
                if (available.contains(bookieAddress)) {
                    state = Bookie.STATE_AVAILABLE;
                } else if (readonly.contains(bookieAddress)) {
                    state = Bookie.STATE_READONLY;
                } else {
                    state = Bookie.STATE_DOWN;
                }
                LOG.log(Level.INFO, "Discovered Bookie {0} state {1}", new Object[]{bookieAddress, state});
                b.setState(state);
                b.setScanTime(now);
                if (b.getState() != Bookie.STATE_DOWN) {
                    BookieInfo info = bookieInfo.get(bookieAddress);
                    LOG.log(Level.INFO, "Bookie info {0}", info);
                    if (info != null) {
                        b.setFreeDiskspace(info.getFreeDiskSpace());
                        b.setTotalDiskspace(info.getTotalDiskSpace());
                    } else {
                        // bookie did not anwer to getBookieInfo, this is not good
                        state = Bookie.STATE_DOWN;
                        b.setState(state);
                    }
                }
                metadataCache.updateBookie(b);
                currentKnownBookiesOnMetadataServer.add(b.getBookieId());
            }
            for (Bookie b : bookiesBefore) {
                if (!currentKnownBookiesOnMetadataServer.contains(b.getBookieId())) {
                    // bookie decommissioned
                    LOG.log(Level.INFO, "Found decommissioned bookie {0}", b.getBookieId());
                    metadataCache.deleteBookie(b.getBookieId());
                }
            }

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
                Set<String> bookieAddresses = getBookieList(ledgerMetadata);
                bookieAddresses.forEach(s -> {
                    bookies.add(new LedgerBookie(ledgerId, s));
                });
                LOG.log(Level.INFO, "Updating ledeger {0} metadata", ledgerId);
                metadataCache.updateLedger(ledger, bookies, metadataEntries);
            }

            lastMetadataCacheRefresh = System.currentTimeMillis();
            LOG.info("Refreshing Metadata Cache Finished");
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Cannot refresh metadata {}", e);
        } finally {
            refreshStatus.compareAndSet(RefreshStatus.WORKING, RefreshStatus.IDLE);
        }
    }

    private static Set<String> getBookieList(LedgerMetadata ledgerMetadata) {
        Set<String> bookieAddresses = new HashSet<>();
        ledgerMetadata.getAllEnsembles().values().forEach(bookieList -> {
            bookieList.forEach(bookieAddress -> {
                bookieAddresses.add(bookieAddress.toString());
            });
        });
        return bookieAddresses;
    }

    /**
     * Build ensemble map
     *
     * @param ledgerMetadata
     * @return
     */
    public static Map<Long, List<String>> buildEnsembleMap(LedgerMetadata ledgerMetadata) {
        Map<Long, List<String>> res = new LinkedHashMap<>();
        ledgerMetadata.getAllEnsembles().entrySet().
                forEach((e) -> {
                    List<String> bookieAddresses = new ArrayList<>();
                    e.getValue().
                            forEach((bsa) -> {
                                bookieAddresses.add(bsa.toString());
                            });
                    res.put(e.getKey(), bookieAddresses);
                });
        return res;
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

    public Ledger getLedger(long ledgerId) throws BookkeeperException {
        return metadataCache.getLedgerMetadata(ledgerId);
    }

    public LedgerMetadata getLedgerMetadata(Ledger ledger) throws BookkeeperException {
        return convertLedgerMetadata(ledger);
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

    public List<Long> getAllLedgers() throws BookkeeperException {
        return metadataCache
                .listLedgers()
                .stream()
                .map(Ledger::getLedgerId)
                .collect(Collectors.toList());

    }

    public List<Long> searchLedgers(String term,
                                    String bookie,
                                    List<Long> ledgerIds,
                                    Integer minLength,
                                    Integer maxLength,
                                    Integer minAge) throws BookkeeperException {
        return metadataCache
                .searchLedgers(term, bookie, ledgerIds)
                .stream()
                .filter(l -> {
                    if (minLength != null && l.getSize() < minLength) {
                        return false;
                    }
                    if (maxLength != null && l.getSize() > maxLength) {
                        return false;
                    }
                    if (minAge != null && l.getAge() < minAge) {
                        return false;
                    }
                    return true;
                })
                .map(Ledger::getLedgerId)
                .collect(Collectors.toList());

    }

    public Collection<Bookie> getAllBookies() throws BookkeeperException {
        return metadataCache.listBookies();
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public ClusterWideConfiguration getClusterWideConfiguration() throws BookkeeperException {
        LOG.log(Level.INFO, "starting getClusterWideConfiguration");
        try {
            int lostBookieRecoveryDelay = 0;
            BookieSocketAddress auditor = null;
            boolean autoRecoveryEnabled = false;
            int layoutFormatVersion = 0;
            String layoutManagerFactoryClass = "";
            int layoutManagerVersion = 0;
            LedgerLayout ledgerLayout = bkClient.getMetadataClientDriver().getLayoutManager().readLedgerLayout();
            layoutFormatVersion = ledgerLayout.getLayoutFormatVersion();
            layoutManagerFactoryClass = ledgerLayout.getManagerFactoryClass();
            layoutManagerVersion = ledgerLayout.getManagerVersion();
            ZKRegistrationClient metadataClient = (ZKRegistrationClient) bkClient.getMetadataClientDriver().getRegistrationClient();
            try {
                auditor = AuditorElector.getCurrentAuditor(new ServerConfiguration(conf), metadataClient.getZk());
                try (LedgerUnderreplicationManager underreplicationManager = bkClient.getMetadataClientDriver().getLedgerManagerFactory().newLedgerUnderreplicationManager()) {
                    autoRecoveryEnabled = underreplicationManager.isLedgerReplicationEnabled();
                    lostBookieRecoveryDelay = underreplicationManager.getLostBookieRecoveryDelay();
                }
            } catch (ReplicationException.UnavailableException | KeeperException
                    | ReplicationException.CompatibilityException notConfigured) {
                // auto replication stuff never initialized
                LOG.log(Level.INFO, "Cannot get auditor info: {0}", notConfigured);
            }
            String auditorDescription = "";
            if (auditor != null) {
                auditorDescription = CommandHelpers.getBookieSocketAddrStringRepresentation(auditor);
            }

            return new ClusterWideConfiguration(auditorDescription, autoRecoveryEnabled, lostBookieRecoveryDelay,
                    layoutFormatVersion, layoutManagerFactoryClass, layoutManagerVersion);
        } catch (InterruptedException
                | MetadataException | IOException ex) {
            LOG.log(Level.SEVERE, "Error", ex);
            throw new BookkeeperException(ex);
        } finally {
            LOG.log(Level.INFO, "finished getClusterWideConfiguration");
        }
    }

    public static final class ClusterWideConfiguration {

        private final String auditor;
        private final boolean autorecoveryEnabled;
        private final int lostBookieRecoveryDelay;
        private final int layoutFormatVersion;
        private final String layoutManagerFactoryClass;
        private final int layoutManagerVersion;

        public ClusterWideConfiguration(String auditor, boolean autorecoveryEnabled, int lostBookieRecoveryDelay, int layoutFormatVersion, String layoutManagerFactoryClass, int layoutManagerVersion) {
            this.auditor = auditor;
            this.autorecoveryEnabled = autorecoveryEnabled;
            this.lostBookieRecoveryDelay = lostBookieRecoveryDelay;
            this.layoutFormatVersion = layoutFormatVersion;
            this.layoutManagerFactoryClass = layoutManagerFactoryClass;
            this.layoutManagerVersion = layoutManagerVersion;
        }

        public String getAuditor() {
            return auditor;
        }

        public boolean isAutorecoveryEnabled() {
            return autorecoveryEnabled;
        }

        public int getLostBookieRecoveryDelay() {
            return lostBookieRecoveryDelay;
        }

        public int getLayoutFormatVersion() {
            return layoutFormatVersion;
        }

        public String getLayoutManagerFactoryClass() {
            return layoutManagerFactoryClass;
        }

        public int getLayoutManagerVersion() {
            return layoutManagerVersion;
        }

    }

}
