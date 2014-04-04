package com.gr.project.security;

import static org.junit.Assert.assertEquals;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.basic.User;

import com.gr.project.security.credential.TokenCredential;

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
        
        TokenCredential tokenCredential = (TokenCredential) this.credentials.getCredential();

        this.identityManager.validateCredentials(tokenCredential);

        assertEquals(Credentials.Status.VALID, tokenCredential.getStatus());

    }

    private void successfulAuthentication(User u) {
		setStatus(AuthenticationStatus.SUCCESS);
    	setAccount(u);
    }

}

