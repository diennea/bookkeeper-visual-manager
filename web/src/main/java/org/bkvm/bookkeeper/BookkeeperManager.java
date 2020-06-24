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
package org.bkvm.bookkeeper;

import static org.bkvm.config.ServerConfiguration.PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT;
import static org.bkvm.config.ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI;
import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD;
import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT;
import static org.bkvm.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT;
import static org.bkvm.config.ServerConfiguration.PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT;
import static org.bkvm.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT;
import static org.bkvm.config.ServerConfiguration.PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
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
import org.bkvm.cache.Bookie;
import org.bkvm.cache.Cluster;
import org.bkvm.cache.Ledger;
import org.bkvm.cache.LedgerBookie;
import org.bkvm.cache.LedgerMetadataEntry;
import org.bkvm.cache.MetadataCache;
import org.bkvm.config.ConfigurationNotValidException;
import org.bkvm.config.ConfigurationStore;
import org.bkvm.config.ConfigurationStoreUtils;

/**
 *
 * @author matteo
 */
public class BookkeeperManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperManager.class.getName());

    private final ConfigurationStore configStore;
    private final ClientConfiguration conf;

    @Deprecated
    private final BookKeeper bkClient;
    private final BookkeeperClusterPool bkClusterPool;

    private final BookKeeperAdmin bkAdmin;
    private final MetadataCache metadataCache;
    private final LedgerMetadataSerDe serDe = new LedgerMetadataSerDe();
    private final ScheduledExecutorService refreshThread;

    public enum RefreshStatus {
        IDLE,
        WORKING
    }
    private volatile long lastMetadataCacheRefresh;
    private final Map<String, String> bookkeeperClientConfiguration;
    private volatile ClusterWideConfiguration lastClusterWideConfiguration;
    private final AtomicReference<RefreshStatus> refreshStatus = new AtomicReference<>(RefreshStatus.IDLE);

    public BookkeeperManager(ConfigurationStore configStore, MetadataCache metadataCache) throws BookkeeperManagerException {
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
                    .setClientConnectTimeoutMillis(zkFirstConnectionTimeout)
                    .setEnableDigestTypeAutodetection(true)
                    .setGetBookieInfoTimeout(1000)
                    .setClientConnectTimeoutMillis(1000);

            LOG.log(Level.INFO, "Bookkeeper client connection string = {0}", zkMetadataServiceUri);
            this.bkClient = BookKeeper.forConfig(conf).build();

            // inizialize the = new BookkeeperClusterPool()
            this.bkClusterPool = new BookkeeperClusterPool();

            Map<String, String> remainingKeys = new HashMap<>();
            this.conf.getKeys().forEachRemaining(key -> {
                remainingKeys.put(key, String.valueOf(conf.getProperty(key)));
            });
            this.bookkeeperClientConfiguration = remainingKeys;
            this.metadataCache = metadataCache;
            this.lastClusterWideConfiguration = ClusterWideConfiguration.UNKNOWN;
            int refreshSeconds = Integer.parseInt(configStore.getProperty(PROPERTY_METADATA_REFRESH_PERIOD, PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT));
        this.refreshThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
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
        if (refreshSeconds > 0) {
            LOG.log(Level.INFO, "Scheduling automatic refresh of metadata, every {0} seconds", refreshSeconds);
            refreshThread.scheduleWithFixedDelay(() -> {
                refreshMetadataCache();
            }, refreshSeconds, refreshSeconds, TimeUnit.SECONDS);
        }
        } catch (ConfigurationNotValidException t) {
            throw new BookkeeperManagerException(t);
        }
    }

    private synchronized BookKeeper getBookKeeperClient() throws BookkeeperManagerException {
        if (bkClient == null) {
            try {
                bkClient = BookKeeper.forConfig(conf).build();
            } catch (IOException | InterruptedException | BKException ex) {
                throw new BookkeeperManagerException(ex);
            }
        }
        return bkClient;
    }
    private synchronized BookKeeperAdmin getBookKeeperAdmin() throws BookkeeperManagerException {
        if (bkAdmin == null) {
            bkAdmin = new BookKeeperAdmin(getBookKeeperClient());
        }
        return bkAdmin;
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

    public ConfigurationStore getConfigStore() {
        return configStore;
    }

    public RefreshCacheWorkerStatus refreshMetadataCache() {
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
            BookKeeper bookkeeper = getBookKeeperClient();
            BookKeeperAdmin bookkeeperAdmin = getBookKeeperAdmin();
            lastClusterWideConfiguration = getClusterWideConfiguration(bookkeeper);
            final Map<BookieSocketAddress, BookieInfo> bookieInfo = bookkeeper.getBookieInfo();
            RegistrationClient metadataClient = bookkeeper.getMetadataClientDriver().getRegistrationClient();
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

            Iterable<Long> ledgersIds = bookkeeperAdmin.listLedgers();
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
    public synchronized void close() throws BookkeeperManagerException {
        if (refreshThread != null) {
            refreshThread.shutdown();
        }
        try {
            // Close the cluster pool
            if (bkClusterPool != null) {
                LOG.log(Level.INFO, "Closing bookkeeper cluster pool");
                bkClusterPool.close();
            }
            if (bkClient != null) {
                LOG.log(Level.INFO, "Closing bookkeeper connection");
                bkClient.close();
            }
            if (bkAdmin != null) {
                LOG.log(Level.INFO, "Closing bookkeeper admin connection");
                bkAdmin.close();
            }
        } catch (InterruptedException | BKException t) {
            LOG.log(Level.SEVERE, "Error closing BKAdmin", t);
        }
    }

    public LedgerManager getLedgerManager() throws BookkeeperManagerException {
        return getBookKeeperClient().getLedgerManager();
    }

    public List<Long> getLedgersForBookie(String bookieId) throws BookkeeperManagerException {
        return metadataCache.getLedgersForBookie(bookieId);
    }

    public Ledger getLedger(long ledgerId) throws BookkeeperManagerException {
        return metadataCache.getLedgerMetadata(ledgerId);
    }

    public LedgerMetadata getLedgerMetadata(Ledger ledger) throws BookkeeperManagerException {
        return convertLedgerMetadata(ledger);
    }

    public LedgerMetadata getLedgerMetadata(long ledgerId) throws BookkeeperManagerException {
        Ledger ledger = metadataCache.getLedgerMetadata(ledgerId);
        return convertLedgerMetadata(ledger);
    }

    private LedgerMetadata convertLedgerMetadata(Ledger ledger) throws BookkeeperManagerException {
        try {
            if (ledger == null) {
                return null;
            }
            return serDe.parseConfig(
                    Base64.getDecoder().decode(ledger.getSerializedMetadata()),
                    Optional.of(ledger.getCtime().getTime()));
        } catch (IOException ex) {
            throw new BookkeeperManagerException(ex);
        }
    }

    private LedgerMetadata readLedgerMetadata(long ledgerId) throws BookkeeperManagerException {
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
            throw new BookkeeperManagerException(e);
        }
    }

    public List<Long> getAllLedgers() throws BookkeeperManagerException {
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
                                    Integer minAge) throws BookkeeperManagerException {
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

    public Collection<Bookie> getAllBookies() throws BookkeeperManagerException {
        return metadataCache.listBookies();
    }

    public Collection<Cluster> getAllClusters() throws BookkeeperException {
        return metadataCache.listClusters();
    }

    public void updateCluster(Cluster cluster) throws BookkeeperException {
        metadataCache.updateCluster(cluster);
    }

    public void deleteCluster(int clusterId) throws BookkeeperException {
        metadataCache.deleteCluster(clusterId);
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private ClusterWideConfiguration getClusterWideConfiguration(BookKeeper bookkeeper) throws BookkeeperManagerException {
        LOG.log(Level.INFO, "starting getClusterWideConfiguration");
        try {
            int lostBookieRecoveryDelay = 0;
            BookieSocketAddress auditor = null;
            boolean autoRecoveryEnabled = false;
            int layoutFormatVersion = 0;
            String layoutManagerFactoryClass = "";
            int layoutManagerVersion = 0;
            LedgerLayout ledgerLayout = bookkeeper.getMetadataClientDriver().getLayoutManager().readLedgerLayout();
            layoutFormatVersion = ledgerLayout.getLayoutFormatVersion();
            layoutManagerFactoryClass = ledgerLayout.getManagerFactoryClass();
            layoutManagerVersion = ledgerLayout.getManagerVersion();
            ZKRegistrationClient metadataClient = (ZKRegistrationClient) bookkeeper.getMetadataClientDriver().getRegistrationClient();
            try {
                auditor = AuditorElector.getCurrentAuditor(new ServerConfiguration(conf), metadataClient.getZk());
                try (LedgerUnderreplicationManager underreplicationManager = bookkeeper.getMetadataClientDriver().getLedgerManagerFactory().newLedgerUnderreplicationManager()) {
                    autoRecoveryEnabled = underreplicationManager.isLedgerReplicationEnabled();
                    lostBookieRecoveryDelay = underreplicationManager.getLostBookieRecoveryDelay();
                }
            } catch (ReplicationException.UnavailableException | KeeperException
                    | ReplicationException.CompatibilityException notConfigured) {
                // auto replication stuff never initialized
                LOG.log(Level.INFO, "Cannot get auditor info: {0}", notConfigured + ""); // do not write stacktrace
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
            throw new BookkeeperManagerException(ex);
        } finally {
            LOG.log(Level.INFO, "finished getClusterWideConfiguration");
        }
    }

    public static final class ClusterWideConfiguration {

        private static final ClusterWideConfiguration UNKNOWN = new ClusterWideConfiguration("?", false, -1, -1, "", -1);

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
