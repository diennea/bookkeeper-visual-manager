package org.bookkepervisualmanager.api.listeners;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperException;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;
import org.bookkeepervisualmanager.config.ConfigurationStore;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore;
import static org.bookkeepervisualmanager.config.PropertiesConfigurationStore.ENV_RESOLVER;
import org.bookkeepervisualmanager.config.PropertiesConfigurationStore.PropertyConfigurationResolver;
import static org.bookkeepervisualmanager.config.PropertiesConfigurationStore.SYSPROP_RESOLVER;
import static org.bookkeepervisualmanager.config.PropertiesConfigurationStore.WEBXML_RESOLVER;

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

    public ConfigurationStore buildInitialConfiguation(ServletContext context) throws IOException {
        PropertyConfigurationResolver resolver;

        resolver = SYSPROP_RESOLVER("bookkeeper.visual.manager.config.path");
        if (resolver.resolve() != null) {
            return new PropertiesConfigurationStore(resolver);
        }

        resolver = ENV_RESOLVER("BVM_CONF_PATH");
        if (resolver.resolve() != null) {
            return new PropertiesConfigurationStore(resolver);
        }

        resolver = WEBXML_RESOLVER(context, "bookkeeper.visual.manager.config.path");
        if (resolver.resolve() != null) {
            return new PropertiesConfigurationStore(resolver);
        }

        return new PropertiesConfigurationStore(() -> new Properties());
    }

}
