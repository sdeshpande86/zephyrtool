package com.appdynamics.tool.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Initializer implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		App.usecaseFeaturesMap.clear();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			App.initialize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
