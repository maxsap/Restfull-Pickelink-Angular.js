package com.gr.project.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.data.MemberDAO;
import com.gr.project.model.Email;
import com.gr.project.model.Member;
import com.gr.project.security.UserLoggedIn;
import com.gr.project.security.credential.Token;
import com.gr.project.security.rest.RegistrationRequest;


@Path("/users")
@Stateless
public class UserRestService {
	
	 @Inject
	 @Named("default.return.message.parameter")
	 private String MESSAGE_RESPONSE_PARAMETER;
	
     @Inject
     private MemberDAO repository;

     @Inject
     private IdentityManager identityManager;
     
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
     @Produces(MediaType.APPLICATION_JSON)
     public Response createMember(@NotNull RegistrationRequest request) {
    	 
    	 Map<String, Object> response = new HashMap<String, Object>();
    	 
         if (!request.getPassword().equals(request.getPasswordConfirmation())) {
             response.put(MESSAGE_RESPONSE_PARAMETER, "Password mismatch.");
         } else {
             try {
                 // if there is no user with the provided e-mail, perform registration
                 if (BasicModel.getUser(this.identityManager, request.getEmail()) == null) {
                	 
                	 String activationCode = createAccount(request);
                     
                	 // XXX handle the path better. Also add a view to redirect to in order for the activation to take place!
                     Email email = new Email("Please complete the signup", "http://localhost:8080/Project/#/activate/" + activationCode, request.getEmail());
         			 event.fire(email);
                     
                     return Response.status(Response.Status.OK).entity("ok").type(MediaType.APPLICATION_JSON_TYPE).build();
                 } else {
                     response.put(MESSAGE_RESPONSE_PARAMETER, "This username is already in use. Try another one.");
                 }
             } catch (IdentityManagementException ime) {
                 response.put(MESSAGE_RESPONSE_PARAMETER, "Oops ! Registration failed, try it later.");
             }
         }
         
         return Response.status(Response.Status.BAD_REQUEST).entity(response).type(MediaType.APPLICATION_JSON_TYPE).build();
     }
     
     
     @POST
     @Path("/activation")
     @Produces(MediaType.APPLICATION_JSON)
     public Response memberActivation(@NotNull String activationCode) {
    	 
    	 IdentityQuery<User> query = this.identityManager.createIdentityQuery(User.class);

         List<User> result = query
             .setParameter(IdentityType.QUERY_ATTRIBUTE.byName("ActivationCode"), activationCode.replaceAll("\"", ""))
             .getResultList();

         if(result == null || result.isEmpty()) {
        	 return Response.status(Response.Status.BAD_REQUEST).entity("Not Found").type(MediaType.APPLICATION_JSON_TYPE).build();
         }
         
         User user = result.get(0);
         
         if(user.isEnabled()) {
        	 return Response.status(Response.Status.BAD_REQUEST).entity("User Already Active").type(MediaType.APPLICATION_JSON_TYPE).build();
         }

         user.setEnabled(true);

         this.identityManager.update(user);

         String tokenId = UUID.randomUUID().toString();
         Token token = new Token(tokenId);

         this.identityManager.updateCredential(user, token);

         return Response.status(Response.Status.OK).entity(token).type(MediaType.APPLICATION_JSON_TYPE).build();
     }

     private String createAccount(RegistrationRequest request) {
         User newUser = new User(request.getEmail());

         newUser.setEmail(request.getEmail());
         newUser.setFirstName(request.getFirstName());
         newUser.setLastName(request.getLastName());
         newUser.setEnabled(false); // by default, user is disabled until the account is activated.

         String activationCode = UUID.randomUUID().toString();

         newUser.setAttribute(new Attribute<String>("ActivationCode", activationCode)); // we set an activation code for future use.

         this.identityManager.add(newUser);

         Password password = new Password(request.getPassword());

         this.identityManager.updateCredential(newUser, password);

         return activationCode;
     }

 }
