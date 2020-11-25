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

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.LedgerManager;
import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author matteo.minardi
 */
@Getter
public class BookkeeperClusterPool implements Closeable {

    private static final Logger LOG = Logger.getLogger(BookkeeperClusterPool.class.getName());

    private Map<Integer, BookkeeperCluster> pool = new ConcurrentHashMap<>();

    @SneakyThrows
    public BookkeeperCluster ensureCluster(int clusterId, String metadataServiceUri, String configuration) {
        return pool.computeIfAbsent(clusterId, id -> {
            ClientConfiguration conf = new ClientConfiguration()
                    .setMetadataServiceUri(metadataServiceUri)
                    .setEnableDigestTypeAutodetection(true)
                    .setGetBookieInfoTimeout(5)
                    .setReadEntryTimeout(5)
                    .setClientConnectTimeoutMillis(1000);

            StringReader reader = new StringReader(configuration);
            try {
                Properties properties = new Properties();
                properties.load(reader);
                for (String p : properties.stringPropertyNames()) {
                    conf.setProperty(p, properties.get(p));
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Wrong configuration passed {0}", configuration);
            }

            LOG.log(Level.INFO, "Creating cluster {0}, at {1}", new Object[]{clusterId, metadataServiceUri});
            BookkeeperCluster bkCluster = new BookkeeperCluster(id, conf);
            return bkCluster;
        });
    }

    public BookkeeperCluster getCluster(int clusterId) {
        return pool.get(clusterId);
    }

    public void removeCluster(int clusterId) throws BookkeeperManagerException {
        BookkeeperCluster bkCluster = pool.remove(clusterId);
        if (bkCluster == null) {
            return;
        }
        releaseBookKeeperCluster(bkCluster);
    }

    @SneakyThrows
    private void releaseBookKeeperCluster(BookkeeperCluster bkCluster) throws BookkeeperManagerException {
        try {
            if (bkCluster.getBkClient() != null) {
                LOG.log(Level.INFO, "Removed bkClient {0}", bkCluster.getBkClient());
                bkCluster.getBkClient().close();
            }
            if (bkCluster.getBkAdmin() != null) {
                bkCluster.getBkAdmin().close();
            }
        } catch (Throwable t) {
            throw new BookkeeperManagerException(t);
        }
    }

    public LedgerManager getLedgerManager(int clusterId) throws IOException, InterruptedException, BKException, ConfigurationException {
        return this.pool.get(clusterId).getBkClient().getLedgerManager();
    }

    @Override
    public void close() throws IOException {
        try {
            for (BookkeeperCluster bkCluster : pool.values()) {
                releaseBookKeeperCluster(bkCluster);
            }
            this.pool = null;
        } catch (BookkeeperManagerException ex) {
            throw new IOException(ex);
        }
    }

    public static class BookkeeperCluster {

        private final int id;
        private BookKeeper bkClient;
        private final ClientConfiguration conf;

        public BookkeeperCluster(int id, ClientConfiguration conf) {
            this.id = id;
            this.conf = conf;
        }

        public int getId() {
            return id;
        }

        public synchronized BookKeeper getBkClient() throws IOException, InterruptedException, BKException, ConfigurationException {
            if (bkClient == null) {
                LOG.log(Level.INFO, "Creating BK client for cluster {0} at {1}", new Object[]{id, conf.getMetadataServiceUri()});
                bkClient = BookKeeper.forConfig(conf).build();
            }
            return bkClient;
        }

        public ClientConfiguration getConf() {
            return conf;
        }

        public BookKeeperAdmin getBkAdmin() {
            return new BookKeeperAdmin(bkClient);
        }

    }

}
