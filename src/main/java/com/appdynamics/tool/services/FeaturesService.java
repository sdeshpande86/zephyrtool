package com.appdynamics.tool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.appdynamics.tool.app.App;

@Path("getfeatures")
public class FeaturesService {
	@GET
	@Produces("application/json")
	public String getFeatures(@QueryParam("usecase") String usecase) throws InterruptedException {
		if (usecase != null && !usecase.isEmpty()) {
			return App.gson.toJson(App.usecaseFeaturesMap.get(usecase));
		} else {
			return App.gson.toJson(App.usecaseFeaturesMap);
		}
 	}
}
