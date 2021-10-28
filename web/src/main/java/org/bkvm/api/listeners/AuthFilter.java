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
package org.bkvm.api.listeners;

import java.io.IOException;
import java.security.Principal;
import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.bkvm.api.resources.Secured;
import org.bkvm.auth.AuthManager;
import org.bkvm.auth.User;

/**
 * Ensures that the user is authenticated
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest request;

    @Context
    private ServletContext application;

    private AuthManager authManager;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (request.getSession(false) == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            String username = (String) request.getSession(false).getAttribute("username");
            if (username != null) {
                authManager = (AuthManager) application.getAttribute("authManager");
                User user = authManager.getUser(username);
                requestContext.setSecurityContext(new CustomSecurityContext(user));
            }
        }
    }

    public static class CustomSecurityContext implements SecurityContext {

        private final User user;

        public CustomSecurityContext(User user) {
            this.user = user;
        }

        @Override
        public Principal getUserPrincipal() {
            return () -> user.getUsername();
        }

        @Override
        public boolean isUserInRole(String role) {
            return role.equals(user.getRole());
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String getAuthenticationScheme() {
            return "Basic";
        }

    }

}
