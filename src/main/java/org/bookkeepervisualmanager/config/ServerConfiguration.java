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
package org.bookkeepervisualmanager.config;

/**
 *
 * @author matteo.minardi
 */
public final class ServerConfiguration {

    /**
     * Zookeeper location.
     */
    public static final String PROPERTY_ZOOKEEPER_SERVER = "zookeeper.servers";
    public static final String PROPERTY_ZOOKEEPER_SERVER_DEFAULT = "127.0.0.1:2181";

    /**
     * Comma separated list of Zookeeper servers.
     */
    public static final String PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI = "zookeeper.metadataServiceUri";
    public static final String PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT = "zk+null://127.0.0.1:2181/ledgers";

    /**
     * Zookeeper session timeout.
     */
    public static final String PROPERTY_ZOOKEEPER_SESSION_TIMEOUT = "zookeeper.session.timeout";
    public static final int PROPERTY_ZOOKEEPER_SESSION_TIMEOUT_DEFAULT = 10000;

    /**
     * Zookeeper first connection timeout.
     */
    public static final String PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT = "zookeeper.connection.timeout";
    public static final int PROPERTY_ZOOKEEPER_CONNECTION_TIMEOUT_DEFAULT = 10000;

    /**
     * Zookeeper ledgers location. This is used only to retrive cookies. It must
     * match with zookeeper service metadata uri.
     */
    @Deprecated
    public static final String PROPERTY_BOOKKEEPER_LEDGERS_PATH = "bookkeeper.ledgers.path";
    public static final String PROPERTY_BOOKKEEPER_LEDGERS_PATH_DEFAULT = "/ledgers";

}
