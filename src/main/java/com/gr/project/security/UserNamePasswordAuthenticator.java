/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gr.project.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>A simple authenticator that supports two credential types: username/password or a simple token.</p>
 */
@RequestScoped
public class UserNamePasswordAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;
    
    @Inject IdentityManager identityManager;
    
	@Override
    public void authenticate() {
        if (this.credentials.getCredential() == null) {
        	return;
        }
        
        
    	IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);
        query.setParameter(User.LOGIN_NAME, this.credentials.getUserId());
        query.setLimit(1);
        
        if(query.getResultList() != null && query.getResultList().isEmpty()) {
        	return;
        }
        
        User u = (User) query.getResultList().get(0);
        
        if(u != null) {
        	UsernamePasswordCredentials upassCred = new UsernamePasswordCredentials();
        	
        	Password password = (Password) this.credentials.getCredential();

        	upassCred.setUsername(this.credentials.getUserId());

        	upassCred.setPassword(password);
        	
        	if (String.valueOf(password.getValue()).equals(String.valueOf(upassCred.getPassword().getValue()))) {
        		successfulAuthentication(u);
        	}
        }
    }

    private void successfulAuthentication(User u) {
		setStatus(AuthenticationStatus.SUCCESS);
    	setAccount(u);
    }

}
