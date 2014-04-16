package com.gr.project.security.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.model.Person;
import com.gr.project.rest.MessageBuilder;
import com.gr.project.security.authorization.AllowedRole;
import com.gr.project.security.authorization.annotation.UserLoggedIn;
import com.gr.project.security.model.ApplicationRole;
import com.gr.project.security.model.MyUser;
import com.gr.project.util.EntityValidator;

@javax.ejb.Stateless
@Path("/admin")
@UserLoggedIn
@AllowedRole(ApplicationRole.ADMINISTRATOR)
public class AdminService {

	
    public static final String ACTIVATION_CODE_ATTRIBUTE_NAME = "ActivationCode";

    @Inject
    @Named("default.return.message.parameter")
    private String MESSAGE_RESPONSE_PARAMETER;

    @Inject
    private IdentityManager identityManager;
    
    @Inject
    private EntityValidator validator;

    
    @POST
    @Path("activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupPersonById(@NotNull Person passedUser) {
    	
        MessageBuilder message;
        try {
        	
        	// validate input
        	validator.validateEntity(passedUser);
            
        	MyUser user = findUserByUserName(passedUser.getEmail());
            
            if (user == null) {
                return MessageBuilder.badRequest().message("Invalid activation code.").build();
            }
            
            if(user.isEnabled()) {
                return MessageBuilder.badRequest().message("User Already Active").build();
            }

            user.setEnabled(true);
            user.removeAttribute(ACTIVATION_CODE_ATTRIBUTE_NAME);

            message = MessageBuilder.ok().message("User Already Active");
        } catch (Exception e) {
            message = MessageBuilder.badRequest().message(e.getMessage());
        }

        return message.build();
    }
    
    
    private MyUser findUserByUserName(String activationCode) {
        if (activationCode == null) {
            throw new IllegalArgumentException("Invalid activation code.");
        }

        IdentityQuery<MyUser> query = this.identityManager.createIdentityQuery(MyUser.class);
        List<MyUser> result = query
            .setParameter(MyUser.USER_NAME, activationCode.replaceAll("\"", ""))
            .getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }
}
