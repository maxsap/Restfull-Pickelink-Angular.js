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

import com.gr.project.rest.MessageBuilder;
import com.gr.project.security.authentication.credential.TokenCredentialStorage;
import org.picketlink.Identity;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Account;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@javax.ejb.Stateless
@Path("/authc")
public class AuthenticationService {

    @Inject
    @Named("default.return.message.parameter")
    private String MESSAGE_RESPONSE_PARAMETER;

    @Inject
    @Identity.Stateless
    private Identity identity;

    @Inject
    private IdentityManager identityManager;

    @Inject
    private DefaultLoginCredentials credentials;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@NotNull DefaultLoginCredentials credential) {
        try {
            if (!this.identity.isLoggedIn()) {
                this.credentials.setUserId(credential.getUserId());
                this.credentials.setPassword(credential.getPassword());

                this.identity.login();
            }

            Account account = this.identity.getAccount();

            if (account != null) {
                return returnToken(account);
            }

            return MessageBuilder.badRequest().message("Invalid credentials.").build();
        } catch (LockedAccountException laex) {
            return MessageBuilder.badRequest().message("Your account is not activated. Check your email for the activation code.").build();
        } catch (Exception ex) {
            return MessageBuilder.badRequest().message("Unexpected error while authenticating.").build();
        }
    }

    private Response returnToken(Account account) {
        TokenCredentialStorage credentialStorage = this.identityManager.retrieveCurrentCredential(account, TokenCredentialStorage.class);
        return MessageBuilder.ok().token(credentialStorage.getToken()).build();
    }
}