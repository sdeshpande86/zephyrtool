package com.appdynamics.tool.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.appdynamics.tool.app.App;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("finddependencies")
public class FindDependenciesService {
	@GET
	@Produces("application/json")
	public String findDependencies(@QueryParam("issueKey") String issueKey) {
		Set<String> dependentIssueKeys = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> intermediateSet = new HashSet<String>();
		intermediateSet.add(issueKey);
		getChildren(dependentIssueKeys, intermediateSet);
		dependentIssueKeys.remove(issueKey);
		return App.gson.toJson(dependentIssueKeys);
	}
	
	private void getChildren(Set<String> dependentIssueKeys, Set<String> intermediateSet) {
		for (String issueKey : intermediateSet) {
			Set<String> temp = new HashSet<String>();
			dependentIssueKeys.add(issueKey);
			String output = App.sendRequestNew("/rest/api/2/issue/" + issueKey);
			JsonObject issueJson = App.parser.parse(output).getAsJsonObject();
			JsonObject fields = issueJson.get("fields").getAsJsonObject();
			JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();
			for (int j = 0; j < issueLinks.size(); j++) {
				if (issueLinks.get(j).getAsJsonObject().has("outwardIssue") && issueLinks.get(j).getAsJsonObject().get("type").getAsJsonObject().get("outward").getAsString().equalsIgnoreCase("blocks")) {
					String dependentIssueKey = issueLinks.get(j).getAsJsonObject().get("outwardIssue").getAsJsonObject().get("key").getAsString();
					if (!dependentIssueKeys.contains(dependentIssueKey)) {
						temp.add(dependentIssueKey);
					}
				}
			}
			getChildren(dependentIssueKeys, temp);
		}
	}
}
