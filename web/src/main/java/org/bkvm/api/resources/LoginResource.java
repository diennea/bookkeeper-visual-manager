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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.bkvm.auth.AuthManager;
import org.bkvm.auth.User;

/**
 * Login
 */
@Path("auth")
public class LoginResource extends AbstractBookkeeperResource {

    private static final Map<String, User> USERS;

    static {
        USERS = new HashMap<>();
        USERS.put("admin", new User("admin", "admin", "Admin"));
    }

    @Context
    private HttpServletRequest request;

    @Context
    private ServletContext application;

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        AuthManager authManager = (AuthManager) application.getAttribute("authManager");

        LoginResponse response = new LoginResponse();
        if (authManager.login(username, password)) {
            // force Session creation
            request.getSession(true).setAttribute("username", username);
            response.setOk(true);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.setOk(false);
        }
        return response;
    }

    @POST
    @Path("logout")
    public void logout() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static class LoginResponse implements java.io.Serializable {

        private boolean ok;
        private String role;

        public LoginResponse() {
        }

        public boolean isOk() {
            return ok;
        }

        public String getRole() {
            return role;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class LoginRequest implements java.io.Serializable {

        private String username;
        private String password;

        public LoginRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

}
