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

import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import static org.junit.Assert.*;

/**
 * <p>
 * Simple test case that shows how to use PicketLink components to test your application security capabilities.
 * </p>
 * <p>
 * This test shows you how to use the {@link IdentityManager} to manage user, role and group information and also the
 * relationship between them.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class IdentityManagementTestCase extends AbstractSecurityTestCase {

    @Test
    @InSequence(1)
    public void testCreateUser() throws Exception {
        User john = new User("john");

        john.setFirstName("John");
        john.setLastName("The Monster");
        john.setEmail("john@ticketmonster.org");
        
        super.identityManager.add(john);

        john = BasicModel.getUser(super.identityManager, john.getLoginName());
        
        assertNotNull(john);
        assertEquals("John", john.getFirstName());
        assertEquals("The Monster", john.getLastName());
        assertEquals("john@ticketmonster.org", john.getEmail());
    }

    @Test
    @InSequence(2)
    public void testCreateRole() throws Exception {
        Role administrator = new Role("Administrator");
        
        super.identityManager.add(administrator);
        
        assertNotNull(BasicModel.getRole(super.identityManager, administrator.getName()));
    }

    @Test
    @InSequence(3)
    public void testCreateGroup() throws Exception {
        Group administrators = new Group("Administrators");
        
        super.identityManager.add(administrators);
        
        assertNotNull(BasicModel.getGroup(super.identityManager, administrators.getName()));
    }

    @Test
    @InSequence(4)
    public void testAssociateUserWithRole() throws Exception {
        User john = BasicModel.getUser(super.identityManager, "john");
        Role administrator = BasicModel.getRole(super.identityManager, "Administrator");
        
        BasicModel.grantRole(super.relationshipManager, john, administrator);
        
        assertTrue(BasicModel.hasRole(super.relationshipManager, john, administrator));
    }

    @Test
    @InSequence(5)
    public void testAssociateUserWithGroup() throws Exception {
        User john = BasicModel.getUser(super.identityManager, "john");
        Group administrators = BasicModel.getGroup(super.identityManager, "Administrators");
        
        BasicModel.addToGroup(super.relationshipManager, john, administrators);
        
        assertTrue(BasicModel.isMember(super.relationshipManager, john, administrators));
    }

    @Test
    @InSequence(6)
    public void testAssociateUserWithGroupAndRole() throws Exception {
        User john = BasicModel.getUser(super.identityManager, "john");
        Role manager = new Role("Manager");
        Group sales = new Group("Sales");
        
        super.identityManager.add(manager);
        super.identityManager.add(sales);
        
        // john is now a Manager of the Sales group
        BasicModel.grantGroupRole(super.relationshipManager, john, manager, sales);
        
        assertTrue(BasicModel.hasGroupRole(super.relationshipManager, john, manager, sales));
    }

}