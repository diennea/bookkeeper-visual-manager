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

import static org.bkvm.bookkeeper.BookkeeperManager.ClusterWideConfiguration;
import static org.bkvm.bookkeeper.BookkeeperManager.RefreshCacheWorkerStatus;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Data;
import org.bkvm.auth.UserRole;
import org.bkvm.cache.Cluster;
import org.bkvm.utils.StringUtils;

@Path("cluster")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class ClusterResource extends AbstractBookkeeperResource {

    @GET
    @Secured
    @PermitAll
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClusterBean> getClusters() throws Exception {
        RefreshCacheWorkerStatus status = getBookkeeperManager().getRefreshWorkerStatus();
        Map<Integer, ClusterWideConfiguration> clusterWideConfigurations = status.getLastClusterWideConfiguration();

        List<ClusterBean> res = new ArrayList<>();
        Collection<Cluster> clusters = getBookkeeperManager().getAllClusters();
        for (Cluster cluster : clusters) {
            ClusterBean bean = new ClusterBean();
            bean.setClusterId(cluster.getClusterId());
            bean.setName(cluster.getName());
            bean.setMetadataServiceUri(cluster.getMetadataServiceUri());

            ClusterWideConfiguration c = clusterWideConfigurations.get(cluster.getClusterId());
            if (c != null) {
                Map<String, Object> conf = new HashMap<>();
                StringReader reader = new StringReader(StringUtils.trimToEmpty(c.getConfiguration()));
                try {
                    Properties properties = new Properties();
                    properties.load(reader);
                    for (String p : properties.stringPropertyNames()) {
                        conf.put(p, properties.get(p));
                    }
                } catch (IOException ex) {}

                bean.setRefreshStatus(status.getStatus().toString());
                ClusterStatus clusterStatus = new ClusterStatus(conf,
                        c.getAuditor(), c.isAutorecoveryEnabled(), c.getLostBookieRecoveryDelay(),
                        c.getLayoutFormatVersion(), c.getLayoutManagerFactoryClass(), c.getLayoutManagerVersion()
                );
                bean.setStatus(clusterStatus);
            }
            res.add(bean);
        }

        return res;
    }

    @GET
    @Secured
    @PermitAll
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public int getClusterCount() throws Exception {
        List<ClusterBean> clusters = getClusters();
        return clusters.size();
    }

    @POST
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addCluster(ClusterBean bean) throws Exception {
        Cluster cluster = new Cluster();
        cluster.setName(bean.getName());
        cluster.setMetadataServiceUri(bean.getMetadataServiceUri());
        cluster.setConfiguration(bean.getConfiguration());
        getBookkeeperManager().updateCluster(cluster);
    }

    @POST
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("edit")
    @Consumes(MediaType.APPLICATION_JSON)
    public void editCluster(ClusterBean bean) throws Exception {
        Cluster cluster = new Cluster();
        cluster.setClusterId(bean.getClusterId());
        cluster.setName(bean.getName());
        cluster.setMetadataServiceUri(bean.getMetadataServiceUri());
        cluster.setConfiguration(bean.getConfiguration());
        getBookkeeperManager().updateCluster(cluster);
    }

    @POST
    @Secured
    @RolesAllowed(UserRole.Fields.Admin)
    @Path("delete/{clusterId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteCluster(@PathParam(value = "clusterId") int clusterId) throws Exception {
        getBookkeeperManager().deleteCluster(clusterId);
    }

    @Data
    public static final class ClusterStatus {

        private final Map<String, Object> bookkeeperConfiguration;
        private final String auditor;
        private final boolean autorecoveryEnabled;
        private final int lostBookieRecoveryDelay;
        private final int layoutFormatVersion;
        private final String layoutManagerFactoryClass;
        private final int layoutManagerVersion;

        public ClusterStatus(Map<String, Object> bookkeeperConfiguration,
                             String auditor, boolean autorecoveryEnabled, int lostBookieRecoveryDelay,
                             int layoutFormatVersion, String layoutManagerFactoryClass, int layoutManagerVersion) {
            this.bookkeeperConfiguration = bookkeeperConfiguration;
            this.auditor = auditor;
            this.autorecoveryEnabled = autorecoveryEnabled;
            this.lostBookieRecoveryDelay = lostBookieRecoveryDelay;
            this.layoutFormatVersion = layoutFormatVersion;
            this.layoutManagerFactoryClass = layoutManagerFactoryClass;
            this.layoutManagerVersion = layoutManagerVersion;
        }

    }

    @Data
    public static final class ClusterBean {

        private int clusterId;
        private String name;
        private String metadataServiceUri;
        private String configuration;

        private String refreshStatus;
        private ClusterStatus status;

    }

}
