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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.TOTPCredentials;

/**
 *
 */
@RequestScoped
public class LoginService {

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    public void login(DefaultLoginCredentials credential) {
        if (!this.identity.isLoggedIn()) {

            this.credentials.setUserId(credential.getUserId());
            this.credentials.setPassword(credential.getPassword());
//            this.credentials.setCredential(credential.getCredential());

            this.identity.login();
        }

    }
    
    
    public void loginWithToken(TOTPCredentials credential) {
        if (!this.identity.isLoggedIn()) {

            this.credentials.setCredential(credential);

            this.identity.login();
        }

    }
    
}