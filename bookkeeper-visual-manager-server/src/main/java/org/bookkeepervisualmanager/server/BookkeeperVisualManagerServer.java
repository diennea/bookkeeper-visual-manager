package org.bookkeepervisualmanager.server;

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
import java.io.File;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.DispatcherType;
import org.bookkeepervisualmanager.api.ApplicationConfig;
import org.bookkeepervisualmanager.api.ForceHeadersAPIRequestsFilter;
import org.bookkeepervisualmanager.configuration.Configuration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;
import static org.glassfish.jersey.servlet.ServletProperties.JAXRS_APPLICATION_CLASS;

public class BookkeeperVisualManagerServer implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(BookkeeperVisualManagerServer.class.getName());

    private static final String API = "/api";
    private static final String UI = "/ui";

    private final File basePath;
    private volatile boolean started;

    private Server server;
    private int serverPort = 8001;
    private String serverHost = "localhost";

    public BookkeeperVisualManagerServer(File basePath) throws Exception {
        this.basePath = basePath;
    }

    public void startServer() throws Exception {
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        server = new Server(new InetSocketAddress(serverHost, serverPort));
        server.setHandler(contexts);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.GZIP);
        context.setContextPath("/");

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer());
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter(JAXRS_APPLICATION_CLASS, ApplicationConfig.class.getCanonicalName());
        context.addFilter(ForceHeadersAPIRequestsFilter.class, API + "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(jerseyServlet, API + "/*");

        context.setAttribute("server", this);
        contexts.addHandler(context);

        File webUi = new File(basePath, "web");
        if (webUi.isDirectory()) {
            WebAppContext webApp = new WebAppContext(webUi.getAbsolutePath(), UI);
            contexts.addHandler(webApp);
        } else {
            LOG.log(Level.SEVERE, "Cannot find {0} directory. Web UI will not be deployed", webUi.getAbsolutePath());
        }

        server.start();

        String apiUrl = "http://" + serverHost + ":" + serverPort + API;
        String uiUrl = "http://" + serverHost + ":" + serverPort + UI;
        LOG.log(Level.INFO, "Base Server/API url: {0}", apiUrl);
        LOG.log(Level.INFO, "Base Server UI url: {0}", uiUrl);

    }

    public void configureAtBoot(Configuration properties) throws NumberFormatException {
        if (started) {
            throw new IllegalStateException("server already started");
        }

        serverPort = Integer.parseInt(properties.getProperty("server.port", serverPort + ""));
        serverHost = properties.getProperty("server.host", serverHost);

        LOG.log(Level.INFO, "server.port={0}", serverPort);
        LOG.log(Level.INFO, "server.host={0}", serverHost);

    }

    public void start() throws InterruptedException {
        try {
            started = true;
        } catch (RuntimeException err) {
            close();
            throw err;
        }
    }

    @Override
    public void close() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception err) {
                LOG.log(Level.SEVERE, "Error while stopping admin server", err);
            } finally {
                server = null;
            }
        }
    }

}
