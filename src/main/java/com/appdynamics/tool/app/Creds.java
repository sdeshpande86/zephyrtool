package com.appdynamics.tool.app;

import org.glassfish.jersey.internal.util.Base64;

public class Creds {
	public static String unEncodedAuthString;
	public static String authorizationString;
	static {
		unEncodedAuthString = "<jira username>:<jira password>";
        authorizationString = "Basic " + new String(Base64.encode(unEncodedAuthString.getBytes()));
	}
}
