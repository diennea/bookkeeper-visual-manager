/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package org.bookkepervisualmanager.api.resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.client.BookieInfoReader.BookieInfo;
import org.apache.bookkeeper.net.BookieSocketAddress;

@Path("bookie")
public class BookiesResource extends AbstractBookkeeperResource {

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BookieBean> getBookies() throws Exception {
        final List<BookieBean> bookies = new ArrayList<>();
        final Map<BookieSocketAddress, BookieInfo> bookiesAvailable = getBookkeeperManger().getBookieInfo();
        final Collection<BookieSocketAddress> bookiesCookie = getBookkeeperManger().getAllBookies();

        for (BookieSocketAddress bookieAddress : bookiesCookie) {
            BookieBean b = new BookieBean();
            b.setDescription(bookieAddress.toString());
            b.setOk(bookiesAvailable.containsKey(bookieAddress));
            if (b.isOk()) {
                BookieInfo info = bookiesAvailable.get(bookieAddress);
                b.setFreeDiskSpace(info.getFreeDiskSpace());
                b.setTotalDiskSpace(info.getTotalDiskSpace());
            }
            bookies.add(b);
        }
        return bookies;
    }

    public final class BookieBean implements Serializable {

        private boolean ok;
        private String description;

        private long freeDiskSpace;
        private long totalDiskSpace;

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
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
            return "BookieBean{" + "ok=" + ok + ", description=" + description + ", freeDiskSpace=" + freeDiskSpace + ", totalDiskSpace=" + totalDiskSpace + '}';
        }

    }

}
