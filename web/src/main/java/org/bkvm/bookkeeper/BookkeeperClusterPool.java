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
    public void addCluster(int clusterId, String metadataServiceUri) {
        BookKeeper bkClient = createBookKeeperClient(metadataServiceUri);
        BookKeeperAdmin bkAdmin = new BookKeeperAdmin(bkClient);
        LOG.log(Level.INFO, "Added bkClient {0}", bkClient.getBookieInfo());

        BookkeeperCluster bkCluster = new BookkeeperCluster(bkClient, bkAdmin, bkAdmin.getConf());
        pool.put(clusterId, bkCluster);
    }

    public BookkeeperCluster getCluster(int clusterId) {
        return pool.get(clusterId);
    }

    private BookKeeper createBookKeeperClient(String metadataServiceUri) {
        try {
            ClientConfiguration conf = new ClientConfiguration()
                    .setMetadataServiceUri(metadataServiceUri)
                    .setEnableDigestTypeAutodetection(true)
                    .setGetBookieInfoTimeout(1000)
                    .setClientConnectTimeoutMillis(1000);
            return BookKeeper.forConfig(conf).build();
        } catch (IOException | InterruptedException | BKException ex) {
            return null;
        }
    }

    public void removeCluster(int clusterId) throws BookkeeperManagerException {
        BookkeeperCluster bkCluster = pool.remove(clusterId);
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

        private BookKeeper bkClient;
        private BookKeeperAdmin bkAdmin;
        private ClientConfiguration conf;

        public BookkeeperCluster(BookKeeper bkClient, BookKeeperAdmin bkAdmin, ClientConfiguration conf) {
            this.bkClient = bkClient;
            this.bkAdmin = bkAdmin;
            this.conf = conf;
        }

    }

}
