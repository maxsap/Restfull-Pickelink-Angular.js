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
import org.picketlink.Identity.Stateless;

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
public class AuthorizationFilter implements Filter {

    @Inject
    @Stateless
    private Identity identity;
    
    @Inject
    private AuthorizationManager authorizationManager;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // to configure which resources should be protected, see the AuthorizationManager class.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        
        try {
        	ThreadLocalUtils.currentRequest.set(httpRequest);
            ThreadLocalUtils.currentResponse.set(httpResponse);
            
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
            ThreadLocalUtils.currentRequest.set(null);
            ThreadLocalUtils.currentResponse.set(null);
        }
    }

    private void performAuthorizedRequest(FilterChain chain, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        chain.doFilter(httpRequest, httpResponse);
    }

    private void handleUnauthorizedRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (!this.identity.isLoggedIn()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            handleAccessDeniedError(httpResponse);
        }
    }

    @Override
    public void destroy() {
    }

    private void handleAccessDeniedError(HttpServletResponse httpResponse) throws IOException {
        if (!this.identity.isLoggedIn()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

}