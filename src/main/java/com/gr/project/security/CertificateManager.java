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

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Realm;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.security.KeyStore;

/**
 * @author Pedro Igor
 */
@Singleton
@Startup
public class CertificateManager {

    public static final String KEYSTORE_FILE_PATH = "/keystore.jks";
    private KeyStore keyStore;

    @Inject
    private PartitionManager partitionManager;

    @Inject
    @PostConstruct
    public void initCertificates() {
        initKeyStore();
        initCertificatesForPartition();
    }

    private void initCertificatesForPartition() {
        Realm partition = getDefaultPartition();

        try {
            partition.setAttribute(new Attribute<byte[]>("PublicKey", this.keyStore.getCertificate("servercert").getPublicKey().getEncoded()));
            partition.setAttribute(new Attribute<byte[]>("PrivateKey", this.keyStore.getKey("servercert", "test123".toCharArray()).getEncoded()));

            partitionManager.update(partition);
        } catch (Exception e) {
            throw new SecurityConfigurationException("Could not create default partition.", e);
        }
    }

    private void initKeyStore() {
        try {
            this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            this.keyStore.load(getClass().getResourceAsStream(KEYSTORE_FILE_PATH), "store123".toCharArray());
        } catch (Exception e) {
            throw new SecurityException("Could not load key store.", e);
        }
    }



    private Realm getDefaultPartition() {
        return partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);
    }
}
