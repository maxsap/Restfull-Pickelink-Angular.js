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

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.security.credential.TokenCredentialHandler;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * <p>
 * Performs some initialization during the startup in order to populate the underlying identity stores with some data.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
@Singleton
@Startup
public class IdentityManagementInitializer {

    private PartitionManager partitionManager;
    private IdentityManager identityManager;

    @PostConstruct
    public void initialize() {
    	this.partitionManager = createPartitionManager();

        this.partitionManager.add(new Realm(Realm.DEFAULT_REALM)); // we need a single partition, so let's create a default.

        this.identityManager = this.partitionManager.createIdentityManager();
        
        
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);
        query.setParameter(User.LOGIN_NAME, "admin@ticketmonster.org");
        query.setLimit(1);
        
        if(query.getResultList() != null && query.getResultList().isEmpty()) {
        	return;
        }
        
        User u = (User) query.getResultList().get(0);
        
        if(u == null) {
        	
        	 User admin = new User("admin@ticketmonster.org");

             admin.setFirstName("Almight");
             admin.setLastName("Administrator");
             
             // let's store the admin user
             identityManager.add(admin);

             Password password = new Password("letmein!");

             // updates the admin password
             identityManager.updateCredential(admin, password);

             Role adminRole = new Role("Administrator");

             // stores the admin role
             identityManager.add(adminRole);

             Group adminGroup = new Group("Administrators");

             // stores the admin group
             identityManager.add(adminGroup);

             RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

             // grants to the admin user the admin role
             BasicModel.grantRole(relationshipManager, admin, adminRole);
             
             // add the admin user to the admin group
             BasicModel.addToGroup(relationshipManager, admin, adminGroup);
             
             Role userRole = new Role("User");
             
             identityManager.add(userRole);
             
             Group usersGroup = new Group("Users");
             
             identityManager.add(usersGroup);
        	
        }
    }
    
    
    private PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("test.config")
                .stores()
                    .file()
                        .addCredentialHandler(TokenCredentialHandler.class)
                        .preserveState(false) // we always reset data during tests.
                        .supportAllFeatures();

        return new DefaultPartitionManager(builder.buildAll());
    }

}
