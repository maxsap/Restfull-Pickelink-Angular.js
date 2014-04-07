/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.gr.project.security.rest;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.Identity;
import org.picketlink.Identity.Stateless;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Account;

import com.gr.project.security.credential.Token;
import com.gr.project.security.credential.TokenCredential;
import com.gr.project.util.ThreadLocalUtils;

/**
 *
 */
@Path("/session")
@RequestScoped
public class SessionService {
	
	@Inject
	@Named("default.return.message.parameter")
	private String MESSAGE_RESPONSE_PARAMETER;

    @Inject
    @Stateless
    private Identity identity;
    
    @Inject
    private IdentityManager identityManager;

    @Inject
    private DefaultLoginCredentials credentials;

    public void login(DefaultLoginCredentials credential) {
        if (!this.identity.isLoggedIn()) {

            this.credentials.setUserId(credential.getUserId());
            this.credentials.setPassword(credential.getPassword());

            this.identity.login();
        }

    }
    
    
    
    
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@NotNull DefaultLoginCredentials credential) {
   	 
   	 Map<String, Object> response = new HashMap<String, Object>();
   	 
   	 try {
	    	 if (!this.identity.isLoggedIn()) {
	    		 HttpServletRequest httpRequest = ThreadLocalUtils.currentRequest.get();
	    		 
	    		 if(httpRequest.getHeader("x-session-token") != null && !httpRequest.getHeader("x-session-token").isEmpty()) {
	    			 if(httpRequest.getHeader("user-id") != null && !httpRequest.getHeader("user-id").isEmpty()) {
		    			 
		    			 TokenCredential tokenCredential = new TokenCredential(httpRequest.getHeader("x-session-token"));

			  		     tokenCredential.setLoginName(httpRequest.getHeader("user-id"));
			  		        
					     this.credentials.setCredential(tokenCredential);
					     
					     this.identity.login();
					     
	    			 }
	    		 } else {
	    			 login(credential);
	    		 }
	         }
	    	 
	         Account account = this.identity.getAccount();
	
	         if (account == null) {
	        	 response.put(MESSAGE_RESPONSE_PARAMETER, "User Not Found.");
	         } else {
	        	 // XXX is this going to invalidate the old token or the user is going to have multiple tokens ?
	        	 // XXX Best would be to update each time, but theoritically is it possible to have one time token, and if yes how to 
	        	 // query from it from the db?
	             String tokenId = "12345";
	             Token token = new Token(tokenId);

	             this.identityManager.updateCredential(account, token);
	             
	        	 return Response.ok().entity(token).type(MediaType.APPLICATION_JSON_TYPE).build();
	         }
	         
   	 } catch(Exception ex) {
   		 response.put(MESSAGE_RESPONSE_PARAMETER, "Oops ! Authentication failed, try it later.");
   	 }
   	 
   	 return Response.status(Response.Status.FORBIDDEN).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public void logout() {
        if (this.identity.isLoggedIn()) {
            this.identity.logout();
        }
    }
    
    
}