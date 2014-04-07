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
package com.gr.project.security.credential;

import org.picketlink.idm.credential.handler.AbstractCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.Date;

/**
 * @author Pedro Igor
 */
@SupportsCredentials(
    credentialClass = { TokenCredential.class, Token.class },
    credentialStorage = TokenCredentialStorage.class
)
public class TokenCredentialHandler<S extends CredentialStore<?>, V extends TokenCredential, U extends Token> extends AbstractCredentialHandler<S, V, U> {

    @Override
    public void setup(S identityStore) {
        super.setup(identityStore);
        // here you can initialize the handler with any properties provided during the configuration.
    }

    @Override
    protected boolean validateCredential(CredentialStorage credentialStorage, TokenCredential credentials) {
        TokenCredentialStorage tokenStorage = (TokenCredentialStorage) credentialStorage;

        if (credentials.getToken() != null) {
            return tokenStorage.getId().equals(credentials.getToken().getId());
        }

        return false;
    }

    @Override
    protected Account getAccount(IdentityContext context, TokenCredential credentials) {
        return getAccount(context, credentials.getLoginName());
    }

    @SuppressWarnings("unchecked")
	@Override
    protected CredentialStorage getCredentialStorage(IdentityContext context, Account account, TokenCredential credentials, @SuppressWarnings("rawtypes") CredentialStore store) {
        return store.retrieveCurrentCredential(context, account, TokenCredentialStorage.class);
    }

    @Override
    public void update(IdentityContext context, Account account, Token credential, @SuppressWarnings("rawtypes") CredentialStore store, Date effectiveDate, Date expiryDate) {
        TokenCredentialStorage tokenStorage = new TokenCredentialStorage();

        tokenStorage.setId(credential.getId());

        if (effectiveDate != null) {
            tokenStorage.setEffectiveDate(effectiveDate);
        }

        tokenStorage.setExpiryDate(expiryDate);

        store.storeCredential(context, account, tokenStorage);
    }
}
