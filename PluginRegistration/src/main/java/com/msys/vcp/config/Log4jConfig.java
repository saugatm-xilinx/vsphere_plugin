package com.msys.vcp.config;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Log4jConfig {
	public static boolean isLoggerconfigured;

	public static void configure(String filepath) {
		// creates pattern layout
		PatternLayout layout = new PatternLayout();
		String conversionPattern = "%-7p %d [%t] %c %x - %m%n";
		layout.setConversionPattern(conversionPattern);

		/*
		 * // creates console appender ConsoleAppender consoleAppender = new
		 * ConsoleAppender(); consoleAppender.setLayout(layout);
		 * consoleAppender.activateOptions();
		 */

		// creates file appender
		FileAppender fileAppender = new FileAppender();
		fileAppender.setFile(filepath);
		fileAppender.setLayout(layout);
		fileAppender.activateOptions();

		// configures the root logger
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		// rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(fileAppender);
		isLoggerconfigured = true;
	}
}
