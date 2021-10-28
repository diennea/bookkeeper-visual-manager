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
package org.bkvm.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.EnumUtils;
import org.bkvm.config.ConfigurationStore;

/**
 * Handles Authentication.
 */
public final class AuthManager {

    private static final Logger LOG = Logger.getLogger(AuthManager.class.getName());

    private final Map<String, User> users = new HashMap<>();

    public AuthManager(ConfigurationStore store) {
        for (int i = 0; i < 10; i++) {
            String username = store.getProperty("user." + i + ".username", "");
            if (!username.isEmpty()) {
                String password = store.getProperty("user." + i + ".password", "");
                String role = store.getProperty("user." + i + ".role", UserRole.Fields.User);
                if (checkUserRole(role)) {
                    LOG.log(Level.CONFIG, "Configure user {0} with role {1}", new Object[]{username, role});
                    users.put(username, new User(username, password, role));
                } else {
                    LOG.log(Level.SEVERE, "Invalid role for user {0}", username);
                }
            }
        }
        if (users.isEmpty()) {
            LOG.log(Level.INFO, "No user is configured, adding default user 'admin' with password 'admin'");
            User user = new User("admin", "admin", "Admin");
            users.put("admin", user);
        }
    }

    private boolean checkUserRole(String role) {
        return EnumUtils.isValidEnum(UserRole.class, role);
    }

    public boolean login(String username, String password) {
        User expected = users.get(username);
        return expected.getPassword().equals(password);
    }

    public User getUser(String username) {
        return users.get(username);
    }
}
