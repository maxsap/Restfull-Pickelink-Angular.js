package com.gr.project.security;

import static org.junit.Assert.assertEquals;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.security.credential.TokenCredential;

@RequestScoped
public class TokenAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;
    
    @Inject IdentityManager identityManager;
    
//	@PicketLink
//	@Override
    public void authenticate() {
        if (this.credentials.getCredential() == null) {
        	return;
        }
        
        if(isUsernamePasswordCredential()) {
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
        } else {
        	TokenCredential tokenCredential = (TokenCredential) this.credentials.getCredential();
        	
        	this.identityManager.validateCredentials(tokenCredential);
        	
        	if(tokenCredential.getStatus().equals(Credentials.Status.VALID)) {
        		// XXX find User who owns the token ???
//        		successfulAuthentication(u);
        	}
        	
        	assertEquals(Credentials.Status.VALID, tokenCredential.getStatus());
        }
    }

	private boolean isUsernamePasswordCredential() {
        return Password.class.equals(credentials.getCredential().getClass()) && credentials.getUserId() != null;
    }

	private void successfulAuthentication(User u) {
		setStatus(AuthenticationStatus.SUCCESS);
    	setAccount(u);
    }

}

