package com.appdynamics.tool.app;

public class Initializer implements Runnable {
	@Override
	public void run() {
		try {
			App.usecases.clear();
			App.usecaseFeaturesMap.clear();
			App.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}