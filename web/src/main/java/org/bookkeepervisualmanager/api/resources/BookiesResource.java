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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.bookkeepervisualmanager.cache.Bookie;


@Path("bookie")
public class BookiesResource extends AbstractBookkeeperResource {

    @GET
    @Secured
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BookieBean> getBookies() throws Exception {
        final List<BookieBean> bookies = new ArrayList<>();
        final Collection<Bookie> fromMetadata = getBookkeeperManger().getAllBookies();

        for (Bookie bookie : fromMetadata) {
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

            b.setFreeDiskSpace(bookie.getFreeDiskspace());
            b.setTotalDiskSpace(bookie.getTotalDiskspace());
            b.setLastScan(bookie.getScanTime().getTime());

            bookies.add(b);
        }
        return bookies;
    }

    public static final class BookieBean implements Serializable {

        private String state;
        private String description;

        private long freeDiskSpace;
        private long totalDiskSpace;
        private long lastScan;

        public long getLastScan() {
            return lastScan;
        }

        public void setLastScan(long lastScan) {
            this.lastScan = lastScan;
        }


        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public long getFreeDiskSpace() {
            return freeDiskSpace;
        }

        public void setFreeDiskSpace(long freeDiskSpace) {
            this.freeDiskSpace = freeDiskSpace;
        }

        public long getTotalDiskSpace() {
            return totalDiskSpace;
        }

        public void setTotalDiskSpace(long totalDiskSpace) {
            this.totalDiskSpace = totalDiskSpace;
        }

        @Override
        public String toString() {
            return "BookieBean{" + "state=" + state + ", description=" + description
                    + ", freeDiskSpace=" + freeDiskSpace + ", totalDiskSpace=" + totalDiskSpace + '}';
        }

    }

}
