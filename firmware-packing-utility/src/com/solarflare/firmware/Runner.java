package com.solarflare.firmware;

import java.io.File;
import java.util.Scanner;

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
			// First check in argument
			if (args.length != 0) {
				path = args[0];
				file = new File(path);
			} else if (System.getenv("FIRMWARE_PATH") != null)// Second check on
																// environment
																// variable
			{
				path = System.getenv("FIRMWARE_PATH");
				file = new File(path);
			} else {

				try (Scanner sc = new Scanner(System.in)) {
					System.out.println("Enter the firmware path : ");
					path = sc.nextLine();
					file = new File(path);
				}
			}

			// if (args.length == 0) {
			// path = System.getProperty("user.dir");
			// File currentFile = new File(path + SEPARATOR +".."+SEPARATOR+
			// RELATIVE_PATH);
			// file = currentFile.getCanonicalFile();
			// } else {
			// path = args[0];
			// file = new File(path);
			// }
			MetadataGeneratorHelper.generateMetadataJSON(file);
			System.out.println("Firmware metadata file generated successfuuly in path :" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
