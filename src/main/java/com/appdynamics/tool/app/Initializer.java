package com.appdynamics.tool.app;

public class Initializer implements Runnable {
	@Override
	public void run() {
		try {
			App.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
