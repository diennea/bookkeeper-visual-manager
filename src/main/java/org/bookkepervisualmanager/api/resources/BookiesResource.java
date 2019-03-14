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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.apache.bookkeeper.net.BookieSocketAddress;

@Path("bookie")
public class BookiesResource extends AbstractBookkeeperResource {

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getBookies() throws Exception {
        List<String> bookies = new ArrayList<>();
        
        Collection<BookieSocketAddress> status = getBookkeeperManger().getAllBookies();
        status.forEach(bookieHostName -> bookies.add(bookieHostName.toString()));

        return bookies;
    }
    
    @GET
    @Path("available")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getAvailableBookies() throws Exception {
        List<String> bookies = new ArrayList<>();

        Collection<BookieSocketAddress> status = getBookkeeperManger().getAvailableBookies();
        status.forEach(bookieHostName -> bookies.add(bookieHostName.toString()));
        return bookies;
    }
}
