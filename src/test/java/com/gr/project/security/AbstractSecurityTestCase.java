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

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;

import com.gr.project.test.AntitheftDeployment;

/**
 * @author Pedro Silva
 *
 */
@RunWith(Arquillian.class)
public abstract class AbstractSecurityTestCase {

    @Deployment
    public static WebArchive deployment() {
        WebArchive deployment = AntitheftDeployment.deployment();

//FIXME: shrinkwrap dependencies
//        deployment
//            .addPackage(AbstractSecurityTestCase.class.getPackage())
//            .addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
//                    .loadMetadataFromPom("pom.xml")
//                        .artifact("org.picketlink:picketlink-core-impl").artifact("org.picketlink:picketlink-idm-schema")
//                        .artifact("org.infinispan:infinispan-core").scope("test")
//                        .resolveAsFiles());

        return deployment;
    }

    @Inject
    protected Identity identity;

    @Inject
    protected IdentityManager identityManager;

    @Inject
    protected RelationshipManager relationshipManager;

    @Inject
    protected UserTransaction userTransaction;
    
    @Before
    public void onInit() throws Exception {
        this.userTransaction.begin();
    }

    @After
    public void onFinish() throws Exception {
        this.userTransaction.commit();
    }
    
}
