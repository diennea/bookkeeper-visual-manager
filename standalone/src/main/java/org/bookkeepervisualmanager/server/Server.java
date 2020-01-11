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
package org.bookkeepervisualmanager.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    Path baseDirectory;

    public Server(ServerConfiguration configuration) {
        this.configuration = configuration;
        this.baseDirectory = Paths.get(configuration.getString(ServerConfiguration.PROPERTY_BASEDIR,
                ServerConfiguration.PROPERTY_BASEDIR_DEFAULT)).toAbsolutePath();
    }

    public void start() throws Exception {

        boolean startBookie = configuration.getBoolean(ServerConfiguration.PROPERTY_BOOKKEEPER_START,
                ServerConfiguration.PROPERTY_BOOKKEEPER_START_DEFAULT);

        if (startBookie && embeddedBookie == null) {
            try {
                Files.createDirectories(this.baseDirectory);
            } catch (IOException ignore) {
                LOGGER.log(Level.SEVERE, "Cannot create baseDirectory " + this.baseDirectory, ignore);
            }
            this.embeddedBookie = new EmbeddedBookie(baseDirectory, configuration);
            this.embeddedBookie.start();
        }

    }

    public void close() {

        if (this.embeddedBookie != null) {
            this.embeddedBookie.close();
        }
    }

}
