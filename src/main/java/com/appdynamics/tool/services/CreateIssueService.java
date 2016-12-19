package com.appdynamics.tool.services;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.appdynamics.tool.app.App;

@Path("createissue")
public class CreateIssueService {
	@GET
	@Produces("application/json")
	public Response createIssue(@QueryParam("usecase") String usecase, @QueryParam("parentissue") String parentIssue) throws InterruptedException, URISyntaxException {
		System.out.println("Redirecting to create issue page");
		// https://singularity.jira.com/secure/CloneIssueDetails!default.jspa?key=ZEP-94
		// https://singularity.jira.com/secure/CreateIssue!default.jspa?selectedProjectId=14890&issuetype=12103
		// https://singularity.jira.com/secure/CreateIssueDetails!init.jspa?pid=14890&issuetype=12103&customfield_16020=16130&customfield_16023=16152&issuelinks-linktype=sets%20requirements%20for&customfield_10520=10435
		return Response
				.temporaryRedirect(new URI(App.JIRA_BASE_URL + "/secure/CreateIssueDetails!init.jspa?pid="
						+ App.PROJECT_ID + "&issuetype=" + App.ID_FOR_ISSUE_TYPE_TEST + "&" + App.TESTTYPE_FIELD_ID
						+ "=" + App.ID_FOR_TEST_TYPE_FUNCTIONALITY + "&" + App.AUTOMATED_FIELD_ID + "="
						+ App.ID_FOR_AUTOMATED_NO + "&issuelinks-linktype=sets%20requirements%20for&"
						+ App.USECASE_FIELD_ID + "=" + App.usecaseValueToIdMap.get(usecase)))
				.build();
 	}
}