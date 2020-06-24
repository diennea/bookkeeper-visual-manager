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
import lombok.SneakyThrows;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.conf.ClientConfiguration;

/**
 *
 * @author matteo.minardi
 */
public class BookkeeperClusterPool implements Closeable {

    private static final Logger LOG = Logger.getLogger(BookkeeperClusterPool.class.getName());

    private Map<Integer, BookKeeper> pool = new ConcurrentHashMap<>();

    @SneakyThrows
    public void addCluster(int clusterId, String metadataServiceUri) {
        BookKeeper bkClient = createBookKeeperClient(metadataServiceUri);
        LOG.log(Level.INFO, "Added bkClient {0}", bkClient.getBookieInfo());

        pool.put(clusterId, bkClient);
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

    public void removeCluster(int clusterId) throws BookkeeperException {
        BookKeeper bkClient = pool.remove(clusterId);
        releaseBookKeeperClient(bkClient);
    }

    @SneakyThrows
    private void releaseBookKeeperClient(BookKeeper bkClient) throws BookkeeperException {
        try {
            if (bkClient != null) {
                LOG.log(Level.INFO, "Removed bkClient {0}", bkClient.getBookieInfo());
                bkClient.close();
            }
        } catch (Throwable t) {
            throw new BookkeeperException(t);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            for (BookKeeper bkClient : pool.values()) {
                releaseBookKeeperClient(bkClient);
            }
            this.pool = null;
        } catch (BookkeeperException ex) {
            throw new IOException(ex);
        }
    }

}
