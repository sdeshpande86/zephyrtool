package com.appdynamics.tool.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.appdynamics.tool.app.App;
import com.appdynamics.tool.dao.Issue;

@Path("getfeatures")
public class FeaturesService {
	@GET
	@Produces("application/json")
	public String getFeatures(@QueryParam("usecase") String usecase) throws InterruptedException {
		if (usecase != null && !usecase.isEmpty()) {
			Map<String, List<Issue>> featureResultsMap = new HashMap<String, List<Issue>>();
			featureResultsMap.put(usecase, App.usecaseFeaturesMap.get(usecase));
			return App.gson.toJson(featureResultsMap);
		} else {
			return App.gson.toJson(App.usecaseFeaturesMap);
		}
 	}
}
