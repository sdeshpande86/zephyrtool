package com.appdynamics.tool.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.appdynamics.tool.app.App;

@Path("updatedata")
public class UpdateDataService {
	@GET
	@Produces("application/json")
	public synchronized String updateData() throws InterruptedException {
		App.usecases.clear();
		App.usecaseFeaturesMap.clear();
		App.initialize();
		return "Updated data successfully";
 	}
}
