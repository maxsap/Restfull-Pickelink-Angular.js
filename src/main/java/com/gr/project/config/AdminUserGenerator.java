package com.gr.project.config;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.IdentityQuery;

import com.gr.project.model.Person;
import com.gr.project.security.authentication.TokenManager;
import com.gr.project.security.authentication.credential.Token;
import com.gr.project.security.model.ApplicationRole;
import com.gr.project.security.model.MyUser;


/**
 * <p>
 * Generates the initial Admin User.
 * </p>
 * 
 * @author Maximos Sapranidis
 * 
 */

@Singleton
@Startup
public class AdminUserGenerator {
	
	@Inject
    private PartitionManager partitionManager;
	
	@Inject
    private TokenManager tokenManager;
	
	@Inject
    @Named("ACTIVATION_CODE_ATTRIBUTE_NAME")
    private String ACTIVATION_CODE_ATTRIBUTE_NAME;

    @PostConstruct
    public void initialize() {
        IdentityManager identityManager = this.partitionManager.createIdentityManager();
        
        // check if admin exists
        IdentityQuery<MyUser> query = identityManager.createIdentityQuery(MyUser.class);
        query.setParameter(MyUser.USER_NAME, "admin@project.com");
        query.setLimit(1);
        
        if(query.getResultList() != null && !query.getResultList().isEmpty()) {
        	return;
        }
        
    	Person person = new Person();

        person.setEmail("admin@project.com");
        person.setFirstName("Admin");

        MyUser newUser = new MyUser(person.getEmail());

        newUser.setPerson(person);
        newUser.setEnabled(true); 


        identityManager.add(newUser);

        Password password = new Password("adminadmin");

        identityManager.updateCredential(newUser, password);
        
        
        // Assign Role
        Role adminRole = new Role(ApplicationRole.ADMINISTRATOR.toString());

        // stores the admin role
        identityManager.add(adminRole);

        // Assign Group
        Group adminGroup = new Group("Administrators");

        // stores the admin group
        identityManager.add(adminGroup);

        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        // grants to the admin user the admin role
        BasicModel.grantRole(relationshipManager, newUser, adminRole);
        
        // add the admin user to the admin group
        BasicModel.addToGroup(relationshipManager, newUser, adminGroup);
        
        Role userRole = new Role("User");
        
        identityManager.add(userRole);
        
        Group usersGroup = new Group("Users");
        
        identityManager.add(usersGroup);
        
        newUser.removeAttribute(ACTIVATION_CODE_ATTRIBUTE_NAME);

        identityManager.update(newUser);

        Token token = this.tokenManager.issue(newUser);

        identityManager.updateCredential(newUser, token);
            
    }

}
