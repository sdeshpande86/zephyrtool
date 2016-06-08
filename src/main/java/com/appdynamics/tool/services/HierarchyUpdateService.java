package com.appdynamics.tool.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.appdynamics.tool.app.App;
import com.appdynamics.tool.app.HierarchyUpdateWorker;

@Path("updatehierarchy")
public class HierarchyUpdateService {
    /**
     * This callback is invoked every time an issue is created/updated. Since large number of issues might be updated
     * by users simultaneously, during peak hours, we create an executor service of 5 threads to update the hierarchy
     */
    @POST
    public void updateHierarchyField(@QueryParam("issue_id") String issueId) throws InterruptedException {
        App.hierarchyExecutorService.submit(new HierarchyUpdateWorker(issueId));
    }
}