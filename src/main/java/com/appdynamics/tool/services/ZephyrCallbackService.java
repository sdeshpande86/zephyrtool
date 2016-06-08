package com.appdynamics.tool.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.appdynamics.tool.app.App;
import com.appdynamics.tool.app.Initializer;
import com.google.gson.JsonObject;

@Path("zephyrcallback")
public class ZephyrCallbackService {
	@POST
	@Consumes
	public Response getFeatures(String s) throws InterruptedException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		System.out.println(s);
		JsonObject context = App.parser.parse(s).getAsJsonObject();
		App.CONTEXT_KEY = context.get("key").getAsString();
		App.CONTEXT_CLIENT_KEY = context.get("clientKey").getAsString();
		App.CONTEXT_PUBLIC_KEY = context.get("publicKey").getAsString();
		App.CONTEXT_SHARED_SECRET = context.get("sharedSecret").getAsString();
		App.CONTEXT_SERVER_VERSION = context.get("serverVersion").getAsString();
		App.CONTEXT_PLUGINS_VERSION = context.get("pluginsVersion").getAsString();
		App.CONTEXT_BASE_URL = context.get("baseUrl").getAsString();
		App.CONTEXT_DESCRIPTION = context.get("description").getAsString();
		App.CONTEXT_EVENT_TYPE = context.get("eventType").getAsString();
		// Load data from JIRA into memory
		// Scheduling after 5 seconds since JIRA needs to get 200 status code before it accepts the JWT token
		App.shceduledExecutor.schedule(new Initializer(), 5, TimeUnit.SECONDS);
		return Response.status(200).build();
 	}
}