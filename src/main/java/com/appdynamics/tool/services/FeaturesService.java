package com.appdynamics.tool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.appdynamics.tool.app.App;

@Path("/usecase")
public class FeaturesService {
	@Path("{value}")
	@GET
	@Produces("application/json")
	public String convertCtoF(@PathParam("value") String value) {
		return App.gson.toJson(App.usecaseFeaturesMap.get(value));
 	}
}
