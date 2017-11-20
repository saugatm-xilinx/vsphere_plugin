package com.msys.vcp.config;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

public class InitLoggingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init(ServletConfig config) throws ServletException {
		System.out.println("InitLoggingServlet is initializing log4j");
		String log4jLocation = config.getInitParameter("log4j-properties");

		ServletContext sc = config.getServletContext();

		if (log4jLocation == null) {
			System.err.println("*** No log4j-properties init param, so initializing log4j with BasicConfigurator");
			BasicConfigurator.configure();
		} else {
			String webAppPath = sc.getRealPath("/");
			String log4jProp = webAppPath + log4jLocation;
			File logFile = new File(log4jProp);
			if (logFile.exists()) {
				System.out.println("Initializing log4j with: " + log4jProp);
				System.setProperty("logDir", "WEB-INF/logs");
				PropertyConfigurator.configure(log4jProp);
			} else {
				System.err
						.println("*** " + log4jProp + " file not found, so initializing log4j with BasicConfigurator");
				BasicConfigurator.configure();
			}
		}
		super.init(config);
	}
}
