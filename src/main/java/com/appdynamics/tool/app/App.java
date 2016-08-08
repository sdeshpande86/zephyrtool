package com.appdynamics.tool.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.appdynamics.tool.dao.Issue;
import com.appdynamics.tool.jwt.JwtBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {
	public static String CONTEXT_KEY;
	public static String CONTEXT_CLIENT_KEY;
	public static String CONTEXT_PUBLIC_KEY;
	public static String CONTEXT_SHARED_SECRET;
	public static String CONTEXT_SERVER_VERSION;
	public static String CONTEXT_PLUGINS_VERSION;
	public static String CONTEXT_BASE_URL;
	public static String CONTEXT_PRODUCT_TYPE;
	public static String CONTEXT_DESCRIPTION;
	public static String CONTEXT_EVENT_TYPE;
	
	public static List<String> usecases = new ArrayList<String>();
	public static Map<String, String> usecaseValueToIdMap = new HashMap<String, String>();
	public static Map<String, List<Issue>> usecaseFeaturesMap = new HashMap<String, List<Issue>>();
	public static Map<String, String> hierarchyUpdateMap = new HashMap<String, String>();
	public static JsonParser parser = new JsonParser();
	public static Gson gson = new Gson();
	public static ExecutorService hierarchyExecutorService = Executors.newFixedThreadPool(5);
	public static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
	
	public static List<Issue> getFeaturesList() {
		List<Issue> result = new ArrayList<Issue>();
		for (Map.Entry<String, List<Issue>> entry : usecaseFeaturesMap.entrySet() ) {
			result.addAll(entry.getValue());
		}
		return result;
	}
	
	public static void getUsecases(String sampleIssueKey) {
		String output = sendRequestNew("/rest/api/2/issue/" + sampleIssueKey + "/editmeta");
		JsonObject issueFieldsJson = parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();
		JsonArray usecaseValuesJson = issueFieldsJson.get("customfield_16420").getAsJsonObject().get("allowedValues").getAsJsonArray(); 
		for (int i=0; i<usecaseValuesJson.size(); i++) {
			JsonObject usecaseJson = usecaseValuesJson.get(i).getAsJsonObject();
			usecaseValueToIdMap.put(usecaseJson.get("value").getAsString(), usecaseJson.get("id").getAsString());
			usecases.add(usecaseJson.get("value").getAsString());
		}
		for (String usecase : usecases) {
			usecaseFeaturesMap.put(usecase, new ArrayList<Issue>());
		}
		System.out.println(usecaseValueToIdMap);
		System.out.println(usecases);
	}
	
	public static void initialize() throws InterruptedException {
		String output = sendGETRequest("/rest/api/2/search", "jql=project%20%3D%20ZEP%20AND%20issuetype%20in%20(Feature%2C%20Subcategory)");

		JsonObject json = parser.parse(output).getAsJsonObject();
		float numberOfIssues = json.get("total").getAsFloat();
		System.out.println("Total Features: " + numberOfIssues);
		
		for (int s = 0; s < Math.ceil(numberOfIssues / 50); s++) {
			output = sendGETRequest("/rest/api/2/search", "jql=project%20%3D%20ZEP%20AND%20issuetype%20in%20(Feature%2C%20Subcategory)&startAt=" + s * 50);
			json = parser.parse(output).getAsJsonObject();
			JsonArray issuesList = json.get("issues").getAsJsonArray();
			for (int i = 0; i < issuesList.size(); i++) {
				boolean isRoot = true;
				
				Issue feature = new Issue();
				JsonObject issue = issuesList.get(i).getAsJsonObject();
				feature.setId(issue.get("id").getAsString());
				feature.setKey(issue.get("key").getAsString());
				JsonObject fields = issue.get("fields").getAsJsonObject();
				feature.setSummary(fields.get("summary").getAsString());
				String usecase = fields.get("customfield_16420").getAsJsonObject().get("value").getAsString();
				
				// Construct list of usecases by calling editmeta for the first feature issue
				if (i == 0) {
					getUsecases(feature.getKey());
				}
				
				// Ignore features and sub categories having parents
				String featureDetails = App.sendRequestNew("/rest/api/2/issue/" + issue.get("key").getAsString());
				JsonObject featureDetailsJson = App.parser.parse(featureDetails).getAsJsonObject();
				JsonObject featureFields = featureDetailsJson.get("fields").getAsJsonObject();
				JsonArray featureIssueLinks = featureFields.get("issuelinks").getAsJsonArray();
				for (int j = 0; j < featureIssueLinks.size(); j++) {
					if(featureIssueLinks.get(j).getAsJsonObject().has("outwardIssue")) {
						System.out.println("This feature/subcategory " + feature.getKey() + " is not a root");
						isRoot = false;
						break;
					}
				}
				
				if (isRoot) {
					usecaseFeaturesMap.get(usecase).add(feature);
					System.out.println(feature);
				}
			}
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
	
	public static String sendRequestNew(String path) {
		return sendGETRequest(path, "");
	}
	
	public static String sendGETRequest(String path, String additionalParams) {
		String requestUrl = App.CONTEXT_BASE_URL + path;
		String canonicalUrl = "GET&" + path + "&" + additionalParams;
		String key = App.CONTEXT_KEY; // from the add-on descriptor and received during installation handshake
		String sharedSecret = App.CONTEXT_SHARED_SECRET; // received during installation Handshake
		String jwtToken = null;
		try {
			jwtToken = JwtBuilder.generateJWTToken(requestUrl, canonicalUrl, key, sharedSecret);
		} catch (Exception e) {
			System.out.println("Exception while generating JWT token");
		}
		String restAPIUrl = null;
		restAPIUrl = requestUrl + "?jwt=" + jwtToken;
		if (!additionalParams.isEmpty()) {
			restAPIUrl = restAPIUrl + "&" + additionalParams;
		}
		System.out.println(restAPIUrl);
		Client client = ClientBuilder.newClient();
		Response response = client.target(restAPIUrl).request().get();
		return response.readEntity(String.class);
	}
	
	public static void sendHierarchyUpdatePUTRequest(String path, String additionalParams, Entity<?> entity) {
		String requestUrl = App.CONTEXT_BASE_URL + path;
		String canonicalUrl = "PUT&" + path + "&" + additionalParams;
		String key = App.CONTEXT_KEY; // from the add-on descriptor and received during installation handshake
		String sharedSecret = App.CONTEXT_SHARED_SECRET; // received during installation Handshake
		String jwtToken = null;
		try {
			jwtToken = JwtBuilder.generateJWTToken(requestUrl, canonicalUrl, key, sharedSecret);
		} catch (Exception e) {
			System.out.println("Exception while generating JWT token");
		}
		String restAPIUrl = null;
		restAPIUrl = requestUrl + "?jwt=" + jwtToken;
		if (!additionalParams.isEmpty()) {
			restAPIUrl = restAPIUrl + "&" + additionalParams;
		}
		System.out.println(restAPIUrl);
		Client client = ClientBuilder.newClient();
		client.target(restAPIUrl).request().put(entity);
	}
	
	public static Response sendAttachmentUploadPOSTRequest(String path, String additionalParams, Entity<?> entity) {
		String requestUrl = App.CONTEXT_BASE_URL + path;
		String canonicalUrl = "POST&" + path + "&" + additionalParams;
		String key = App.CONTEXT_KEY; // from the add-on descriptor and received during installation handshake
		String sharedSecret = App.CONTEXT_SHARED_SECRET; // received during installation Handshake
		String jwtToken = null;
		try {
			jwtToken = JwtBuilder.generateJWTToken(requestUrl, canonicalUrl, key, sharedSecret);
		} catch (Exception e) {
			System.out.println("Exception while generating JWT token");
		}
		String restAPIUrl = null;
		restAPIUrl = requestUrl + "?jwt=" + jwtToken;
		if (!additionalParams.isEmpty()) {
			restAPIUrl = restAPIUrl + "&" + additionalParams;
		}
		System.out.println(restAPIUrl);
		Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
		return client.target(restAPIUrl).request().header("X-Atlassian-Token", "no-check").post(entity);
	}
}
