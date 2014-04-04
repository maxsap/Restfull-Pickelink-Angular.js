package com.gr.project.security;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.Authenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredentials;

import com.gr.project.security.credential.TokenCredential;

/*
 * This class will allow us to plug-in more authenticator methods in the future by providing social login.
 */
public class AuthenticatorSelector {
	
	@Inject Instance<UserNamePasswordAuthenticator> userNamePassAuthenticator;
	
	@Inject Instance<TokenAuthenticator> tokenAuthenticator;
	
	@Inject
    private DefaultLoginCredentials credentials;

//	@PicketLink
//    @Produces
//    @RequestScoped
    public Authenticator chooseAuthenticator() {
//        HttpServletRequest httpServletRequest = (HttpServletRequest) ThreadLocalUtils.currentRequest.get();
//        HttpServletResponse httpServletResponse = (HttpServletResponse) ThreadLocalUtils.currentResponse.get();

        
        Authenticator authenticator = null;

        if(isCustomCredential()){
            authenticator = tokenAuthenticator.get();
        } else if(isUsernamePasswordCredential()){
        	authenticator = userNamePassAuthenticator.get();
        }
        
        return authenticator;
    }

	private boolean isUsernamePasswordCredential() {
        return Password.class.equals(credentials.getCredential().getClass()) && credentials.getUserId() != null;
    }
	
	private boolean isCustomCredential() {
        return TokenCredential.class.equals(credentials.getCredential().getClass());
    }
}
