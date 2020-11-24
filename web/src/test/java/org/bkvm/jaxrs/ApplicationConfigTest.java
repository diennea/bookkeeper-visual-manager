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
package org.bkvm.jaxrs;

import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.bkvm.api.ApplicationConfig;
import org.junit.Test;

/**
 *
 * @author matteo.minardi
 */
public class ApplicationConfigTest {

    @Test
    public void resourcesClassesTest() {
        ApplicationConfig config = new ApplicationConfig();

        Set<Class<?>> classes = config.getClasses();
        assertTrue(classes.size() == 6);

        assertContainsClass(classes, org.bkvm.api.listeners.AuthFilter.class);
        assertContainsClass(classes, org.bkvm.api.resources.LoginResource.class);
        assertContainsClass(classes, org.bkvm.api.resources.BookiesResource.class);
        assertContainsClass(classes, org.bkvm.api.resources.ClusterResource.class);
        assertContainsClass(classes, org.bkvm.api.resources.LedgersResource.class);
        assertContainsClass(classes, org.bkvm.api.resources.LoginResource.class);
        assertContainsClass(classes, org.bkvm.api.resources.SystemStatusResource.class);

    }

    private void assertContainsClass(Set<Class<?>> classes, Class clazz) {
        assertTrue(classes.contains(clazz));
    }

}
