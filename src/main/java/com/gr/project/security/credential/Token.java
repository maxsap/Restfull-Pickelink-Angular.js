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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;

/**
 * <p>This class represents the concept of a token. For now we're using a String-based token.</p>
 *
 * <p>The reason why we have a specific type is to support future changes to the token structure, so we can
 * store more data related with it.</p>
 *
 * @author Pedro Igor
 */
@XmlRootElement
public class Token {

    private String userId;
    private String id;

    public static Token fromRequest(HttpServletRequest request) {
        Token token = null;
        String header = request.getHeader("x-session-token");

        if (header != null) {
            JsonReader reader = null;

            try {
                reader = Json.createReader(new StringReader(header));
                JsonObject json = reader.readObject();

                token = new Token();

                token.setId(json.getString("id"));
                token.setUserId(json.getString("userId"));
            } finally {
                reader.close();
            }
        }

        return token;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
