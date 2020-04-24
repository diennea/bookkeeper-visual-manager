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

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

/**
 *
 * @author matteo.minardi
 */
public class LocalZookeeperServer implements Closeable {

    CloseableZooKeeperServerMain zooKeeperServer;

    public LocalZookeeperServer(int port, String dataDir) throws IOException {
        QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        try {
            Properties zkProperties = new Properties();
            zkProperties.put("clientPort", port);
            zkProperties.put("dataDir", dataDir);
            quorumConfiguration.parseProperties(zkProperties);
        } catch (IOException | QuorumPeerConfig.ConfigException e) {
            throw new RuntimeException(e);
        }

        zooKeeperServer = new CloseableZooKeeperServerMain();
        final ServerConfig configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);

        new Thread() {
            public void run() {
                try {
                    zooKeeperServer.runFromConfig(configuration);
                } catch (IOException | AdminServer.AdminServerException e) {
                    System.out.println("ZooKeeper Failed");
                    e.printStackTrace(System.err);
                }
            }
        }.start();
    }

    @Override
    public void close() {
        if (zooKeeperServer != null) {
            zooKeeperServer.close();
        }
    }

    private final class CloseableZooKeeperServerMain extends ZooKeeperServerMain implements Closeable {

        @Override
        public void close() {
            try {
                shutdown();
            } catch (Exception t) {
                System.out.println("ZooKeeper failed to close");
                t.printStackTrace(System.err);
            }
        }

    }
}
