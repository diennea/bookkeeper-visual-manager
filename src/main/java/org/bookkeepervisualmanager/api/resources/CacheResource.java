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
package org.bookkeepervisualmanager.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bookkeepervisualmanager.bookkeeper.BookkeeperManager.RefreshCacheWorkerStatus;

@Path("cache")
public class CacheResource extends AbstractBookkeeperResource {

    public static final class CacheInfo {

        private long lastCacheRefresh;
        private String status;
        private String bookkeeperConfiguration;

        public CacheInfo(RefreshCacheWorkerStatus status) {
            this.lastCacheRefresh = status.getLastMetadataCacheRefresh();
            this.status = status.getStatus().toString();
            this.bookkeeperConfiguration = status.getBookkkeeperClientConfiguration();
        }

        public String getBookkeeperConfiguration() {
            return bookkeeperConfiguration;
        }

        public void setBookkeeperConfiguration(String bookkeeperConfiguration) {
            this.bookkeeperConfiguration = bookkeeperConfiguration;
        }

        public long getLastCacheRefresh() {
            return lastCacheRefresh;
        }

        public void setLastCacheRefresh(long lastCacheRefresh) {
            this.lastCacheRefresh = lastCacheRefresh;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

    @GET
    @Secured
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public CacheInfo getInfo() throws Exception {
        return new CacheInfo(getBookkeeperManger().getRefreshWorkerStatus());
    }

    @GET
    @Secured
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public CacheInfo refresh() throws Exception {
        getBookkeeperManger().refreshMetadataCache();
        return getInfo();
    }

}
