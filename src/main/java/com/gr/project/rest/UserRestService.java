package com.gr.project.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.gr.project.security.credential.TokenCredential;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import com.gr.project.data.MemberDAO;
import com.gr.project.model.Email;
import com.gr.project.model.Member;
import com.gr.project.security.UserLoggedIn;
import com.gr.project.security.rest.LoginService;
import com.gr.project.security.rest.RegistrationRequest;
import com.gr.project.service.Registrator;
import com.gr.project.util.EntityValidator;


@Path("/users")
@Stateless
public class UserRestService {
	
     private static final String MESSAGE_RESPONSE_PARAMETER = "message";
    
     @Inject
     private Logger log;

     @Inject
     private MemberDAO repository;

     @Inject
     private EntityValidator validator;
     
     @Inject
     Registrator registration;
     
     @Inject
     private IdentityManager identityManager;
     
     @Inject
     private Identity identity;

     @Inject
     private RelationshipManager relationshipManager;

     @Inject
     private LoginService loginService;
     
     @Inject
     private EntityManager em;
     
     @Inject
 	 @Any
 	 private Event<Email> event;
     
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @UserLoggedIn
     public List<Member> listAllMembers() {
         return repository.findAllOrderedByName();
     }

     @GET
     @Path("/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     @UserLoggedIn
     public Member lookupMemberById(@PathParam("id") String id) {
    	 Member user = repository.findById(id);
         if (user == null) {
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
         return user;
     }
     
     @POST
     @Path("/login")
     @Produces(MediaType.APPLICATION_JSON)
     public Response loginUser(@NotNull DefaultLoginCredentials credential) {
    	 
    	 Map<String, Object> response = new HashMap<String, Object>();
    	 
    	 try {
	    	 if (!this.identity.isLoggedIn()) {
	             this.loginService.login(credential);
	         }
	
	         Account account = this.identity.getAccount();
	
	         if (account == null) {
	        	 log.log(Level.WARNING, "Authentication account is empty");
	        	 response.put(MESSAGE_RESPONSE_PARAMETER, "User Not Found.");
	         } else {
	        	 return Response.ok().entity(account).type(MediaType.APPLICATION_JSON_TYPE).build();
	         }
	         
    	 } catch(Exception ex) {
    		 response.put(MESSAGE_RESPONSE_PARAMETER, "Oops ! Authentication failed, try it later.");
    	 }
    	 
    	 return Response.status(Response.Status.FORBIDDEN).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
     }


     
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     public Response createMember(@NotNull RegistrationRequest request) {
    	 
    	 Map<String, Object> response = new HashMap<String, Object>();
    	 
    	 Member member = null;
         
         if (!request.getPassword().equals(request.getPasswordConfirmation())) {
             response.put(MESSAGE_RESPONSE_PARAMETER, "Password mismatch.");
         } else {
             try {
                 // if there is no user with the provided e-mail, perform registration
                 if (BasicModel.getUser(this.identityManager, request.getEmail()) == null) {
                	 member = performRegistration(request);
                	 
                	 this.validator.validateEntity(member);
                	 
                	 em.persist(member);

                     // if the registration was successful, we perform a silent authentication.
                     performSilentAuthentication(request);
                     
//                     Email email = new Email("Successfull Subscription", "Success registration", "max.sapranidis@gmail.com");
//         			 event.fire(email);
                     
                     return Response.status(Response.Status.OK).entity(member).type(MediaType.APPLICATION_JSON_TYPE).build();
                 } else {
                     response.put(MESSAGE_RESPONSE_PARAMETER, "This username is already in use. Try another one.");
                 }
             } catch (IdentityManagementException ime) {
            	 if(member != null)
            		 em.remove(member);
                 response.put(MESSAGE_RESPONSE_PARAMETER, "Oops ! Registration failed, try it later.");
             }
         }
         
         return Response.status(Response.Status.BAD_REQUEST).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
     }
     /**
      * <p>Performs a registration using the data provided by {@link RegistrationRequest}.</p>
      * <p>The roles and groups in this method were previously created by the
      * {@link org.jboss.jdf.example.ticketmonster.security.IdentityManagementInitializer} during startup.</p>
      * @param request
      */
     private Member performRegistration(RegistrationRequest request) {
         User newUser = new User(request.getEmail());
         
         newUser.setEmail(request.getEmail());

         newUser.setFirstName(request.getFirstName());
         newUser.setLastName(request.getLastName());
         
         newUser.setAttribute( new Attribute<String>("Status", "Disabled") );
         
         
         this.identityManager.add(newUser);
         
         Password password = new Password(request.getPassword());
         
//         TOTPCredentials totp = new TOTPCredentials();
//         
//         totp.setPassword();
//         
//         
//         TimeBasedOTP timeBasedOTP = new TimeBasedOTP();
//
//         // let's manually generate a token based on the user secret
//         String token = timeBasedOTP.generate(request.getPassword());
//
//         totp.setToken(token);
         
//         newUser.setAttribute( new Attribute<String>("token", token) );
         
         this.identityManager.updateCredential(newUser, password);
         
         TokenCredential tokenCredentials = new TokenCredential(UUID.randomUUID().toString());
         
         // also bound the user with the token
         this.identityManager.updateCredential(newUser, tokenCredentials);
         
         Role userRole = BasicModel.getRole(this.identityManager, "User");

         BasicModel.grantRole(this.relationshipManager, newUser, userRole);
         
         Group userGroup = BasicModel.getGroup(this.identityManager, "Users");
         
         BasicModel.addToGroup(this.relationshipManager, newUser, userGroup);
         
         return memberFromUser(newUser);
     }
     
     private Member memberFromUser(User newUser) {
		Member m = new Member();
		m.setEmail(newUser.getEmail());
		m.setFirstName(newUser.getFirstName());
		m.setId(newUser.getId());
		m.setLastName(newUser.getLastName());
		m.setActivationCode(UUID.randomUUID().toString());
		
		return m;
	}

	private void performSilentAuthentication(RegistrationRequest request) {
         DefaultLoginCredentials authenticationRequest = new DefaultLoginCredentials();
         
         authenticationRequest.setUserId(request.getEmail());
         authenticationRequest.setPassword(request.getPassword());

         this.loginService.login(authenticationRequest);
     }

 }
