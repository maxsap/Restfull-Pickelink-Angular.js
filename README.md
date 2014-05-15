Restfull-Pickelink-Angular.js
=============================

## Synopsis

PLEASE NOTE: THIS PROJECT IS UNDER DEVELOPMENT!!!

Sample single page application authentication example.
This example demonstrates how to use PicketLink and Angular.js to secure a Restfull (JAX-RS) JEE application.

## Pre Requirements
This project is using WildFly 8 and assumes that SSL is enabled on the server and that there is a JNDI resource for mail (Using a Gmail account).

## Configuration
In order to configure SSL support on Wildfly please follow this instructions:

* Edit standalone.xml, search for **"urn:jboss:domain:undertow:1.0"**
add following listener: **https-listener name="default-https" socket-binding="https" security-realm="ssl-realm"**
	
final undertow subsystem should look like this one:

	<subsystem xmlns="urn:jboss:domain:undertow:1.0">
		<buffer-caches>
			<buffer-cache name="default" buffer-size="1024" buffers-per-region="1024" max-regions="10"/>
        </buffer-caches>
        <server name="default-server">
			<http-listener name="default" socket-binding="http"/>
            <https-listener name="default-https" socket-binding="https" security-realm="ssl-realm"/>
            <host name="default-host" alias="localhost">
				<location name="/" handler="welcome-content"/>
            </host>
        </server>
        <servlet-container name="default" default-buffer-cache="default" stack-trace-on-error="local-only">
			<jsp-config/>
            <persistent-sessions/>
        </servlet-container>
        <handlers>
			<file name="welcome-content" path="${jboss.home.dir}/welcome-content" directory-listing="true"/>
        </handlers>
    </subsystem>

	
follow instructions <a href="https://docs.jboss.org/author/display/WFLY8/Examples">found here</a> for **how to generate ssl certificate.**

* Edit standalone.xml, search for **security-realms**

Add following security-realm: 


	<security-realm name="ssl-realm">
		<server-identities>
			<ssl>
			<keystore path="server.keystore" relative-to="jboss.server.config.dir"
			keystore-password="SUPER_SECRET_PASS" alias="server" key-password="SUPER_SECRET_PASS"
			/>
			</ssl>
		</server-identities>
		<authentication>
			<local default-user="$local"/>
			<properties path="mgmt-users.properties" relative-to="jboss.server.config.dir"/>
		</authentication>
	</security-realm>
	
Final security realms should look like:

	<security-realms>
		<security-realm name="ManagementRealm">
			<authentication>
				<local default-user="$local"/>
				<properties path="mgmt-users.properties" relative-to="jboss.server.config.dir"/>
			</authentication>
			<authorization map-groups-to-roles="false">
				<properties path="mgmt-groups.properties" relative-to="jboss.server.config.dir"/>
			</authorization>
		</security-realm>
		<security-realm name="ssl-realm">
			<server-identities>
				<ssl>
					<keystore path="server.keystore" relative-to="jboss.server.config.dir" keystore-password="SUPER_SECRET_PASS" alias="server" key-password="SUPER_SECRET_PASS" />
				</ssl>
			</server-identities>
			<authentication>
				<local default-user="$local"/>
				<properties path="mgmt-users.properties" relative-to="jboss.server.config.dir"/>
			</authentication>
		</security-realm>
		<security-realm name="ApplicationRealm">
			<authentication>
				<local default-user="$local" allowed-users="*"/>
				<properties path="application-users.properties" relative-to="jboss.server.config.dir"/>
			</authentication>
			<authorization>
				<properties path="application-roles.properties" relative-to="jboss.server.config.dir"/>
			</authorization>
		</security-realm>
	</security-realms>
			

In order to configure the email JNDI resource please follow this instructions:

* Edit standalone.xml, search for **subsystem xmlns="urn:jboss:domain:mail:2.0"**
	
add following mail-session as follows:

	<mail-session name="App" jndi-name="java:/mail/gmail">
		<smtp-server outbound-socket-binding-ref="mail-smtp-gmail" ssl="true" username="YOUR_GMAIL_EMAIL" password="YOUR_GMAIL_PASSWORD"/>
	</mail-session>
			
The final mail subsystem should be:

	<subsystem xmlns="urn:jboss:domain:mail:2.0">
		<mail-session name="default" jndi-name="java:jboss/mail/Default">
			<smtp-server outbound-socket-binding-ref="mail-smtp"/>
		</mail-session>
		<mail-session name="App" jndi-name="java:/mail/gmail">
			<smtp-server outbound-socket-binding-ref="mail-smtp-gmail" ssl="true" username="YOUR_GMAIL_EMAIL" password="YOUR_GMAIL_PASSWORD"/>
		</mail-session>
	</subsystem>
			
* search for **outbound-socket-binding name="mail-smtp"**

Add the following outbound-socket-binding:

	<outbound-socket-binding name="mail-smtp-gmail">
		<remote-destination host="smtp.gmail.com" port="465"/>
	</outbound-socket-binding>
	
in the **socket-binding-group**. 
	
The final one should be like this one :

	<socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
		<socket-binding name="management-native" interface="management" port="${jboss.management.native.port:9999}"/>
		<socket-binding name="management-http" interface="management" port="${jboss.management.http.port:9990}"/>
		<socket-binding name="management-https" interface="management" port="${jboss.management.https.port:9993}"/>
		<socket-binding name="ajp" port="${jboss.ajp.port:8009}"/>
		<socket-binding name="http" port="${jboss.http.port:8080}"/>
		<socket-binding name="https" port="${jboss.https.port:8443}"/>
		<socket-binding name="txn-recovery-environment" port="4712"/>
		<socket-binding name="txn-status-manager" port="4713"/>
		<outbound-socket-binding name="mail-smtp">
			<remote-destination host="localhost" port="25"/>
		</outbound-socket-binding>
		<outbound-socket-binding name="mail-smtp-gmail">
			<remote-destination host="smtp.gmail.com" port="465"/>
		</outbound-socket-binding>
	</socket-binding-group>
