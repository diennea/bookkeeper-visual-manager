/*
 * Licensed to Diennea S.r.l. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Diennea S.r.l. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.bkvm.bookkeeper.restapi;

import java.net.URI;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.bkvm.api.ApplicationConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author dene
 */
public class BookkeeperApiTest {

    private HttpServer server;
    private WebTarget bookieApi;
    int httpServerPort;

    @Before
    public void setUp() throws Exception {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        URI uri = UriBuilder.fromUri("http://localhost/").port(8081).build();
        server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);
        bookieApi = ClientBuilder.newClient().target("http://localhost:8081/gc");
    }

    @After
    public void tearDown() {
        server.shutdownNow();
    }

    @Test
    public void bookieGcTriggerTest() throws Exception {
        Response response = bookieApi
                .path("trigger")
                .queryParam("bookie", "localhost:" + 8080)
                .queryParam("ssl", "false")
                .request()
                .put(Entity.entity(BookieApiResponse.class, MediaType.APPLICATION_JSON));
        String message = response.readEntity(String.class);
        System.out.println("Response " + message);
        //assertEquals(Status.OK.getStatusCode(), response.getStatus());

    }
}
