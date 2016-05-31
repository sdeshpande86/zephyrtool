package com.appdynamics.tool.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.appdynamics.tool.dao.Issue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Worker implements Runnable {
	public static String urlPrefix = "https://singularity.jira.com/rest/api/2/issue/";
	
	private Issue issue;
	
	public Worker(Issue issue) {
		this.issue = issue;
	}
	
	@Override
	public void run() {
		System.out.println("Processing issue " + issue.getKey());
		String output = App.sendRequest(urlPrefix + issue.getKey());
		JsonObject issueJson = App.parser.parse(output).getAsJsonObject();
		JsonObject fields = issueJson.get("fields").getAsJsonObject();
		
		issue.setIssueType(fields.get("issuetype").getAsJsonObject().get("name").getAsString());
		
		// Set test type only if issue type is Test
		if (issue.getIssueType().equalsIgnoreCase("Test")) {
			if (fields.has("customfield_16020")) {
				issue.setTestType(fields.get("customfield_16020").getAsJsonObject().get("value").getAsString());
			}
		}
		
		if (fields.has("components")) {
			JsonArray componentsArray = fields.get("components").getAsJsonArray();
			for (int c = 0; c < componentsArray.size(); c++) {
				issue.addComponent(componentsArray.get(c).getAsJsonObject().get("name").getAsString());
			}
		}
		
		JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();
		for (int j = 0; j < issueLinks.size(); j++) {
			Issue childIssue = new Issue();
			if (!issueLinks.get(j).getAsJsonObject().has("inwardIssue")) {
				continue;
			}
			JsonObject issueLink = issueLinks.get(j).getAsJsonObject().get("inwardIssue").getAsJsonObject();
			childIssue.setId(issueLink.get("id").getAsString());
			childIssue.setKey(issueLink.get("key").getAsString());
			JsonObject subIssueFields = issueLink.get("fields").getAsJsonObject();
			childIssue.setSummary(subIssueFields.get("summary").getAsString());
			issue.addChild(childIssue);
		}
		ExecutorService executorService = Executors.newFixedThreadPool(issue.getChildren().size());
		for (Issue child : issue.getChildren()) {
			executorService.submit(new Worker(child));
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
