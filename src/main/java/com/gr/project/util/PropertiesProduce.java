package com.gr.project.util;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class PropertiesProduce {

	@Produces @Named("default.encoding")
	public String getDefaultEncoding() {
		return "UTF-8";
	}
}
