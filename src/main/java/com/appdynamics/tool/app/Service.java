package com.appdynamics.tool.app;

import static spark.Spark.get;

public class Service {
	public static void main(String[] args) throws InterruptedException {
		App.initialize();
		get("/zephyrtool/getfeatures", (request, response) -> {
				return App.gson.toJson(App.usecaseFeaturesMap);
		});
	}
}