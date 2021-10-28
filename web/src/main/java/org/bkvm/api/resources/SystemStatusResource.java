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

import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD;
import static org.bkvm.config.ServerConfiguration.PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.bkvm.auth.UserRole;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.bookkeeper.BookkeeperManager.RefreshCacheWorkerStatus;
import org.bkvm.config.ConfigurationStore;

@Path("cache")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class SystemStatusResource extends AbstractBookkeeperResource {

    @Getter
    @Setter
    public static final class SystemStatus {

        private final String status;
        private final long lastCacheRefresh;
        private Integer metadataRefreshPeriod;

        public SystemStatus(RefreshCacheWorkerStatus status) {
            this.status = status.getStatus().toString();
            this.lastCacheRefresh = status.getLastMetadataCacheRefresh();
        }

    }

    @GET
    @Secured
    @PermitAll
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemStatus getInfo() throws Exception {
        BookkeeperManager bookkeeperManager = getBookkeeperManager();
        ConfigurationStore configStore = bookkeeperManager.getConfigStore();

        Integer metadataRefreshPeriod = Integer.parseInt(configStore.getProperty(PROPERTY_METADATA_REFRESH_PERIOD, PROPERTY_METADATA_REFRESH_PERIOD_DEFAULT));
        SystemStatus ss = new SystemStatus(bookkeeperManager.getRefreshWorkerStatus());
        ss.setMetadataRefreshPeriod(metadataRefreshPeriod);

        return ss;
    }

    @GET
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemStatus refresh() throws Exception {
        getBookkeeperManager().refreshMetadataCache();
        return getInfo();
    }

}
