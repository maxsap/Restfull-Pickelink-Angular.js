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
package com.gr.project.test;

import com.gr.project.security.credential.Token;
import com.gr.project.security.credential.TokenCredential;
import com.gr.project.security.credential.TokenCredentialHandler;
import com.gr.project.security.rest.RegistrationRequest;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public class UserRegistrationTestCase {

    private PartitionManager partitionManager;
    private IdentityManager identityManager;

    @Before
    public void onSetup() {
        this.partitionManager = createPartitionManager();

        this.partitionManager.add(new Realm(Realm.DEFAULT_REALM)); // we need a single partition, so let's create a default.

        this.identityManager = this.partitionManager.createIdentityManager();
    }

    @Test
    public void testRegistrationWorkFlow() {
        RegistrationRequest request = createRegistrationRequest();

        // create an account and generate a activation code. The accound is disabled until the activation code is used to enable it.
        String activationCode = createAccount(request);

        // we need the activation code to enable the account later
        assertNotNull(activationCode);

        User disabledAccount = BasicModel.getUser(this.identityManager, request.getEmail());

        // make sure the account was properly created and disabled
        assertNotNull(disabledAccount);
        assertFalse(disabledAccount.isEnabled());

        String tokenId = activateAccount(activationCode);

        assertNotNull(tokenId);

        User enabledAccount = BasicModel.getUser(this.identityManager, request.getEmail());

        // make sure the account was properly created and enabled
        assertNotNull(enabledAccount);
        assertTrue(enabledAccount.isEnabled());

        Token token = new Token();

        token.setId(tokenId);
        token.setUserId(enabledAccount.getId());

        TokenCredential tokenCredential = new TokenCredential(token);

        this.identityManager.validateCredentials(tokenCredential);

        assertEquals(Credentials.Status.VALID, tokenCredential.getStatus());

        Token invalidToken = new Token();

        invalidToken.setId("13323");
        invalidToken.setUserId(enabledAccount.getId());

        TokenCredential invalidCredential = new TokenCredential(invalidToken);

        this.identityManager.validateCredentials(invalidCredential);

        assertEquals(Credentials.Status.INVALID, invalidCredential.getStatus());
    }

    private String activateAccount(String activationCode) {
        IdentityQuery<User> query = this.identityManager.createIdentityQuery(User.class);

        List<User> result = query
            .setParameter(IdentityType.QUERY_ATTRIBUTE.byName("ActivationCode"), activationCode)
            .getResultList();

        // make sure we found the user associated with the given activation code
        assertFalse(result.isEmpty());

        User user = result.get(0);

        user.setEnabled(true);

        this.identityManager.update(user);

        String tokenId = UUID.randomUUID().toString();
        Token token = new Token();

        token.setId(tokenId);
        token.setUserId(user.getId());

        this.identityManager.updateCredential(user, token);

        return tokenId;
    }

    private String createAccount(RegistrationRequest request) {
        User newUser = new User(request.getEmail());

        newUser.setEmail(request.getEmail());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEnabled(false); // by default, user is disabled until the account is activated.

        String activationCode = "123456";

        newUser.setAttribute(new Attribute<String>("ActivationCode", activationCode)); // we set an activation code for future use.

        this.identityManager.add(newUser);

        Password password = new Password(request.getPassword());

        this.identityManager.updateCredential(newUser, password);

        return activationCode;
    }

    private RegistrationRequest createRegistrationRequest() {
        RegistrationRequest registrationRequest = new RegistrationRequest();

        registrationRequest.setFirstName("Maximos");
        registrationRequest.setLastName("Maximos");
        registrationRequest.setEmail("maxsap@maxsap.com");
        registrationRequest.setPassword("password");

        return registrationRequest;
    }

    private PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("test.config")
                .stores()
                    .file()
                        .addCredentialHandler(TokenCredentialHandler.class)
                        .preserveState(false) // we always reset data during tests.
                        .supportAllFeatures();

        return new DefaultPartitionManager(builder.buildAll());
    }
}
