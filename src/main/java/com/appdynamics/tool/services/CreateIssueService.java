package com.appdynamics.tool.services;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("createissue")
public class CreateIssueService {
	@GET
	@Produces("application/json")
	public Response createIssue(@QueryParam("issuekey") String issueKey) throws InterruptedException, URISyntaxException {
		System.out.println("Redirecting to create issue page");
		// https://singularity.jira.com/secure/CreateIssueDetails!init.jspa?pid=14890&issuetype=12103&summary=appdtest
		// https://singularity.jira.com/secure/CreateIssue!default.jspa?selectedProjectId=14890&issuetype=12103
		// https://singularity.jira.com/secure/CloneIssueDetails!default.jspa?key=ZEP-94
		return Response.temporaryRedirect(new URI("https://singularity.jira.com/secure/CreateIssue!default.jspa?selectedProjectId=14890&issuetype=12103")).build();
 	}
}