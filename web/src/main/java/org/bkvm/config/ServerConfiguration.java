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
package org.bkvm.config;

/**
 * Configuration entries
 * @author matteo.minardi
 */
public final class ServerConfiguration {

    /**
     * Default service URI for the 'default' cluster, automatically created at first boot if set
     */
    public static final String PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI = "metadataServiceUri";
    public static final String PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT = ""; // "zk+null://127.0.0.1:2181/ledgers"

    /**
     * Automatically refresh metadata
     */
    public static final String PROPERTY_METADATA_REFRESH_PERIOD = "metadata.refreshPeriodSeconds";
    public static final String PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT = "300";

    /**
     * Trigger metadata refresh at boot
     */
    public static final String PROPERTY_METADATA_REFRESH_AT_BOOT = "metadata.refreshAtBoot";
    public static final String PROPERTY_METADATA_REFRESH_AT_BOOT_DEFAULT = "false";

    /**
     * Trigger metadata refresh at boot
     */
    public static final String PROPERTY_BK_METADATA_DESCRIPTION = "metadata.ledgerdescription";
    public static final String PROPERTY_BK_METADATA_DESCRIPTION_DEFAULT = "tablespacename,pulsar/,application";

}
