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
	public static final String JIRA_BASE_URL = "https://singularity.jira.com";
	public static final String PROJECT = "ZEP";
	public static final String PROJECT_ID = "14890";
	public static final String USECASE_FIELD_ID = "customfield_16420";
	public static final String HIERARCHY_FIELD_ID = "customfield_16221";
	public static final String TESTTYPE_FIELD_ID = "customfield_16020";
	public static final String AUTOMATED_FIELD_ID = "customfield_16023";
	
	public static final String ID_FOR_ISSUE_TYPE_TEST = "12103";
	public static final String ID_FOR_TEST_TYPE_FUNCTIONALITY = "16130";
	public static final String ID_FOR_AUTOMATED_NO = "16152";
	
	/*public static final String JIRA_BASE_URL = "https://eng-jira.corp.appdynamics.com";
	public static final String PROJECT = "QE";
	public static final String PROJECT_ID = "10000";
	public static final String USECASE_FIELD_ID = "customfield_10105";
	public static final String HIERARCHY_FIELD_ID = "customfield_10104";
	public static final String TESTTYPE_FIELD_ID = "customfield_10102";
	public static final String AUTOMATED_FIELD_ID = "customfield_10101";
	
	public static final String ID_FOR_ISSUE_TYPE_TEST = "10007";
	public static final String ID_FOR_TEST_TYPE_FUNCTIONALITY = "10102";
	public static final String ID_FOR_AUTOMATED_NO = "10101";*/
	
	public static List<String> usecases = new ArrayList<String>();
	public static Map<String, String> usecaseValueToIdMap = new HashMap<String, String>();
	public static Map<String, List<Issue>> usecaseFeaturesMap = new HashMap<String, List<Issue>>();
	public static Map<String, String> hierarchyUpdateMap = new HashMap<String, String>();
	public static JsonParser parser = new JsonParser();
	public static Gson gson = new Gson();
	public static ExecutorService hierarchyExecutorService = Executors.newFixedThreadPool(5);

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
	
	public static void getUsecases(String sampleIssueKey) {
		String url = JIRA_BASE_URL + "/rest/api/2/issue/" + sampleIssueKey + "/editmeta";
		String output = sendRequest(url);
		JsonObject issueFieldsJson = parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();
		JsonArray usecaseValuesJson = issueFieldsJson.get(USECASE_FIELD_ID).getAsJsonObject().get("allowedValues").getAsJsonArray(); 
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
		String url = JIRA_BASE_URL + "/rest/api/2/search?jql=project%20%3D%20" + PROJECT + "%20AND%20issuetype%20in%20(Feature%2C%20Subcategory)";
		String output = sendRequest(url);
		
		JsonObject json = parser.parse(output).getAsJsonObject();
		float numberOfIssues = json.get("total").getAsFloat();
		System.out.println("Total Features: " + numberOfIssues);
		
		for (int s = 0; s < Math.ceil(numberOfIssues / 50); s++) {
			url = JIRA_BASE_URL + "/rest/api/2/search?jql=project%20%3D%20" + PROJECT + "%20AND%20issuetype%20in%20(Feature%2C%20Subcategory)&startAt=" + s*50;
			output = sendRequest(url);
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
				String usecase = fields.get(USECASE_FIELD_ID).getAsJsonObject().get("value").getAsString();
				
				// Construct list of usecases by calling editmeta for the first feature issue
				if (s == 0 && i == 0) {
					getUsecases(feature.getKey());
				}
				
				// Ignore features and sub categories having parents
				String featureDetails = App.sendRequest(Worker.urlPrefix + issue.get("key").getAsString());
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
}
