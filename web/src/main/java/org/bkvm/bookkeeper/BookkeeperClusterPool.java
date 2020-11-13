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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.meta.LedgerManager;

/**
 *
 * @author matteo.minardi
 */
@Getter
public class BookkeeperClusterPool implements Closeable {

    private static final Logger LOG = Logger.getLogger(BookkeeperClusterPool.class.getName());

    private Map<Integer, BookkeeperCluster> pool = new ConcurrentHashMap<>();

    @SneakyThrows
    public BookkeeperCluster ensureCluster(int clusterId, String metadataServiceUri) {
        return pool.computeIfAbsent(clusterId, id -> {
            try {
                LOG.log(Level.INFO, "creating cluster " + clusterId + ", at " + metadataServiceUri);
                BookKeeper bkClient = createBookKeeperClient(metadataServiceUri);
                BookKeeperAdmin bkAdmin = new BookKeeperAdmin(bkClient);
                LOG.log(Level.INFO, "Added bkClient {0}", bkClient.getBookieInfo());
                BookkeeperCluster bkCluster = new BookkeeperCluster(id, bkClient, bkAdmin, bkAdmin.getConf());

                return bkCluster;
            } catch (BKException | IOException ex) {
                throw new RuntimeException(ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
        });
    }

    public BookkeeperCluster getCluster(int clusterId) {
        return pool.get(clusterId);
    }

    private BookKeeper createBookKeeperClient(String metadataServiceUri) throws IOException, InterruptedException, BKException {
            ClientConfiguration conf = new ClientConfiguration()
                    .setMetadataServiceUri(metadataServiceUri)
                    .setEnableDigestTypeAutodetection(true)
                    .setGetBookieInfoTimeout(1000)
                    .setClientConnectTimeoutMillis(1000);
            return BookKeeper.forConfig(conf).build();
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
                LOG.log(Level.INFO, "Removed bkClient {0}", bkCluster.getBkClient().getBookieInfo());
                bkCluster.getBkClient().close();
            }
            if (bkCluster.getBkAdmin() != null) {
                bkCluster.getBkAdmin().close();
            }
        } catch (Throwable t) {
            throw new BookkeeperManagerException(t);
        }
    }

    public LedgerManager getLedgerManager(int clusterId) {
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

    @Data
    public static class BookkeeperCluster {

        private int id;
        private BookKeeper bkClient;
        private BookKeeperAdmin bkAdmin;
        private ClientConfiguration conf;

        public BookkeeperCluster(int id, BookKeeper bkClient, BookKeeperAdmin bkAdmin, ClientConfiguration conf) {
            this.id = id;
            this.bkClient = bkClient;
            this.bkAdmin = bkAdmin;
            this.conf = conf;
        }

    }

}
