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

import java.util.Map;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bkvm.auth.UserRole;
import org.bkvm.bookkeeper.topology.BookieTopologyCache;

@Path("topology")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class BookiesTopologyResource extends AbstractBookkeeperResource {


    @Data
    @AllArgsConstructor
    public static final class GetBookiesResult {

        private Map<String, BookieTopologyCache.BookieTopology> bookies;

    }

    @GET
    @Secured
    @PermitAll
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public GetBookiesResult getBookies() throws Exception {
        return new GetBookiesResult(getBookkeeperManager().getBookiesTopology());
    }

}
