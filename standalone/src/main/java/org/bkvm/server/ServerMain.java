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
package org.bkvm.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.bkvm.daemons.PidFileLocker;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerMain implements AutoCloseable {

    private static ServerMain runningInstance;
    private static final Logger LOG = Logger.getLogger(ServerMain.class.getName());
    private static final CountDownLatch running = new CountDownLatch(1);

    public static void main(String... args) {
        try {
            LOG.info("Starting BookKeeper Visual Manager");
            Properties configuration = new Properties();

            boolean configFileFromParameter = false;
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (!arg.startsWith("-")) {
                    File configFile = new File(args[i]).getAbsoluteFile();
                    LOG.severe("Reading configuration from " + configFile);
                    System.setProperty("bookkeeper.visual.manager.config.path", configFile.getAbsolutePath());
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile),
                            StandardCharsets.UTF_8)) {
                        configuration.load(reader);
                    }
                    configFileFromParameter = true;
                }
            }
            if (!configFileFromParameter) {
                File configFile = new File("conf/server.properties").getAbsoluteFile();
                System.out.println("Reading configuration from " + configFile);
                System.setProperty("bookkeeper.visual.manager.config.path", configFile.getAbsolutePath());
                if (configFile.isFile()) {
                    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile),
                            StandardCharsets.UTF_8)) {
                        configuration.load(reader);
                    }
                }
            }

            LogManager.getLogManager().readConfiguration();

            Runtime.getRuntime().addShutdownHook(new Thread("ctrlc-hook") {

                @Override
                public void run() {
                    System.out.println("Ctrl-C trapped. Shutting down");
                    ServerMain _brokerMain = runningInstance;
                    if (_brokerMain != null) {
                        _brokerMain.close();
                    }
                }

            });
            runningInstance = new ServerMain(configuration);
            runningInstance.start();

            runningInstance.join();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static ServerMain getRunningInstance() {
        return runningInstance;
    }

    private final Properties configuration;
    private final PidFileLocker pidFileLocker;
    private org.eclipse.jetty.server.Server httpserver;
    private boolean started;
    private String uiurl;

    public ServerMain(Properties configuration) {
        this.configuration = configuration;
        this.pidFileLocker = new PidFileLocker(Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath());
    }

    @Override
    public void close() {
        if (httpserver != null) {
            try {
                httpserver.stop();
            } catch (Exception ex) {
                Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                httpserver = null;
            }
        }
        pidFileLocker.close();
        running.countDown();
    }

    public boolean isStarted() {
        return started;
    }

    public void join() {
        try {
            running.await();
        } catch (InterruptedException discard) {
        }
        started = false;
    }

    public void start() throws Exception {
        pidFileLocker.lock();


        String httphost = configuration.getProperty("http.host", "localhost");
        int httpport = Integer.parseInt(configuration.getProperty("http.port", "4500"));

        httpserver = new org.eclipse.jetty.server.Server(new InetSocketAddress(httphost, httpport));

        org.eclipse.jetty.webapp.Configuration.ClassList classlist =
                org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(httpserver);
        classlist.addAfter(org.eclipse.jetty.webapp.FragmentConfiguration.class.getName(),
                org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getName(),
                org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getName());
        classlist.addBefore(org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getName(),
                org.eclipse.jetty.annotations.AnnotationConfiguration.class.getName());
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        httpserver.setHandler(contexts);
        WebAppContext webApp = new WebAppContext(new File("web").getAbsolutePath(), "/");
        contexts.addHandler(webApp);
        uiurl = "http://" + httphost + ":" + httpport + "/";
        System.out.println("Listening for client (http) connections on " + httphost + ":" + httpport);
        httpserver.start();

        System.out.println("Web Interface: " + uiurl);
        started = true;
    }

    public String getUiurl() {
        return uiurl;
    }

}
