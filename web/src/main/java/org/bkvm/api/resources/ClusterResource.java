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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bkvm.cache.Cluster;

@Path("cluster")
public class ClusterResource extends AbstractBookkeeperResource {

    @GET
    @Secured
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClusterBean> getClusters() throws Exception {
        List<ClusterBean> res = new ArrayList<>();

        Collection<Cluster> clusters = getBookkeeperManager().getAllClusters();
        for (Cluster cluster : clusters) {
            ClusterBean bean = new ClusterBean();
            bean.setClusterId(cluster.getClusterId());
            bean.setName(cluster.getName());
            bean.setMetadataServiceUri(cluster.getMetadataServiceUri());

            res.add(bean);
        }

        return res;
    }

    @GET
    @Secured
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public int getClusterCount() throws Exception {
        List<ClusterBean> clusters = getClusters();
        return clusters.size();
    }

    @POST
    @Secured
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
    @Path("delete/{clusterId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteCluster(@PathParam(value = "clusterId") int clusterId) throws Exception {
        getBookkeeperManager().deleteCluster(clusterId);
    }

    public static final class ClusterBean implements Serializable {

        private int clusterId;
        private String name;
        private String metadataServiceUri;
        private String configuration;

        public int getClusterId() {
            return clusterId;
        }

        public void setClusterId(int clusterId) {
            this.clusterId = clusterId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMetadataServiceUri() {
            return metadataServiceUri;
        }

        public void setMetadataServiceUri(String metadataServiceUri) {
            this.metadataServiceUri = metadataServiceUri;
        }

        public String getConfiguration() {
            return configuration;
        }

        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }

        @Override
        public String toString() {
            return "ClusterBean{" + "name=" + name + ", metadataServiceUri=" + metadataServiceUri + '}';
        }

    }

}