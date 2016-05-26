package com.appdynamics.tool.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.appdynamics.tool.dao.Issue;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {
	public static List<String> usecases;
	public static Map<String, List<Issue>> usecaseFeaturesMap;
	public static JsonParser parser = new JsonParser();
	public static Gson gson = new Gson();
	
	static {
		usecases = new ArrayList<String>();
		usecases.add("APM Functional");
		usecases.add("Technical Debt");
		usecaseFeaturesMap = new HashMap<String, List<Issue>>();
		for (String usecase : usecases) {
			usecaseFeaturesMap.put(usecase, new ArrayList<Issue>());
		}
	}
	
	public static String sendRequest(String url) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(url).request().header("Authorization", Creds.authorizationString).get();
		return response.readEntity(String.class);
	}
	
	public static List<Issue> getFeaturesList() {
		List<Issue> result = new ArrayList<Issue>();
		for (Map.Entry<String, List<Issue>> entry : usecaseFeaturesMap.entrySet() ) {
			result.addAll(entry.getValue());
		}
		return result;
	}
	
	public static void initialize() throws InterruptedException {
		String url = "https://singularity.jira.com/rest/api/2/search?jql=project%20%3D%20ZEP%20AND%20issuetype%20%3D%20Feature";
		String output = sendRequest(url);
		
		JsonObject json = parser.parse(output).getAsJsonObject();
		int numberOfIssues = json.get("total").getAsInt();
		System.out.println("Total Features: " + numberOfIssues);
		
		JsonArray issuesList = json.get("issues").getAsJsonArray();
		for (int i = 0; i < issuesList.size(); i++) {
			Issue feature = new Issue();
			JsonObject issue = issuesList.get(i).getAsJsonObject();
			feature.setId(issue.get("id").getAsString());
			feature.setKey(issue.get("key").getAsString());
			JsonObject fields = issue.get("fields").getAsJsonObject();
			feature.setSummary(fields.get("summary").getAsString());
			String usecase = fields.get("customfield_10520").getAsJsonObject().get("value").getAsString();
			usecaseFeaturesMap.get(usecase).add(feature);
			System.out.println(feature);
		}
		List<Issue> features = getFeaturesList();
		ExecutorService executorService = Executors.newFixedThreadPool(features.size());
		for (Issue feature : features) {
			executorService.submit(new Worker(feature));
		}
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.HOURS);
		
		System.out.println(gson.toJson(usecaseFeaturesMap));
	}
}
