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

import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD;
import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.bookkeeper.client.BKException.BKBookieHandleNotAvailableException;
import org.apache.bookkeeper.client.BKException.BKNoSuchLedgerExistsOnMetadataServerException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.common.concurrent.FutureUtils;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.discover.BookieServiceInfo;
import org.apache.bookkeeper.discover.RegistrationClient;
import org.apache.bookkeeper.meta.LedgerLayout;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.bookkeeper.meta.LedgerMetadataSerDe;
import org.apache.bookkeeper.meta.LedgerUnderreplicationManager;
import org.apache.bookkeeper.meta.exceptions.MetadataException;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.replication.ReplicationException;
import org.apache.bookkeeper.tools.cli.helpers.CommandHelpers;
import org.apache.zookeeper.KeeperException;
import org.bkvm.bookkeeper.BookkeeperClusterPool.BookkeeperCluster;
import org.bkvm.cache.Bookie;
import org.bkvm.cache.Cluster;
import org.bkvm.cache.Ledger;
import org.bkvm.cache.LedgerBookie;
import org.bkvm.cache.LedgerMetadataEntry;
import org.bkvm.cache.MetadataCache;
import org.bkvm.config.ConfigurationStore;

/**
 *
 * @author matteo
 */
public class BookkeeperManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperManager.class.getName());

    private final ConfigurationStore configStore;
    private final BookkeeperClusterPool bkClusterPool;

    private final MetadataCache metadataCache;
    private final LedgerMetadataSerDe serDe = new LedgerMetadataSerDe();
    private final ScheduledExecutorService refreshThread;

    public void ensureDefaultCluster(String metadataServiceUri) throws BookkeeperManagerException {
        Cluster exists = metadataCache.listClusters().stream().filter(c -> c.getName().equals("default")).findFirst().orElse(null);
        if (exists != null) {
            LOG.log(Level.INFO, " Default cluster exists: " + exists.getClusterId() + " " + exists.getName() + " at " + exists.getMetadataServiceUri());
            return;
        }
        LOG.log(Level.INFO, " Default cluster does not exist, creating 'default' at " + metadataServiceUri);
        Cluster cluster = new Cluster();
        cluster.setName("default");
        cluster.setMetadataServiceUri(metadataServiceUri);
        cluster.setConfiguration("");
        updateCluster(cluster);
    }

    private Bookie.BookieInfo convertBookieServiceInfo(BookieServiceInfo bookieServiceInfo) {
        Bookie.BookieInfo info = new Bookie.BookieInfo();
        info.setProperties(bookieServiceInfo.getProperties());
        info.setEndpoints(bookieServiceInfo
                .getEndpoints()
                .stream()
                .map(s -> {
                    Bookie.EndpointInfo endpoint = new Bookie.EndpointInfo();
                    endpoint.setAddress(s.getHost() + ":" + s.getPort());
                    endpoint.setProtocol(s.getProtocol());
                    endpoint.setId(s.getId());
                    if (s.getAuth() != null) {
                        endpoint.setAuth(String.join(",", s.getAuth()));
                    } else {
                        endpoint.setAuth("");
                    }
                    return endpoint;
                })
                .collect(Collectors.toList()));
        return info;
    }

    public enum RefreshStatus {
        IDLE,
        WORKING
    }
    private volatile long lastMetadataCacheRefresh;
    private final ConcurrentHashMap<Integer, ClusterWideConfiguration> lastClusterWideConfiguration = new ConcurrentHashMap<>();
    private final AtomicReference<RefreshStatus> refreshStatus = new AtomicReference<>(RefreshStatus.IDLE);

    public BookkeeperManager(ConfigurationStore configStore, MetadataCache metadataCache) throws BookkeeperManagerException {
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
        this.configStore = configStore;
        this.metadataCache = metadataCache;
        this.bkClusterPool = new BookkeeperClusterPool();
    }

    @Getter
    public static final class RefreshCacheWorkerStatus {

        private final RefreshStatus status;
        private final long lastMetadataCacheRefresh;
        private final Map<Integer, ClusterWideConfiguration> lastClusterWideConfiguration;

        public RefreshCacheWorkerStatus(RefreshStatus status, long lastMetadataCacheRefresh,
                Map<Integer, ClusterWideConfiguration> lastClusterWideConfiguration) {
            this.status = status;
            this.lastMetadataCacheRefresh = lastMetadataCacheRefresh;
            this.lastClusterWideConfiguration = lastClusterWideConfiguration;
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
        return new RefreshCacheWorkerStatus(refreshStatus.get(), lastMetadataCacheRefresh, lastClusterWideConfiguration);
    }

    public void doRefreshMetadataCache() {
        LOG.info("Refreshing Metadata Cache");
        try {
            for (Cluster cluster : this.metadataCache.listClusters()) {
                String clusterName = cluster.getName();
                LOG.log(Level.INFO, "Refreshing cluster {0} at {1}", new Object[]{clusterName, cluster.getMetadataServiceUri()});

                BookkeeperCluster bkCluster = this.bkClusterPool.ensureCluster(
                        cluster.getClusterId(),
                        cluster.getMetadataServiceUri(),
                        cluster.getConfiguration()
                );

                int clusterId = bkCluster.getId();
                ClientConfiguration conf = bkCluster.getConf();
                BookKeeper bkClient = bkCluster.getBkClient();
                BookKeeperAdmin bkAdmin = bkCluster.getBkAdmin();

                lastClusterWideConfiguration.put(clusterId, getClusterWideConfiguration(clusterId, cluster.getName(),
                        cluster.getConfiguration(), bkClient, conf, bkAdmin));
                RegistrationClient metadataClient = bkClient.getMetadataClientDriver().getRegistrationClient();
                final Collection<BookieId> bookiesCookie = metadataClient.getAllBookies().get().getValue();
                final Collection<BookieId> available = metadataClient.getWritableBookies().get().getValue();
                final Collection<BookieId> readonly = metadataClient.getReadOnlyBookies().get().getValue();
                LOG.log(Level.INFO, "all Bookies {0}", bookiesCookie);
                LOG.log(Level.INFO, "writable Bookies {0}", available);
                LOG.log(Level.INFO, "readonly Bookies {0}", readonly);
                final Map<BookieId, BookieInfo> bookieInfo;
                if (available.size() + readonly.size() > 0) {
                    // https://github.com/apache/bookkeeper/pull/2498
                    bookieInfo = bkClient.getBookieInfo();
                } else {
                    bookieInfo = Collections.emptyMap();
                }
                java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
                List<Bookie> bookiesBefore = metadataCache.listBookies(clusterId);
                List<String> currentKnownBookiesOnMetadataServer = new ArrayList<>();
                for (BookieId bookieId : bookiesCookie) {
                    LOG.log(Level.INFO, "Discovered Bookie {0}", bookieId);
                    Bookie b = metadataCache.getBookie(clusterId, bookieId.toString());
                    if (b == null) {
                        b = new Bookie();
                    }
                    b.setClusterId(clusterId);
                    b.setBookieId(bookieId.toString());
                    b.setDescription(bookieId.toString());
                    try {
                        BookieServiceInfo bookieServiceInfo = FutureUtils.result(metadataClient.getBookieServiceInfo(bookieId)).getValue();
                        Bookie.BookieInfo info = convertBookieServiceInfo(bookieServiceInfo);
                        b.setBookieInfo(Bookie.formatBookieInfo(info));
                    } catch (BKBookieHandleNotAvailableException err) {
                        LOG.log(Level.INFO, "Bookie " + bookieId + " is not available (" + err + ")");
                        // keep current bookieInfo
                    }
                    int state;
                    if (available.contains(bookieId)) {
                        state = Bookie.STATE_AVAILABLE;
                    } else if (readonly.contains(bookieId)) {
                        state = Bookie.STATE_READONLY;
                    } else {
                        state = Bookie.STATE_DOWN;
                    }
                    LOG.log(Level.INFO, "Discovered Bookie {0} state {1}", new Object[]{bookieId, state});
                    b.setState(state);
                    b.setScanTime(now);
                    if (b.getState() != Bookie.STATE_DOWN) {
                        BookieInfo bookieSpaceInfo = bookieInfo.get(bookieId);
                        LOG.log(Level.INFO, "Bookie info {0}", bookieSpaceInfo);
                        if (bookieSpaceInfo != null) {
                            b.setFreeDiskspace(bookieSpaceInfo.getFreeDiskSpace());
                            b.setTotalDiskspace(bookieSpaceInfo.getTotalDiskSpace());
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
                        metadataCache.deleteBookie(b.getClusterId(), b.getBookieId());
                    }
                }

                Iterable<Long> ledgersIds = bkAdmin.listLedgers();
                for (long ledgerId : ledgersIds) {
                    LedgerMetadata ledgerMetadata = readLedgerMetadata(ledgerId, clusterId);
                    if (ledgerMetadata == null) {
                        // ledger disappeared
                        metadataCache.deleteLedger(clusterId, ledgerId);
                        return;
                    }
                    Ledger ledger = new Ledger(ledgerId, clusterId,
                            ledgerMetadata.getLength(),
                            new java.sql.Timestamp(ledgerMetadata.getCtime()),
                            new java.sql.Timestamp(System.currentTimeMillis()),
                            Base64.getEncoder().encodeToString(serDe.serialize(ledgerMetadata)));
                    List<LedgerMetadataEntry> metadataEntries = new ArrayList<>();
                    ledgerMetadata.getCustomMetadata().forEach((n, v) -> {
                        metadataEntries.add(new LedgerMetadataEntry(ledgerId, clusterId,
                                n, new String(v, StandardCharsets.UTF_8)));
                    });
                    List<LedgerBookie> bookies = new ArrayList<>();
                    Set<String> bookieAddresses = getBookieList(ledgerMetadata);
                    bookieAddresses.forEach(bookieId -> {
                        bookies.add(new LedgerBookie(ledgerId, bookieId, clusterId));
                    });
                    LOG.log(Level.FINE, "Updating ledger {0} metadata", ledgerId);
                    metadataCache.updateLedger(ledger, bookies, metadataEntries);
                }
            }

            lastMetadataCacheRefresh = System.currentTimeMillis();
            LOG.info("Refreshing Metadata Cache Finished");
        } catch (Throwable e) {
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
        } catch (IOException t) {
            LOG.log(Level.SEVERE, "Error closing BKAdmin", t);
        }
    }

    public List<Long> getLedgersForBookie(int clusterId, String bookieId) throws BookkeeperManagerException {
        return metadataCache.getLedgersForBookie(clusterId, bookieId);
    }

    public Ledger getLedger(int clusterId, long ledgerId) throws BookkeeperManagerException {
        return metadataCache.getLedgerMetadata(clusterId, ledgerId);
    }

    public Cluster getCluster(int clusterId) {
        return metadataCache.getCluster(clusterId);
    }

    public LedgerMetadata getLedgerMetadata(Ledger ledger) throws BookkeeperManagerException {
        return convertLedgerMetadata(ledger);
    }

    public LedgerMetadata getLedgerMetadata(int clusterId, long ledgerId) throws BookkeeperManagerException {
        Ledger ledger = metadataCache.getLedgerMetadata(clusterId, ledgerId);
        return convertLedgerMetadata(ledger);
    }

    private LedgerMetadata convertLedgerMetadata(Ledger ledger) throws BookkeeperManagerException {
        try {
            if (ledger == null) {
                return null;
            }
            return serDe.parseConfig(
                    Base64.getDecoder().decode(ledger.getSerializedMetadata()),
                    ledger.getLedgerId(),
                    Optional.of(ledger.getCtime().getTime()));
        } catch (IOException ex) {
            throw new BookkeeperManagerException(ex);
        }
    }

    private LedgerMetadata readLedgerMetadata(long ledgerId, int clusterId) throws BookkeeperManagerException {
        AtomicReference<LedgerMetadata> ledgerMetadata = new AtomicReference<>();
        try {
            LedgerManager lm = this.bkClusterPool.getLedgerManager(clusterId);
            FutureUtils.result(lm.readLedgerMetadata(ledgerId).whenComplete((metadata, exception) -> {
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

    public List<Map.Entry<Integer, Long>> searchLedgers(String term,
            String bookieId,
            Integer clusterId,
            List<Long> ledgerIds,
            Integer minLength,
            Integer maxLength,
            Integer minAge) throws BookkeeperManagerException {
        return metadataCache
                .searchLedgers(term, bookieId, clusterId, ledgerIds)
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
                .map(l -> new AbstractMap.SimpleImmutableEntry<>(l.getClusterId(), l.getLedgerId()))
                .collect(Collectors.toList());

    }

    public Collection<Bookie> getAllBookies() throws BookkeeperManagerException {
        return metadataCache.listBookies();
    }

    public String getHttpServerEndpoint(int clusterId, String bookieId) {
        Bookie bookie = metadataCache.getBookie(clusterId, bookieId);
        Bookie.BookieInfo parsedBookieInfo = Bookie.parseBookieInfo(bookie.getBookieInfo());
        String httpServerUri = null;
        for (Bookie.EndpointInfo info : parsedBookieInfo.getEndpoints()) {
            if (info.getId().equals("httpserver")) {
                httpServerUri = info.getProtocol() + "://" + info.getAddress();
                break;
            }
        }
        return httpServerUri;
    }

    public Collection<Cluster> getAllClusters() throws BookkeeperManagerException {
        return metadataCache.listClusters();
    }

    public void updateCluster(Cluster cluster) throws BookkeeperManagerException {
        metadataCache.updateCluster(cluster);
        bkClusterPool.ensureCluster(cluster.getClusterId(), cluster.getMetadataServiceUri(), cluster.getConfiguration());
    }

    public void deleteCluster(int clusterId) throws BookkeeperManagerException {
        metadataCache.deleteCluster(clusterId);
        bkClusterPool.removeCluster(clusterId);
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static ClusterWideConfiguration getClusterWideConfiguration(int clusterId, String clusterName,
                                                                        String configString,
                                                                        BookKeeper bookkeeper,
                                                                        ClientConfiguration conf,
                                                                        BookKeeperAdmin admin) throws BookkeeperManagerException {
        LOG.log(Level.INFO, "starting getClusterWideConfiguration");
        try {
            int lostBookieRecoveryDelay = 0;
            BookieId auditor = null;
            boolean autoRecoveryEnabled = false;
            int layoutFormatVersion = 0;
            String layoutManagerFactoryClass = "";
            int layoutManagerVersion = 0;
            LedgerLayout ledgerLayout = bookkeeper.getMetadataClientDriver().getLayoutManager().readLedgerLayout();
            layoutFormatVersion = ledgerLayout.getLayoutFormatVersion();
            layoutManagerFactoryClass = ledgerLayout.getManagerFactoryClass();
            layoutManagerVersion = ledgerLayout.getManagerVersion();
            try {
                auditor = admin.getCurrentAuditor();
            } catch (IOException ioException) {
                if (ioException.getCause() instanceof KeeperException) {
                    LOG.log(Level.INFO, "Cannot get auditor info: {0}", ioException + ""); // do not write stacktrace
                } else {
                    throw ioException;
                }
            }

            try {

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
                auditorDescription = CommandHelpers.getBookieSocketAddrStringRepresentation(auditor, bookkeeper.getBookieAddressResolver());
            }

            return new ClusterWideConfiguration(clusterId, clusterName, configString, auditorDescription, autoRecoveryEnabled, lostBookieRecoveryDelay,
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

        private final int clusterId;
        private final String clusterName;
        private final String auditor;
        private final boolean autorecoveryEnabled;
        private final int lostBookieRecoveryDelay;
        private final int layoutFormatVersion;
        private final String layoutManagerFactoryClass;
        private final int layoutManagerVersion;
        private final String configuration;

        public ClusterWideConfiguration(int clusterId, String clusterName, String configuration, String auditor, boolean autorecoveryEnabled, int lostBookieRecoveryDelay, int layoutFormatVersion,
                String layoutManagerFactoryClass, int layoutManagerVersion) {
            this.clusterId = clusterId;
            this.clusterName = clusterName;
            this.configuration = configuration;
            this.auditor = auditor;
            this.autorecoveryEnabled = autorecoveryEnabled;
            this.lostBookieRecoveryDelay = lostBookieRecoveryDelay;
            this.layoutFormatVersion = layoutFormatVersion;
            this.layoutManagerFactoryClass = layoutManagerFactoryClass;
            this.layoutManagerVersion = layoutManagerVersion;
        }

        public String getClusterName() {
            return clusterName;
        }

        public String getConfiguration() {
            return configuration;
        }

        public int getClusterId() {
            return clusterId;
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
