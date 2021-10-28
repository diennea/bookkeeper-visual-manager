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
package org.bkvm.api.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.bkvm.auth.UserRole;
import org.bkvm.bookkeeper.restapi.BookieApiResponse;
import org.bkvm.bookkeeper.restapi.HttpRequestUtils;
import org.bkvm.utils.BookkeeperApiEndpoint;

@Path("gc")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class BookieGcResource extends AbstractBookkeeperResource {

    @GET
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("details")
    @Produces(MediaType.APPLICATION_JSON)
    public BookieApiResponse getGCDeatails(@QueryParam("clusterId") int clusterId, @QueryParam("bookieId") String bookieId) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        String bookieHttpServerUri = getBookkeeperManager().getHttpServerEndpoint(clusterId, bookieId);
        return HttpRequestUtils.sendGetRequest(bookieHttpServerUri, BookkeeperApiEndpoint.BOOKIE_GC_DETAIL);
    }

    @GET
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("trigger")
    @Produces(MediaType.APPLICATION_JSON)
    public BookieApiResponse triggerGC(@QueryParam("clusterId") int clusterId, @QueryParam("bookieId") String bookieId) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        String bookieHttpServerUri = getBookkeeperManager().getHttpServerEndpoint(clusterId, bookieId);
        return HttpRequestUtils.sendPutRequest(bookieHttpServerUri, BookkeeperApiEndpoint.BOOKIE_GC, null);
    }

    @GET
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("gcstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public BookieApiResponse gcStatus(@QueryParam("clusterId") int clusterId, @QueryParam("bookieId") String bookieId) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
        String bookieHttpServerUri = getBookkeeperManager().getHttpServerEndpoint(clusterId, bookieId);
        return HttpRequestUtils.sendGetRequest(bookieHttpServerUri, BookkeeperApiEndpoint.BOOKIE_GC);
    }

}
