package com.appdynamics.tool.app;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import javax.ws.rs.core.Response;

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
	public static Map<String, List<Issue>> usecaseFeaturesMap = new HashMap<String, List<Issue>>();
	public static JsonParser parser = new JsonParser();
	public static Gson gson = new Gson();
	public static ScheduledExecutorService shceduledExecutor = Executors.newScheduledThreadPool(1);
	
	public static List<Issue> getFeaturesList() {
		List<Issue> result = new ArrayList<Issue>();
		for (Map.Entry<String, List<Issue>> entry : usecaseFeaturesMap.entrySet() ) {
			result.addAll(entry.getValue());
		}
		return result;
	}
	
	public static void getUsecases(String sampleIssueKey) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		String output = sendRequest("/rest/api/2/issue/" + sampleIssueKey + "/editmeta");
		JsonObject issueFieldsJson = parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();
		JsonArray usecaseValuesJson = issueFieldsJson.get("customfield_10520").getAsJsonObject().get("allowedValues").getAsJsonArray(); 
		for (int i=0; i<usecaseValuesJson.size(); i++) {
			usecases.add(usecaseValuesJson.get(i).getAsJsonObject().get("value").getAsString());
		}
		for (String usecase : usecases) {
			usecaseFeaturesMap.put(usecase, new ArrayList<Issue>());
		}
		System.out.println(usecases);
	}
	
	public static void initialize() throws InterruptedException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		String output = sendRequest("/rest/api/2/search", "jql=project%20%3D%20ZEP%20AND%20issuetype%20%3D%20Feature");
		
		JsonObject json = parser.parse(output).getAsJsonObject();
		int numberOfIssues = json.get("total").getAsInt();
		System.out.println("Total Features: " + numberOfIssues);
		
		JsonArray issuesList = json.get("issues").getAsJsonArray();
		for (int i = 0; i < issuesList.size(); i++) {
			Issue feature = new Issue();
			JsonObject issue = issuesList.get(i).getAsJsonObject();
			feature.setId(issue.get("id").getAsString());
			feature.setKey(issue.get("key").getAsString());
			// Construct list of usecases by calling editmeta for the first feature issue
			if (i == 0) {
				getUsecases(feature.getKey());
			}
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
	
	public static String sendRequest(String path) throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		return sendRequest(path, "");
	}
	
	public static String sendRequest(String path, String additionalParams)
			throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
		String requestUrl = App.CONTEXT_BASE_URL + path;
		String canonicalUrl = "GET&" + path + "&" + additionalParams;
		String key = App.CONTEXT_KEY; // from the add-on descriptor and received during installation handshake
		String sharedSecret = App.CONTEXT_SHARED_SECRET; // received during installation Handshake
		String jwtToken = JwtBuilder.generateJWTToken(requestUrl, canonicalUrl, key, sharedSecret);
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
}
