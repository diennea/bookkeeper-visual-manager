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
import org.apache.bookkeeper.client.BookKeeper;

/**
 *
 * @author matteo.minardi
 */
public class BookkeeperClusterPool implements Closeable {

    private Map<String, BookKeeper> pool = new ConcurrentHashMap<>();

    public void addCluster(String name, String metadataServiceUri) {
        BookKeeper bkClient = createBookKeeperClient(metadataServiceUri);
        pool.put(name, bkClient);
    }

    private BookKeeper createBookKeeperClient(String metadataServiceUri) {
        // Establish a connection to metadataServiceUri
        return null;
    }

    public void removeCluster(String name, String metadataServiceUri) throws BookkeeperException {
        BookKeeper bkClient = pool.remove(name);
        releaseBookKeeperClient(bkClient);
    }

    private void releaseBookKeeperClient(BookKeeper bkClient) throws BookkeeperException {
        // close the connection
        try {
            if (bkClient != null) {
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
