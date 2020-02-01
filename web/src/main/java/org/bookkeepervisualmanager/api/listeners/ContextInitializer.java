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
package org.bookkeepervisualmanager.api.listeners;

import herddb.jdbc.HerdDBEmbeddedDataSource;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.bookkeepervisualmanager.auth.AuthManager;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;
import org.bookkeepervisualmanager.cache.MetadataCache;
import org.bookkeepervisualmanager.config.ConfigurationNotValidException;
import org.bookkeepervisualmanager.config.ConfigurationStore;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore.PropertiesConfigurationFactory;
import org.bookkeepervisualmanager.config.ServerConfiguration;

@WebListener
public class ContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.log("starting");

        // force register calcite driver
        new org.apache.calcite.jdbc.Driver();

        try {
            ConfigurationStore configStore = buildInitialConfiguration(context);
            context.setAttribute("config", configStore);

            HerdDBEmbeddedDataSource datasource = new HerdDBEmbeddedDataSource();
            // In the future we will make this configurable
            String jdbcUrl = configStore.getProperty("jdbc.url", "jdbc:herddb:local");
            datasource.setUrl(jdbcUrl);

            context.setAttribute("datasource", datasource);

            AuthManager authManager = new AuthManager(configStore);
            context.setAttribute("authManager", authManager);

            MetadataCache metadataCache = new MetadataCache(datasource);

            context.setAttribute("metadataCache", metadataCache);
            boolean startEmbeddedDatabase = Boolean.parseBoolean(configStore.getProperty("jdbc.startDatabase", "true"));
            if (startEmbeddedDatabase) {
                // boot the server
                datasource.setStartServer(true);
                datasource.getConnection().close();
            }

            BookkeeperManager bookkeeperManager = new BookkeeperManager(configStore, metadataCache);
            context.setAttribute("bookkeeper", bookkeeperManager);

            // launch reload in background
            bookkeeperManager.refreshMetadataCache();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException("Unexpected error occurred " + ex, ex);
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
        } catch (Exception ex) {
            ex.printStackTrace();
            context.log("An error occurred while closing the application", ex);
        }
        try {
            MetadataCache metadataCache = (MetadataCache) context.getAttribute("metadataCache");
            if (metadataCache != null) {
                metadataCache.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            context.log("An error occurred while closing the application", ex);
        }

        try {
            HerdDBEmbeddedDataSource datasource = (HerdDBEmbeddedDataSource) context.getAttribute("datasource");
            if (datasource != null) {
                datasource.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            context.log("An error occurred while closing the application", ex);
        }
    }

    /**
     * Creates a {@link ConfigurationStore} following this priority order:
     * <ol>
     * <li>Simple: System Property
     * bookkeeper.visual.manager.metadataServiceUri</li>
     * <li>Advanced: System Property bookkeeper.visual.manager.config.path</li>
     * <li>Advanced: Environment variable BVM_CONF_PATH</li>
     * <li>Advanced: web.xml property bookkeeper.visual.manager.config.path</li>
     * </ol>
     *
     * @param context The Servlet context
     * @return A {@link ConfigurationStore} containing the Bookkeeper
     * configuration
     * @throws ConfigurationNotValidException
     */
    public ConfigurationStore buildInitialConfiguration(ServletContext context) throws ConfigurationNotValidException {
        try {
            Properties properties = new Properties();

            String metadataServiceUri = System.getProperty("bookkeeper.visual.manager.metadataServiceUri");
            if (metadataServiceUri != null) {
                properties.put(ServerConfiguration.PROPERTY_BOOKKEEPER_METADATA_SERVICE_URI, metadataServiceUri);
                return new PropertiesConfigurationStore(properties);
            }

            properties = PropertiesConfigurationFactory.buildFromSystemProperty(
                    "bookkeeper.visual.manager.config.path");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }

            properties = PropertiesConfigurationFactory.buildFromEnvironmentVariable("BVM_CONF_PATH");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }

            properties = PropertiesConfigurationFactory.buildFromWebXML(context,
                    "bookkeeper.visual.manager.config.path");
            if (properties != null) {
                return new PropertiesConfigurationStore(properties);
            }

            return new PropertiesConfigurationStore(new Properties());
        } catch (IOException t) {
            throw new ConfigurationNotValidException(t);
        }
    }

}
