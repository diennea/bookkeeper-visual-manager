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
package org.bookkeepervisualmanager.api;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.bookkeepervisualmanager.api.listeners.AuthFilter;

@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(AuthFilter.class);
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.bookkeepervisualmanager.api.resources.BookiesResource.class);
        resources.add(org.bookkeepervisualmanager.api.resources.CacheResource.class);
        resources.add(org.bookkeepervisualmanager.api.resources.LedgersResource.class);
        resources.add(org.bookkeepervisualmanager.api.resources.LoginResource.class);
    }

}
