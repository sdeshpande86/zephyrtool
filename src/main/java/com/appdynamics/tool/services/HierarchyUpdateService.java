package com.appdynamics.tool.services;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.appdynamics.tool.app.App;
import com.appdynamics.tool.app.Creds;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Path("updatehierarchy")
public class HierarchyUpdateService {
    private static final String ISSUE_URL = "https://singularity.jira.com/rest/api/2/issue/";
    private Set<String> outwardIssueIds = new LinkedHashSet<String>();

    @POST
    public void updateHierarchyField(@QueryParam("issue_id") String issueId) throws InterruptedException {
        getOutwardIssueList(issueId);

        // Outward issue links are added in reverse with the last one being the parent issue. Reverse it to get the
        // correct parent ordering starting from the root parent
        List<String> outwardIssues = new LinkedList<String>(outwardIssueIds);
        Collections.reverse(outwardIssues);

        System.out.println(outwardIssues);

        StringBuilder hierarchy = new StringBuilder();
        hierarchy.append("/");
        for (String issue : outwardIssues) {
            hierarchy.append(getIssueSummary(issue) + "/");
        }

        hierarchy.append(getIssueSummary(issueId));
        String hierarchyString = hierarchy.toString();
        // Remove all unwanted characters from the string
        hierarchyString = hierarchyString.replaceAll("\"", "");
        hierarchyString = hierarchyString.replaceAll(" /", "/");
        hierarchyString = hierarchyString.replaceAll("/ ", "/");

        System.out.println(hierarchyString);

        updateHierarchy(issueId, hierarchyString);
    }

    private Set<String> getOutwardIssueList(String issueId) {
        String url = ISSUE_URL + issueId;
        String output = App.sendRequest(url);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();

        // Check if this issue has outward issue

        if (hasOutwardIssue(issueId)) {
            System.out.println("Has outward issue....");
            JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();

            for (int j = 0; j < issueLinks.size(); j++) {
                if (issueLinks.get(j).getAsJsonObject().has("outwardIssue")) {
                    // Get the name of the outward issue
                    JsonObject issueLink = issueLinks.get(j).getAsJsonObject().get("outwardIssue").getAsJsonObject();
                    String outwardIssueId = issueLink.get("id").getAsString();

                    // Now we need to get the outward issues for this issue (outward issue of outward issue)
                    System.out.println("Adding id to outwardList = " + outwardIssueId);

                    if (!outwardIssueIds.contains(outwardIssueId)) {
                        outwardIssueIds.add(outwardIssueId);
                    }
                    if(getOutwardIssueList(outwardIssueId) == null)
                        return null;
                }
            }
        } else {
            return null;
        }

        return outwardIssueIds;
    }

    private boolean hasOutwardIssue(String issueId) {
        String url = ISSUE_URL + issueId;
        String output = App.sendRequest(url);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();

        // Check if this issue has outward issue

        if (fields.has("issuelinks")) {
            JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();

            for (int j = 0; j < issueLinks.size(); j++) {
                // If this issue has an outward Issue, this can only be a Test Set or a Test
                if (issueLinks.get(j).getAsJsonObject().has("outwardIssue")) {
                  return true;
                }
            }
        }

        return false;
    }

    private String getIssueSummary(String issueId) {
        String url = ISSUE_URL + issueId;
        String output = App.sendRequest(url);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();
        String summary = fields.get("summary").toString();
        return summary;
    }
    
    private void updateHierarchy(String issue, String hierarchy) {
        String input = "{ \"fields\": " +
                "    {\"customfield_16221\": " + "\"" + hierarchy + "\"" + "}" +
                "}";
        String urlString = ISSUE_URL + issue;
        System.out.println("POST URL STRING = " + urlString);
		Client client = ClientBuilder.newClient();
		client.target(urlString).request().header("Authorization", Creds.authorizationString).put(Entity.entity(input, MediaType.APPLICATION_JSON_TYPE));
    }
}