/*
 * Copyright 2014 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.gr.project.security.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

/**
 * Extremely simple chat application supportiong WebSocket, Server Side-Events, Long-Polling and Streaming.
 *
 * @author Jeanfrancois Arcand
 */
@Path("/chat")
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class},
        path = "/chat",
        servlet = "org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher")
@Consumes("application/json")
@Produces("application/json")
public class RestToAtmosphere {

    /**
     * Echo the chat message. Jackson can clearly be used here, but for simplicity we just echo what we receive.
     * @param message
     */
    @SuppressWarnings("deprecation")
	@POST
    public void broadcast(String message) {
        BroadcasterFactory.getDefault().lookup("/chat").broadcast(message);
    }

}
