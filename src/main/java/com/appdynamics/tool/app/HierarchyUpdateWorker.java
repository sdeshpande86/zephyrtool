package com.appdynamics.tool.app;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class HierarchyUpdateWorker implements Runnable {
    private StringBuilder finalHierarchy = new StringBuilder();
    private String issueId;

    public HierarchyUpdateWorker(String issueId) {
        this.issueId = issueId;
    }

    @Override
    public void run() {
        List<String> hierarchyPathList = new LinkedList<>();
        getHierarchyList(issueId, hierarchyPathList);

        String hierarchyString = finalHierarchy.toString();
        // Remove all unwanted characters from the hierarchy string
        hierarchyString = hierarchyString.replaceAll("\"", "");
        hierarchyString = hierarchyString.replaceAll(" /", "/");
        hierarchyString = hierarchyString.replaceAll("/ ", "/");
        hierarchyString = hierarchyString.substring(0,hierarchyString.lastIndexOf(","));

        System.out.println(hierarchyString);

        try {
            updateHierarchy(issueId, hierarchyString);
        } catch (Exception e) {
            System.out.println("Exception while updating hierarchy for issue " + issueId);
        }
    }

    private boolean hasOutwardIssue(String issueId) {
        if (issueId == null || issueId.length() <= 0) {
            System.out.println("Invalid issue id");
            return false;
        }

        String output = App.sendRequestNew("/rest/api/2/issue/" + issueId);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();

        // Check if this issue has outward issue
        if (fields.has("issuelinks")) {
            JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();

            for (int j = 0; j < issueLinks.size(); j++) {
                if (issueLinks.get(j).getAsJsonObject().has("outwardIssue")) {
                    JsonObject issueLink = issueLinks.get(j).getAsJsonObject().get("outwardIssue").getAsJsonObject();
                    String outwardIssueKey = issueLink.get("key").getAsString();

                    // We need to add only "ZEP" issue links. Any "CORE" links shouldn't be considered as outward issues
                    if (outwardIssueKey!= null && outwardIssueKey.contains("CORE-"))
                        continue;

                    return true;
                }
            }
        }

        return false;
    }

    private String getIssueSummary(String issueId) {
        String output = App.sendRequestNew("/rest/api/2/issue/" + issueId);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();
        String summary = fields.get("summary").toString();
        return summary;
    }

    private void updateHierarchy(String issue, String hierarchy)
            throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (issue == null || hierarchy == null || issue.length() <= 0 || hierarchy.length() <= 0) {
            return;
        }

        // Get the existing hierarchy field for the issue. We update only if the hierarchy
        String existingHierarchy = App.hierarchyUpdateMap.get(issue);
        if (existingHierarchy != null) {
            if (existingHierarchy.contentEquals(hierarchy)) {
                // We don't need to update in this case
                System.out.println("Hierarchy hasn't changed. No update...");
                return;
            }
        }

        System.out.println("Adding <" + issue + ", " + hierarchy + "> to Hierarchy Map");
        App.hierarchyUpdateMap.put(issue, hierarchy);

        String input = "{ \"fields\": " +
                "    {\"customfield_16221\": " + "\"" + hierarchy + "\"" + "}" +
                "}";
        System.out.println("/rest/api/2/issue/" + issue);

        App.sendHierarchyUpdatePUTRequest("/rest/api/2/issue/" + issue, "", Entity.entity(input, MediaType.APPLICATION_JSON_TYPE));
    }

    private List<String> getParentIssues(String issueId) {
        List<String> parentIssues = new ArrayList<>();
        String output = App.sendRequestNew("/rest/api/2/issue/" + issueId);
        JsonObject fields = App.parser.parse(output).getAsJsonObject().get("fields").getAsJsonObject();

        // Check if this issue has outward issue
        if (fields.has("issuelinks")) {
            JsonArray issueLinks = fields.get("issuelinks").getAsJsonArray();

            for (int j = 0; j < issueLinks.size(); j++) {
                if (issueLinks.get(j).getAsJsonObject().has("outwardIssue")) {
                    // Outward issues can be "cloned" issues also. We need to ignore them
                    // Cloned issue types are named "Cloners". If we encounter them, don't add to parent issue list
                    JsonObject issueLink = issueLinks.get(j).getAsJsonObject();
                    String issueTypeName = issueLink.get("type").getAsJsonObject().get("name").getAsString();
                    if (issueTypeName.contains("Cloners")) {
                        continue;
                    }

                    JsonObject outwardIssue = issueLink.get("outwardIssue").getAsJsonObject();
                    String outwardIssueKey = outwardIssue.get("key").getAsString();

                    // We need to add only "ZEP" issue links. Any "CORE" links shouldn't be added in hierarchy
                    if (outwardIssueKey != null && !outwardIssueKey.contains("CORE-")) {
                        String outwardIssueId = outwardIssue.get("id").getAsString();
                        parentIssues.add(outwardIssueId);
                    }
                }
            }
        }

        return parentIssues;
    }

    private void getHierarchyList(String issueId, List<String> hierarchyPath) {
        if (!(hasOutwardIssue(issueId))) {
            String issueSummary = getIssueSummary(issueId);
            hierarchyPath.add(issueSummary);
            // Once we have the reached the parent issue i.e there are no more outward issues, we build the hierarchy
            buildHierarchy(hierarchyPath);
            return;
        }

        String issueSummary = getIssueSummary(issueId);
        hierarchyPath.add(issueSummary);

        List<String> parentIssues = getParentIssues(issueId);
        for (int i = 0; i < parentIssues.size(); i++) {
            String parentIssue = parentIssues.get(i);
            System.out.println(getIssueSummary(parentIssue));

            getHierarchyList(parentIssue, hierarchyPath);
        }
    }

    private void buildHierarchy(List<String> hierarchyPath) {
        List<String> hierarchyList = new ArrayList<>();
        for (String hPath : hierarchyPath) {
            if (hPath != null) {
                hierarchyList.add(hPath);
            }
        }

        // Clear only the parent issues for the next hierarchy build.
        String currentIssue = hierarchyPath.get(0);
        hierarchyPath.clear();
        hierarchyPath.add(currentIssue);

        // Outward issue links are added in reverse with the last one being the parent issue. Reverse it to get the
        // correct parent ordering starting from the root parent
        Collections.reverse(hierarchyList);

        StringBuilder hierarchy = new StringBuilder();
        hierarchy.append("/");
        for (String issue : hierarchyList) {
            if (issue != null) {
                hierarchy.append(issue + "/");
            }
        }

        // Remove the last "/" appended to the hierarchy string
        String hierarchyString = hierarchy.toString();
        hierarchyString = hierarchyString.substring(0,hierarchyString.lastIndexOf("/"));

        finalHierarchy.append(hierarchyString);
        finalHierarchy.append(",");
    }
}