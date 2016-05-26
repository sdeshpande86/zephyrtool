package com.appdynamics.tool.app;

import org.glassfish.jersey.internal.util.Base64;

public class Creds {
	public static String unEncodedAuthString;
	public static String authorizationString;
	static {
		unEncodedAuthString = "prudhvi.chaganti@appdynamics.com:<dummy>";
        authorizationString = "Basic " + new String(Base64.encode(unEncodedAuthString.getBytes()));
	}
}
