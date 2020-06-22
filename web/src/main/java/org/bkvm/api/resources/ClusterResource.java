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
    public List<ClusterBean> getCluster() throws Exception {
        List<ClusterBean> res = new ArrayList<>();

        Collection<Cluster> clusters = getBookkeeperManger().getAllClusters();
        for (Cluster cluster : clusters) {
            ClusterBean bean = new ClusterBean();
            bean.setName(cluster.getName());
            bean.setMetadataServiceUri(cluster.getMetadataServiceUri());
            
            res.add(bean);
        }

        return res;
    }

    @POST
    @Secured
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addCluster(ClusterBean bean) throws Exception {

        Cluster cluster = new Cluster();
        cluster.setName(bean.getName());
        cluster.setMetadataServiceUri(bean.getMetadataServiceUri());
        getBookkeeperManger().updateCluster(cluster);
    }
    
    @POST
    @Secured
    @Path("delete/{clusterName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteCluster(@PathParam(value = "clusterName") String clusterName) throws Exception {
        getBookkeeperManger().deleteCluster(clusterName);
    }

    public static final class ClusterBean implements Serializable {

        private String name;
        private String metadataServiceUri;

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

        @Override
        public String toString() {
            return "ClusterBean{" + "name=" + name + ", metadataServiceUri=" + metadataServiceUri + '}';
        }

    }

}
