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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.bkvm.bookkeeper.BookkeeperManager.RefreshCacheWorkerStatus;

@Path("cache")
public class SystemStatusResource extends AbstractBookkeeperResource {

    @Getter
    @Setter
    public static final class SystemStatus {

        private String status;
        private long lastCacheRefresh;

        public SystemStatus(RefreshCacheWorkerStatus status) {
            this.lastCacheRefresh = status.getLastMetadataCacheRefresh();
            this.status = status.getStatus().toString();
        }

    }

    @GET
    @Secured
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemStatus getInfo() throws Exception {
        return new SystemStatus(getBookkeeperManager().getRefreshWorkerStatus());
    }

    @GET
    @Secured
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemStatus refresh() throws Exception {
        getBookkeeperManager().refreshMetadataCache();
        return getInfo();
    }

}
