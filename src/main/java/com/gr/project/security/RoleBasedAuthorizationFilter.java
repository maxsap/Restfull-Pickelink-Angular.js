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

import java.io.IOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;
import org.picketlink.Identity;
import org.picketlink.idm.credential.TOTPCredentials;

import com.gr.project.security.rest.LoginService;
import com.gr.project.util.ThreadLocalUtils;

/**
 * <p>
 * A RBAC {@link Filter} that can be used to protected web resources based on a simple role mapping.
 * </p>
 * <p>
 * This filter accepts two params:
 * </p>
 *
 * @author Pedro Silva
 * 
 */
@WebFilter(urlPatterns = "/*")
public class RoleBasedAuthorizationFilter implements Filter {

    @Inject
    private Instance<Identity> identity;

    @Inject
    private AuthorizationManager authorizationManager;
    
    @Inject
    private LoginService loginService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // to configure which resources should be protected, see the AuthorizationManager class.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String publicToken = "6a79efce-995f-4db9-bf16-ee6d836a2626";//httpRequest.getParameter("x-authc-token");
        String appId = "f9083e6f-6ec4-46ea-9fda-b5cf35cc91a6";//httpRequest.getParameter("x-authc-appID");
        
        try {
        	// used to "Inject" the request and the response globally for the app. Also will be needed for social login
        	ThreadLocalUtils.currentRequest.set(httpRequest);
            ThreadLocalUtils.currentResponse.set(httpResponse);
            
            /*
             * Intercept all calls that include appID and PublicToken,
             * If the call is not already authenticated then try to authenticate.
             */
//            if(publicToken != null && appId != null) {
//            	if(!getIdentity().isLoggedIn()) {
//	            	TOTPCredentials credential = new TOTPCredentials();
//	            	
//	            	credential.setToken(publicToken);
//	            	credential.setUsername(appId);
//	            	
//	                this.loginService.loginWithToken(credential);
//            	}
//            }
            
            if (this.authorizationManager.isAllowed(httpRequest)) {
                performAuthorizedRequest(chain, httpRequest, httpResponse);                
            } else {
                handleUnauthorizedRequest(httpRequest, httpResponse);
            }
        } catch (AccessDeniedException ade) {
            handleUnauthorizedRequest(httpRequest, httpResponse);
        } catch (Exception e) {
            if (AccessDeniedException.class.isInstance(e.getCause())) {
                handleUnauthorizedRequest(httpRequest, httpResponse);
            } else {
                httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally{
//        	if(appId != null) {
//        		getIdentity().logout();
//        	}
            ThreadLocalUtils.currentRequest.set(null);
            ThreadLocalUtils.currentResponse.set(null);
        }
    }

    private void performAuthorizedRequest(FilterChain chain, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        chain.doFilter(httpRequest, httpResponse);
    }

    private void handleUnauthorizedRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (!getIdentity().isLoggedIn()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            handleAccessDeniedError(httpResponse);
        }
    }

    @Override
    public void destroy() {
    }

    private Identity getIdentity() {
        return this.identity.get();
    }

    private void handleAccessDeniedError(HttpServletResponse httpResponse) throws IOException {
        if (!getIdentity().isLoggedIn()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

}