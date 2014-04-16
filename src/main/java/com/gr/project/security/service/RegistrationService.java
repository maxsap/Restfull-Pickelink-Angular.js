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
package com.gr.project.security.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.model.Email;
import com.gr.project.model.Person;
import com.gr.project.rest.MessageBuilder;
import com.gr.project.security.authentication.TokenManager;
import com.gr.project.security.authentication.credential.Token;
import com.gr.project.security.model.ApplicationRole;
import com.gr.project.security.model.MyUser;
import com.gr.project.security.model.Registration;

/**
 * <p>RESTFul endpoint responsible for:</p>
 *
 * <ul>
 *     <li>Create a new user account and send a notification with the activation code.</li>
 *     <li>Activate a previously created account based on a activation code..</li>
 * </ul>
 *
 * <p>
 *  After a successful registration, an account is always disabled. In order to enable the account and be able to log in,
 *  the activation code must be used to invoke the <code>activateAccount</code> resource.
 * </p>
 *
 * @author Pedro Igor
 */
@Stateless
@Path("/register")
public class RegistrationService {

    @Inject
    @Named("ACTIVATION_CODE_ATTRIBUTE_NAME")
    private String ACTIVATION_CODE_ATTRIBUTE_NAME;

    @Inject
    @Named("default.return.message.parameter")
    private String MESSAGE_RESPONSE_PARAMETER;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private TokenManager tokenManager;
    
    @Inject
    private PartitionManager partitionManager;

    @Inject
    @Any
    private Event<Email> event;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMember(@NotNull Registration request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            return MessageBuilder.badRequest().message("Password mismatch.").build();
        }

        MessageBuilder message;

        try {
            // if there is no user with the provided e-mail, perform registration
            if (findUserByLoginName(request.getEmail()) == null) {
                String activationCode = createAccount(request);

                sendNotification(request, activationCode);

                message = MessageBuilder.ok().activationCode(activationCode);
            } else {
                message = MessageBuilder.badRequest().message("This username is already in use. Try another one.");
            }
        } catch (Exception e) {
            message = MessageBuilder.badRequest().message(e.getMessage());
        }

        return message.build();
    }

    @POST
    @Path("/activation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateAccount(@NotNull String activationCode) {
        MessageBuilder message;

        try {
            MyUser user = findUserByActivationCode(activationCode);

            if (user == null) {
                return MessageBuilder.badRequest().message("Invalid activation code.").build();
            }

            user.setEnabled(true);
            user.removeAttribute(ACTIVATION_CODE_ATTRIBUTE_NAME);

            this.identityManager.update(user);

            Token token = this.tokenManager.issue(user);

            this.identityManager.updateCredential(user, token);

            message = MessageBuilder.ok().token(token.getToken());
        } catch (Exception e) {
            message = MessageBuilder.badRequest().message(e.getMessage());
        }

        return message.build();
    }

    private String createAccount(Registration request) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Insuficient information.");
        }

        Person person = new Person();

        person.setEmail(request.getEmail());
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());

        MyUser newUser = new MyUser(request.getEmail());

        newUser.setPerson(person);
        newUser.setEnabled(false); // by default, user is disabled until the account is activated.

//        String activationCode = UUID.randomUUID().toString();
        String activationCode = "12345"; // testing purposes

        newUser.setAttribute(new Attribute<String>(ACTIVATION_CODE_ATTRIBUTE_NAME, activationCode)); // we set an activation code for future use.

        this.identityManager.add(newUser);

        Password password = new Password(request.getPassword());

        this.identityManager.updateCredential(newUser, password);
        
        // Assign a group to the newly created user
        
        Role userRole = new Role(ApplicationRole.USER.toString());


        Group userGroup = new Group("Users");


        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        // grants to the user user the user role
        BasicModel.grantRole(relationshipManager, newUser, userRole);
        
        // add the user to the user group
        BasicModel.addToGroup(relationshipManager, newUser, userGroup);
        
        return activationCode;
    }

    public MyUser findUserByActivationCode(String activationCode) {
        if (activationCode == null) {
            throw new IllegalArgumentException("Invalid activation code.");
        }

        IdentityQuery<MyUser> query = this.identityManager.createIdentityQuery(MyUser.class);
        List<MyUser> result = query
            .setParameter(IdentityType.QUERY_ATTRIBUTE.byName(ACTIVATION_CODE_ATTRIBUTE_NAME), activationCode.replaceAll("\"", ""))
            .getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    private MyUser findUserByLoginName(String loginName) {
        if (loginName == null) {
            throw new IllegalArgumentException("Invalid login name.");
        }

        IdentityQuery<MyUser> query = this.identityManager.createIdentityQuery(MyUser.class);

        query.setParameter(MyUser.USER_NAME, loginName);

        List<MyUser> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    private void sendNotification(Registration request, String activationCode) {
        Email email = new Email("Please complete the signup", "http://localhost:8080/Project/#/activate/" + activationCode, request.getEmail());

        event.fire(email);
    }
}
