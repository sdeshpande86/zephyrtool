package com.appdynamics.tool.dao;

import java.util.ArrayList;
import java.util.List;

public class Issue {
	private String id;
	private String key;
	private String issueType;
	private String testType;
	private String summary;
	private List<String> components;
	private List<Issue> children;
	private int testCount;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	
	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<String> getComponents() {
		if (components == null) {
			components = new ArrayList<String>();
		}
		return components;
	}

	public void addComponent(String component) {
		if (components == null) {
			components = new ArrayList<String>();
		}
		this.components.add(component);
	}
	
	public List<Issue> getChildren() {
		if (children == null) {
			children = new ArrayList<Issue>();
		}
		return children;
	}

	public void setChildren(List<Issue> children) {
		this.children = children;
	}
	
	public void addChild(Issue childIssue) {
		if (children == null) {
			children = new ArrayList<Issue>();
		}
		children.add(childIssue);
	}
	
	public int getTestCount() {
		return testCount;
	}

	public void setTestCount(int testCount) {
		this.testCount = testCount;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(getId()).append(", ")
		.append("Key: ").append(getKey()).append(", ")
		.append("Issue Type: ").append(getIssueType()).append(",")
		.append("Test Type: ").append(getTestType()).append(", ")
		.append("Summary: ").append(getSummary()).append(", ")
		.append("Components: ").append(getComponents()).append(",")
		.append("Children: ").append(children).append(",")
		.append("Test Count: ").append(getTestCount());
		return sb.toString();
	}
}
