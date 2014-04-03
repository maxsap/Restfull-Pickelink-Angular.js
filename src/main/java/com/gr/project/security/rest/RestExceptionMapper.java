/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gr.project.security.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;

/**
 * @author pedroigor
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<AccessDeniedException> {

    @Override
    public Response toResponse(AccessDeniedException exception) {
    	if(exception instanceof AccessDeniedException) {
    		StringBuilder violations = new StringBuilder(exception.getViolations().size());
    		
    		for(SecurityViolation v : exception.getViolations()) {
    			violations.append(v.getReason()).append(" ");
    		}
    		return Response.status(Response.Status.UNAUTHORIZED).entity(violations.toString()).build();
    	}
    	
        return Response.status(Response.Status.FORBIDDEN).entity(exception.getMessage()).build();
    }

}
