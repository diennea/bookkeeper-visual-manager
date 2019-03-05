package org.bookkepervisualmanager.api.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperException;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager;
import static org.bookkeepervisualmanager.bookkeeper.BookkeeperManager.ZK_SERVER;

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
            BookkeeperManager bookkeeperManager = new BookkeeperManager(ZK_SERVER);
            context.setAttribute("bookkeeper", bookkeeperManager);
        } catch (BookkeeperException ex) {
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

}
