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

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.bkvm.config.PropertiesConfigurationStore.PropertiesConfigurationFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class ConfigurationStoreTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private String propertiesFilePath;

    @Before
    public void beforeStartup() throws Exception {
        propertiesFilePath = folder.newFile().getAbsolutePath();
        Properties properties = getTestingProperties();

        try (OutputStream os = new FileOutputStream(new File(propertiesFilePath))) {
            properties.store(os, "");
        }
    }

    @Test
    public void testSystemPropertyConfiguration() throws Exception {
        System.setProperty("bookkeeper.visual.manager.config.path", propertiesFilePath);

        Properties resultProperties = PropertiesConfigurationFactory.buildFromSystemProperty(
                "bookkeeper.visual.manager.config.path");

        Properties expectedProperties = getTestingProperties();
        assertEquals(resultProperties, expectedProperties);

        System.setProperty("bookkeeper.visual.manager.config.path", "");
    }

    @Test
    public void testWebXMLConfiguration() throws Exception {
        final ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(servletContext.getInitParameter("bookkeeper.visual.manager.config.path"))
                .thenReturn(propertiesFilePath);

        Properties resultProperties = PropertiesConfigurationFactory.buildFromWebXML(
                servletContext, "bookkeeper.visual.manager.config.path");

        Properties expectedProperties = getTestingProperties();
        assertEquals(resultProperties, expectedProperties);
    }

    private Properties getTestingProperties() {
        Properties properties = new Properties();
        properties.put(ServerConfiguration.PROPERTY_BOKKEEPER_METADATA_SERVICE_URI_DEFAULT,
                "zk+null://127.0.0.1:2181/ledgers");
        return properties;
    }

}
