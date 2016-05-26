package com.appdynamics.tool.app;

import static spark.Spark.get;

public class Service {
	public static void main(String[] args) throws InterruptedException {
		App.initialize();

		get("/zephyrtool/getfeatures", (request, response) -> {
			String usecase = request.queryParams("usecase");
			if (usecase != null && !usecase.isEmpty()) {
				return App.gson.toJson(App.usecaseFeaturesMap.get(usecase));
			} else {
				return App.gson.toJson(App.usecaseFeaturesMap);
			}
		});
		
		get("zephyrtool/getallusecases", (request, response) -> {
			return App.gson.toJson(App.usecases);
		});
	}
}