package com.appdynamics.tool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.appdynamics.tool.app.App;

@Path("getusecases")
public class UsecasesService {
	@GET
	@Produces("application/json")
	public String getFeatures() throws InterruptedException {
		return App.gson.toJson(App.usecases);
 	}
}
