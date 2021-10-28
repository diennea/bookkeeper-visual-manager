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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bkvm.auth.UserRole;
import org.bkvm.cache.Bookie;
import org.bkvm.cache.Cluster;

@Path("bookie")
@DeclareRoles({UserRole.Fields.Admin, UserRole.Fields.User})
public class BookiesResource extends AbstractBookkeeperResource {

    @Data
    @AllArgsConstructor
    public static final class GetBookiesResult {

        private List<BookieBean> bookies;
        private int totalBookies;

    }

    @GET
    @Secured
    @PermitAll
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public GetBookiesResult getBookies(@QueryParam("page") int page,
            @QueryParam("size") int size
    ) throws Exception {
        final Collection<Bookie> allBookies = getBookkeeperManager().getAllBookies();
        final List<Bookie> filteredBookies = filterBookies(allBookies, page, size);

        final Map<Integer, Cluster> allClusters = getBookkeeperManager().getAllClusters()
                .stream().collect(Collectors.toMap(Cluster::getClusterId, Function.identity()));

        final List<BookieBean> bookies = new ArrayList<>();
        for (Bookie bookie : filteredBookies) {
            BookieBean b = new BookieBean();
            b.setDescription(bookie.getDescription());
            switch (bookie.getState()) {
                case Bookie.STATE_AVAILABLE:
                    b.setState("available");
                    break;
                case Bookie.STATE_READONLY:
                    b.setState("readonly");
                    break;
                default:
                    b.setState("down");
                    break;
            }

            b.setClusterId(bookie.getClusterId());
            b.setClusterName(allClusters.get(bookie.getClusterId()).getName());
            b.setBookieId(bookie.getBookieId());
            b.setFreeDiskSpace(bookie.getFreeDiskspace());
            b.setTotalDiskSpace(bookie.getTotalDiskspace());
            b.setLastScan(bookie.getScanTime().getTime());
            Bookie.BookieInfo parsedBookieInfo = Bookie.parseBookieInfo(bookie.getBookieInfo());
            Map<String, String> endpoints = new HashMap<>();
            parsedBookieInfo.getEndpoints().forEach(info -> {
                endpoints.put(info.getId(), info.getProtocol() + "://" + info.getAddress());
            });
            b.setEndpoints(endpoints);
            b.setProperties(parsedBookieInfo.getProperties());
            bookies.add(b);
        }

        return new GetBookiesResult(bookies, allBookies.size());
    }

    private List<Bookie> filterBookies(Collection<Bookie> bookies, int offset, int limit) {
        int skipIndex = (offset - 1) * limit;
        return bookies.stream()
                .skip(skipIndex)
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Data
    public static final class BookieBean implements Serializable {

        private String state;
        private int clusterId;
        private String clusterName;
        private String bookieId;
        private String description;

        private long freeDiskSpace;
        private long totalDiskSpace;
        private long lastScan;
        private Map<String, String> endpoints;
        private Map<String, String> properties;

    }

}
