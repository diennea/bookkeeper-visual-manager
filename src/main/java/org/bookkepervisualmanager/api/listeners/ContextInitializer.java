package org.bookkepervisualmanager.api.listeners;

import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperException;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;
import org.bookkeepervisualmanager.config.ConfigurationNotValidException;
import org.bookkeepervisualmanager.config.ConfigurationStore;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore.PropertiesConfigurationFactory;
import org.bookkeepervisualmanager.config.ServerConfiguration;

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
@WebListener
public class ContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        try {
            ConfigurationStore configStore = buildInitialConfiguation(context);
            context.setAttribute("config", configStore);
            
            BookkeeperManager bookkeeperManager = new BookkeeperManager(configStore);
            context.setAttribute("bookkeeper", bookkeeperManager);
        } catch (Throwable ex) {
            throw new RuntimeException("Unexpected error occurred " + ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        try {
            BookkeeperManager bookkeeperManager = (BookkeeperManager) context.getAttribute("bookkeeper");
            if (bookkeeperManager != null) {
                bookkeeperManager.close();
            }
        } catch (BookkeeperException ex) {
            throw new RuntimeException("Unexpected error occurred " + ex);
        }
    }
    
    /**
     * Creates a {@link ConfigurationStore} following this priority order:
     * <ol>
     *  <li>Simple: System Property bookkeeper.visual.manager.metadataServiceUri</li>
     *  <li>Advanced: System Property bookkeeper.visual.manager.config.path</li>
     *  <li>Advanced: Environment variable BVM_CONF_PATH</li>
     *  <li>Advanced: web.xml property bookkeeper.visual.manager.config.path</li>
     * </ol>
     * @param context The Servlet context
     * @return A {@link ConfigurationStore} containing the Bookkeeper configuration
     * @throws ConfigurationNotValidException 
     */
    public ConfigurationStore buildInitialConfiguation(ServletContext context) throws ConfigurationNotValidException {
        try {
            Properties properties = new Properties();

            String metadataServiceUri = System.getProperty("bookkeeper.visual.manager.metadataServiceUri");
            if (metadataServiceUri != null) {
                properties.put(ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI, metadataServiceUri);

                String msu = metadataServiceUri.split("//")[1];
                properties.put(ServerConfiguration.PROPERTY_ZOOKEEPER_SERVER, msu.split("/")[0]);
                properties.put(ServerConfiguration.PROPERTY_BOOKKEEPER_LEDGERS_PATH, "/" + msu.split("/")[1]);

                return new PropertiesConfigurationStore(properties);
            }

            properties = PropertiesConfigurationFactory.buildFromSystemProperty("bookkeeper.visual.manager.config.path");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }
            
            properties = PropertiesConfigurationFactory.buildFromEnvironmentVariable("BVM_CONF_PATH");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }

            properties = PropertiesConfigurationFactory.buildFromWebXML(context, "bookkeeper.visual.manager.config.path");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }

            if (properties == null) {
                throw new ConfigurationNotValidException("Configuration not provided.");
            }
            
            return new PropertiesConfigurationStore(properties);
        } catch (Throwable t) {
            throw new ConfigurationNotValidException(t);
        }
    }

}
