package com.solarflare.firmware;

import java.io.File;

import com.solarflare.firmware.util.MetadataGeneratorHelper;

public class Runner {

	static String SEPARATOR = File.separator;
	static final String RELATIVE_PATH = "installer" + SEPARATOR + "dist"
			+ SEPARATOR + "Tomcat_Server" + SEPARATOR + "webapps" + SEPARATOR
			+ "firmware" + SEPARATOR;
	static int basePathLastIndex;

	public static void main(String args[]){
		try {
			String path = "";
			File file = null;
			int lastIndex = 0;
			// First check in argument
			if (args.length != 0) {
				path = args[0];
				path = path + File.separator;
				lastIndex = path.lastIndexOf(File.separator);
				file = new File(path);
			} 
			else if (System.getenv("FIRMWARE_PATH") != null)// Second check on environment variable
			{
				path = System.getenv("FIRMWARE_PATH");
				path = path + File.separator;
				lastIndex = path.lastIndexOf(File.separator);
				file = new File(path);
			} else {

				throw new RuntimeException("binary file path not found !!!");
			}
			MetadataGeneratorHelper.generateMetadataJSON(file, lastIndex);
			System.out.println("Firmware metadata file generated successfuuly in path :" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
