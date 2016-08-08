package com.appdynamics.tool.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.appdynamics.tool.app.App;
import com.appdynamics.tool.app.Worker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("finddependencies")
public class FindDependenciesService {
	@GET
	@Produces("application/json")
	public String findDependencies(@QueryParam("issueKey") String issueKey) {
		List<String> dependentIssueKeys = new ArrayList<String>();
		String output = App.sendRequest(Worker.urlPrefix + issueKey);
		JsonObject issueJson = App.parser.parse(output).getAsJsonObject();
		JsonObject fields = issueJson.get("fields").getAsJsonObject();
		JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();
		for (int j = 0; j < issueLinks.size(); j++) {
			if (issueLinks.get(j).getAsJsonObject().has("inwardIssue") && issueLinks.get(j).getAsJsonObject().get("type").getAsJsonObject().get("inward").getAsString().equalsIgnoreCase("is caused by")) {
				dependentIssueKeys.add(issueLinks.get(j).getAsJsonObject().get("inwardIssue").getAsJsonObject().get("key").getAsString());
			}
		}
		return App.gson.toJson(dependentIssueKeys);
	}
}
