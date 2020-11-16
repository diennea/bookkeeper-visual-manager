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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bkvm.bookkeeper.BookkeeperManager;
import org.bkvm.bookkeeper.BookkeeperManager.RefreshCacheWorkerStatus;

@Path("cache")
public class SystemStatusResource extends AbstractBookkeeperResource {

    public static final class ClusterStatus {

        private final int clusterId;
        private final String clusterName;
        private final String bookkeeperConfiguration;
        private final String auditor;
        private final boolean autorecoveryEnabled;
        private final int lostBookieRecoveryDelay;
        private final int layoutFormatVersion;
        private final String layoutManagerFactoryClass;
        private final int layoutManagerVersion;

        public ClusterStatus(int clusterId, String clusterName, String bookkeeperConfiguration, String auditor, boolean autorecoveryEnabled, int lostBookieRecoveryDelay, int layoutFormatVersion,
                             String layoutManagerFactoryClass,
                             int layoutManagerVersion) {
            this.clusterId = clusterId;
            this.clusterName = clusterName;
            this.bookkeeperConfiguration = bookkeeperConfiguration;
            this.auditor = auditor;
            this.autorecoveryEnabled = autorecoveryEnabled;
            this.lostBookieRecoveryDelay = lostBookieRecoveryDelay;
            this.layoutFormatVersion = layoutFormatVersion;
            this.layoutManagerFactoryClass = layoutManagerFactoryClass;
            this.layoutManagerVersion = layoutManagerVersion;
        }

        public String getBookkeeperConfiguration() {
            return bookkeeperConfiguration;
        }

        public String getAuditor() {
            return auditor;
        }

        public boolean isAutorecoveryEnabled() {
            return autorecoveryEnabled;
        }

        public int getLostBookieRecoveryDelay() {
            return lostBookieRecoveryDelay;
        }

        public int getLayoutFormatVersion() {
            return layoutFormatVersion;
        }

        public String getLayoutManagerFactoryClass() {
            return layoutManagerFactoryClass;
        }

        public int getLayoutManagerVersion() {
            return layoutManagerVersion;
        }

        public int getClusterId() {
            return clusterId;
        }

        public String getClusterName() {
            return clusterName;
        }

    }

    public static final class SystemStatus {

        private long lastCacheRefresh;
        private String status;
        private List<ClusterStatus> clusters;

        public SystemStatus(RefreshCacheWorkerStatus status) {
            this.lastCacheRefresh = status.getLastMetadataCacheRefresh();
            this.status = status.getStatus().toString();
            this.clusters = new ArrayList<>();
            Map<Integer, BookkeeperManager.ClusterWideConfiguration> clusterWideConfiguration = status.getLastClusterWideConfiguration();
            clusterWideConfiguration.values().forEach(c -> {
                ClusterStatus clusterStatus = new ClusterStatus(c.getClusterId(), c.getClusterName(), c.getConfiguration(), c.getAuditor(), c.isAutorecoveryEnabled(), c.getLostBookieRecoveryDelay(),
                        c.getLayoutFormatVersion(), c.getLayoutManagerFactoryClass(), c.getLayoutManagerVersion());
                clusters.add(clusterStatus);
            });
        }

        public List<ClusterStatus> getClusters() {
            return clusters;
        }

        public void setClusters(List<ClusterStatus> clusters) {
            this.clusters = clusters;
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
