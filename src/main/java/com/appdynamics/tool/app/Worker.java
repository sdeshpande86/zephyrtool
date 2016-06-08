package com.appdynamics.tool.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.appdynamics.tool.dao.Issue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Worker implements Runnable {
	private Issue issue;
	
	public Worker(Issue issue) {
		this.issue = issue;
	}
	
	@Override
	public void run() {
		System.out.println("Processing issue " + issue.getKey());
		String output = null;
		try {
			output = App.sendRequestNew("/rest/api/2/issue/" + issue.getKey());
		} catch (Exception e) {
			System.out.println("Failed to get issue JSON for issue " + issue.getKey());
		}
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
			
			// If inward issue is not present or type is not "sets requirements for", continue
			if (!issueLinks.get(j).getAsJsonObject().has("inwardIssue") || !issueLinks.get(j).getAsJsonObject().get("type").getAsJsonObject().get("outward").getAsString().equalsIgnoreCase("sets requirements for")) {
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
		
		// Find the number of tests under this issue
		int testCount = 0;
		for (Issue childIssue : issue.getChildren()) {
			if (childIssue.getIssueType().equalsIgnoreCase("Test")) {
				testCount++;
			} else {
				testCount += childIssue.getTestCount();
			}
		}
		issue.setTestCount(testCount);
	}

}
