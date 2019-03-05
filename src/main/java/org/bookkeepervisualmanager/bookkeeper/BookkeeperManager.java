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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookieInfoReader;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.net.BookieSocketAddress;

/**
 *
 * @author matteo
 */
public class BookkeeperManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperManager.class.getName());

    public static final String ZK_SERVER = System.getProperty("zk.servers", "127.0.0.1:2181");

    private final ClientConfiguration conf;
    private BookKeeper client;

    public BookkeeperManager(String servers) throws BookkeeperException {
        this.conf = new ClientConfiguration();
        this.conf.setMetadataServiceUri("zk+null://" + servers + "/ledgers");
        
        try {
            LOG.log(Level.INFO, "Starting bookkeeper connection with connection string = {0}", conf.getMetadataServiceUri());
            this.client = new BookKeeper(conf);
        } catch (Throwable t) {
            throw new BookkeeperException(t);
        }
    }

    @Override
    public void close() throws BookkeeperException {
        if (client != null) {
            try {
                LOG.log(Level.INFO, "Closing bookkeeper connection");
                client.close();
            } catch (Throwable t) {
                throw new BookkeeperException(t);
            } finally {
                client = null;
            }
        }
    }

    public Map<BookieSocketAddress, BookieInfoReader.BookieInfo> getBookieInfo() throws Exception {
        return client.getBookieInfo();
    }

}
