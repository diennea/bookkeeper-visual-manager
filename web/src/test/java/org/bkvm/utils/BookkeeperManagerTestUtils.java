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
package org.bkvm.utils;

import herddb.jdbc.HerdDBEmbeddedDataSource;
import java.util.Properties;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.bookkeeper.BookkeeperManagerException;
import org.bkvm.cache.Cluster;
import org.bkvm.cache.MetadataCache;
import org.bkvm.config.ConfigurationStore;
import org.bkvm.config.PropertiesConfigurationStore;
import org.junit.After;
import org.junit.Before;

/**
 * Testing class that provides before each test a {@link BookkeeperManager}
 * connection with an active Zookeeper and Bookkeeper Bookie.
 *
 * @author matteo.minardi
 */
public class BookkeeperManagerTestUtils extends AbstractBookkeeperTestUtils {

    static {
        System.setProperty("herddb.network.sendstacktraces", "false");
    }

    private HerdDBEmbeddedDataSource datasource;
    private MetadataCache metadataCache;
    private BookkeeperManager bookkeeperManager;

    @Before
    public void beforeSetup() throws Exception {
        startZookeeper();
        startBookie();

        datasource = new HerdDBEmbeddedDataSource();
        datasource.setUrl("jdbc:herddb:local");
        metadataCache = new MetadataCache(datasource);
        ConfigurationStore config = new PropertiesConfigurationStore(new Properties());
        bookkeeperManager = new BookkeeperManager(config, metadataCache);
        init();
    }

    protected void init() throws Exception {
        createCluster("cluster1", getMetadataServiceUri(), "");
    }

    protected String getMetadataServiceUri() {
        return "zk+null://" + getZooKeeperAddress() + "/ledgers";
    }

    protected Cluster createCluster(String name, String metadataServiceUri, String configuration) throws BookkeeperManagerException {
        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setMetadataServiceUri(metadataServiceUri);
        cluster.setConfiguration(configuration);
        bookkeeperManager.updateCluster(cluster);
        return cluster;
    }

    @After
    public void afterTeardown() throws Exception {
        if (bookkeeperManager != null) {
            bookkeeperManager.close();
        }
        if (metadataCache != null) {
            metadataCache.close();
        }
        if (datasource != null) {
            datasource.close();
        }

        if (zkServer != null) {
            zkServer.close();
        }
    }

    public BookkeeperManager getBookkeeperManager() {
        return bookkeeperManager;
    }

}
