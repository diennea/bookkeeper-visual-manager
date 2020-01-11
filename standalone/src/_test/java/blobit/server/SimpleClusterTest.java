package blobit.server;

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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.curator.test.TestingServer;
import org.blobit.core.api.BucketConfiguration;
import org.blobit.core.api.BucketHandle;
import org.blobit.core.api.Configuration;
import org.blobit.core.api.ObjectManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * SimpleServer boot test
 *
 * @author enrico.olivelli
 */
public class SimpleClusterTest {

    static {
        System.setProperty("zookeeper.admin.enableServer", "false");
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws Exception {

        try (TestingServer zookeeperServer = new TestingServer(-1, folder.newFolder("zk"));) {
            zookeeperServer.start();

            File tmpConfFile = folder.newFile("test.server_cluster.properties");

            try (InputStream in = SimpleClusterTest.class.getResourceAsStream("/conf/test.server_cluster.properties")) {
                Properties props = new Properties();
                props.load(in);

                props.put(ServerConfiguration.PROPERTY_BASEDIR, folder.newFolder().getAbsolutePath());
                props.put(ServerConfiguration.PROPERTY_ZOOKEEPER_ADDRESS, zookeeperServer.getConnectString());
                props.put(ServerConfiguration.PROPERTY_BOOKKEEPER_ZK_LEDGERS_ROOT_PATH, "/custom-path");

                props.put("herddb." + herddb.server.ServerConfiguration.PROPERTY_BASEDIR, folder.newFolder().
                        getAbsolutePath());
                props.put("herddb." + herddb.server.ServerConfiguration.PROPERTY_MODE,
                        herddb.server.ServerConfiguration.PROPERTY_MODE_CLUSTER);

                // client configuration of the BlobIt client started inside the server (For the HTTP API)
                props.put(Configuration.ZOOKEEPER_URL, zookeeperServer.getConnectString());
                props.put(Configuration.BOOKKEEPER_ZK_LEDGERS_ROOT_PATH, "/custom-path");

                props.put(ServerConfiguration.PROPERTY_BOOKKEEPER_START, "true");
                props.put("bookie.allowLoopback", "true");
                try (FileOutputStream oo = new FileOutputStream(tmpConfFile)) {
                    props.store(oo, "");
                }
                System.out.println("props: " + props);
            }
            Thread runner = new Thread(() -> {
                ServerMain.main(tmpConfFile.getAbsolutePath());
            });
            runner.start();
            while (ServerMain.getRunningInstance() == null
                    || !ServerMain.getRunningInstance().isStarted()) {
                Thread.sleep(1000);
                System.out.println("waiting for boot");
            }

            try (ObjectManager client = ServerMain.getRunningInstance().getClient();) {
                client.createBucket("mybucket", "mybucket", BucketConfiguration.DEFAULT);
                BucketHandle bucket = client.getBucket("mybucket");
                String id = bucket.put("myblob", "test".getBytes(StandardCharsets.UTF_8)).get();
                bucket.get(id).get();
                bucket.getByName("myblob").get();
            }

//                Thread.sleep(Integer.MAX_VALUE);
            ServerMain.getRunningInstance().close();
        } finally {
            if (ServerMain.getRunningInstance() != null) {
                ServerMain.getRunningInstance().close();
            }
        }

    }
}
