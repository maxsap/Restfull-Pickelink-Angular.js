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

package com.gr.project.security;

import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * <p>
 * Simple test case that shows how to use PicketLink components to test your application security capabilities.
 * </p>
 * <p>
 * This test shows you how to use the {@link IdentityManager} to manage user information and credentials and also how to
 * authenticate them using the {@link Identity} component.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class AuthenticationTestCase extends AbstractSecurityTestCase {

    private static final String USER_LOGIN_NAME = "john";
    private static final String USER_PASSWORD = "letmein";

    @Inject
    protected DefaultLoginCredentials credentials;
    
    @Test
    @InSequence(1)
    public void testCreateUser() throws Exception {
        User john = new User(USER_LOGIN_NAME);

        super.identityManager.add(john);

        assertNotNull(BasicModel.getUser(super.identityManager, john.getLoginName()));
    }

    @Test
    @InSequence(2)
    public void testPopulateCredentials() throws Exception {
        User john = BasicModel.getUser(super.identityManager, USER_LOGIN_NAME);

        assertNotNull(john);

        super.identityManager.updateCredential(john, new Password(USER_PASSWORD));
    }

    @Test
    @InSequence(3)
    public void testSuccessfulAuthentication() throws Exception {
        this.credentials.setUserId(USER_LOGIN_NAME);
        this.credentials.setCredential(new Password(USER_PASSWORD));

        assertFalse(super.identity.isLoggedIn());

        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
    }

    @Test
    @InSequence(4)
    public void testUnSuccessfulAuthentication() throws Exception {
        this.credentials.setUserId(USER_LOGIN_NAME);
        this.credentials.setCredential(new Password("letmein2"));

        super.identity.login();

        Assert.assertFalse(super.identity.isLoggedIn());
    }

    @Test
    @InSequence(5)
    public void testLogout() throws Exception {
        this.credentials.setUserId(USER_LOGIN_NAME);
        this.credentials.setCredential(new Password(USER_PASSWORD));

        super.identity.login();

        assertTrue(super.identity.isLoggedIn());

        super.identity.logout();

        assertFalse(super.identity.isLoggedIn());
    }

}