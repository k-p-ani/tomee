/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.servlet;

import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.util.NetworkUtil;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.apache.tomee.security.client.BasicAuthFilter;
import org.junit.Test;

import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SimpleServletTest {
    @Test
    public void testWebApp() throws Exception {
        try (Container container = new Container(
                new Configuration()
                        .conf("conf")
                        .http(NetworkUtil.getNextAvailablePort())
                        .property("openejb.container.additional.exclude", "org.apache.tomee.security.")
                        .property("openejb.additional.include", "tomee-"))
                .deployPathsAsWebapp(
                        JarLocation.jarLocation(SimpleServletTest.class),
                        JarLocation.jarLocation(TomEESecurityServletContainerInitializer.class))) {

            final Client client = ClientBuilder.newBuilder().register(new BasicAuthFilter()).build();
            final Response response =
                    client.target("http://localhost:" + container.getConfiguration().getHttpPort() + "/servlet")
                          .request()
                          .get();
            assertEquals(200, response.getStatus());
        }
    }

    @WebServlet(urlPatterns = "/servlet")
    @ServletSecurity(@HttpConstraint(rolesAllowed = "tomcat"))
    @BasicAuthenticationMechanismDefinition
    public static class TestServlet extends HttpServlet {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
                throws ServletException, IOException {
            resp.getWriter().write("ok!");
        }
    }
}
