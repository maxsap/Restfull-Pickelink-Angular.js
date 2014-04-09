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

import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.jpa.model.sample.simple.AbstractCredentialTypeEntity;

import javax.persistence.Entity;

/**
 * <p>{@link javax.persistence.Entity} representing a {@link com.gr.project.security.credential.Token}.</p>
 *
 * @author Pedro Igor
 */
@ManagedCredential(TokenCredentialStorage.class)
@Entity
public class TokenCredentialTypeEntity extends AbstractCredentialTypeEntity {

	private static final long serialVersionUID = 3208265179514358055L;

	@CredentialProperty(name = "id")
    private String tokenId;

    @CredentialProperty
    private String userId;

    public String getTokenId() {
        return this.tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
