package com.gr.project.security.service;

import com.gr.project.model.Person;
import com.gr.project.rest.MessageBuilder;
import com.gr.project.security.authorization.AllowedRole;
import com.gr.project.security.model.ApplicationRole;
import com.gr.project.security.model.IdentityModelManager;
import com.gr.project.security.model.MyUser;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@javax.ejb.Stateless
@Path("/admin")
@AllowedRole(ApplicationRole.ADMINISTRATOR)
public class AdminService {

    @Inject
    private IdentityModelManager identityModelManager;

    @POST
    @Path("activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateAccount(@NotNull Person passedUser) {
        MessageBuilder message;

        try {
            MyUser user = this.identityModelManager.findByLoginName(passedUser.getEmail());

            if (user == null) {
                return MessageBuilder.badRequest().message("Invalid activation code.").build();
            }
            
            if(user.isEnabled()) {
                return MessageBuilder.badRequest().message("User Already Active").build();
            }

            this.identityModelManager.activateAccount(user);

            message = MessageBuilder.ok().message("User is now active.");
        } catch (Exception e) {
            message = MessageBuilder.badRequest().message(e.getMessage());
        }

        return message.build();
    }
}
