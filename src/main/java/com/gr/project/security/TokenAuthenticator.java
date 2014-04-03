package com.gr.project.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

@RequestScoped
public class TokenAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;
    
    @Inject IdentityManager identityManager;
    
	@Override
    public void authenticate() {
        if (this.credentials.getCredential() == null) {
        	return;
        }
        
        TOTPCredentials tokenCred = (TOTPCredentials) this.credentials.getCredential();
        
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);
        query.setParameter(User.ID, tokenCred.getUsername());
        query.setLimit(1);
        
        if(query.getResultList() != null && query.getResultList().isEmpty()) {
        	return;
        }
        
        User u = (User) query.getResultList().get(0);
        
        if(u != null) {
        	
        	if(u.getAttribute("token").getValue().equals(tokenCred.getToken())) {
        		successfulAuthentication(u);
        	}
        }
    }

    private void successfulAuthentication(User u) {
		setStatus(AuthenticationStatus.SUCCESS);
    	setAccount(u);
    }

}

