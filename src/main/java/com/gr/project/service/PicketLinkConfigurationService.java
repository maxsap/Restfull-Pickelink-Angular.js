package com.gr.project.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;

import com.gr.project.security.credential.TokenCredentialHandler;

@ApplicationScoped
public class PicketLinkConfigurationService {

  @Produces
  public IdentityConfiguration produceJPAConfiguration() {
	  
	  IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

      builder
          .named("test.config")
              .stores()
                  .file()
                      .addCredentialHandler(TokenCredentialHandler.class)
                      .preserveState(false) // we always reset data during tests.
                      .supportAllFeatures();

      return builder.build();

  }
}
