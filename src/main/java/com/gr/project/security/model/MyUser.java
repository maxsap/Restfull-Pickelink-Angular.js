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
package com.gr.project.security.model;

import com.gr.project.model.Person;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * <p>This is a custom {@link org.picketlink.idm.model.Account} type to represent the application users.</p>
 *
 * <p>Instead of using the {@link org.picketlink.idm.model.basic.User} type provided by PicketLink the application is using its
 * own type to map users to/from the configured identity stores.</p>
 *
 * <p>This is a perfect example about how to integrate PicketLink IDM with existing models. The <code>person</code> association
 * is basically a link between application's data model with the identity model provided by PicketLink.</p>
 *
 * @author Pedro Igor
 */
public class MyUser extends AbstractIdentityType implements Account {

	private static final long serialVersionUID = 1L;

    private static final String ACTIVATION_CODE_ATTRIBUTE_NAME = "ActivationCode";

    /**
     * <p>Can be used to query users by their activation code.</p>
     */
    public static final AttributeParameter ACTIVATION_CODE = QUERY_ATTRIBUTE.byName(ACTIVATION_CODE_ATTRIBUTE_NAME);

    /**
     * <p>Can be used to query users by their login name.</p>
     */
	public static final QueryParameter USER_NAME = QUERY_ATTRIBUTE.byName("loginName");

    @AttributeProperty
    @Unique
    private String loginName;

    @AttributeProperty
    private Person person;

    public MyUser() {
        this(null);
    }

    public MyUser(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setActivationCode(String activationCode) {
        setAttribute(new Attribute<String>(ACTIVATION_CODE_ATTRIBUTE_NAME, activationCode));
    }

    public void invalidateActivationCode() {
        removeAttribute(ACTIVATION_CODE_ATTRIBUTE_NAME);
    }

    public String getActivationCode() {
        Attribute<String> attribute = getAttribute(ACTIVATION_CODE_ATTRIBUTE_NAME);

        if (attribute == null) {
            return null;
        }

        return attribute.getValue();
    }
}
