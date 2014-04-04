package com.gr.project.security.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;

import com.gr.project.rest.UserRestService;
import com.gr.project.security.credential.TokenCredential;
import com.gr.project.util.ThreadLocalUtils;

@Path("/session")
@Stateless
public class SessionManagmentEndpoint {
	
	@Inject
    private Identity identity;

    @Inject
    private SessionService loginService;
    
    @Inject
    private DefaultLoginCredentials credentials;

	@POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@NotNull DefaultLoginCredentials credential) {
   	 
   	 Map<String, Object> response = new HashMap<String, Object>();
   	 
   	 try {
	    	 if (!this.identity.isLoggedIn()) {
	    		 HttpServletRequest httpRequest = ThreadLocalUtils.currentRequest.get();
	    		 
	    		 if(httpRequest.getHeader("x-session-token") != null) {
	    			 credentials.setCredential(new TokenCredential(httpRequest.getHeader("x-session-token")));
	    			 loginService.loginWithToken(credential);
	    		 } else {
	    			 loginService.login(credential);
	    		 }
	         }
	
	         Account account = this.identity.getAccount();
	
	         if (account == null) {
	        	 response.put(UserRestService.MESSAGE_RESPONSE_PARAMETER, "User Not Found.");
	         } else {
	        	 return Response.ok().entity(account).type(MediaType.APPLICATION_JSON_TYPE).build();
	         }
	         
   	 } catch(Exception ex) {
   		 response.put(UserRestService.MESSAGE_RESPONSE_PARAMETER, "Oops ! Authentication failed, try it later.");
   	 }
   	 
   	 return Response.status(Response.Status.FORBIDDEN).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
