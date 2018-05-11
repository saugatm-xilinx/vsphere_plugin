package com.solarflare.vcp.helper;

public class VCenterHelper {

	public static String getLatestVersion(String version1, String version2) {
		// TODO : Review Comment : Check for null input
		// removing non digit characters
		String removedNonDigitChar1 = removeNonDigitChar(version1);
		String removedNonDigitChar2 = removeNonDigitChar(version2);

		// TODO : Review Comment : Check for null
		String versionAr1[] = removedNonDigitChar1.split("\\.");
		String versionAr2[] = removedNonDigitChar2.split("\\.");
		// Consider the version like this 1.1.1.1

		if (removedNonDigitChar1.equals(removedNonDigitChar2)) {
			return removedNonDigitChar1;
		}

		int l1 = versionAr1.length;
		int l2 = versionAr1.length;

		String latest = "";

		if (l1 == l2) {
			for (int i = 0; i < l1; i++) {
				int a1 = Integer.parseInt(versionAr1[i].trim());
				int a2 = Integer.parseInt(versionAr2[i].trim());
				if (a1 > a2) {
					latest = removedNonDigitChar1;
					break;
				} else if (a2 > a1) {
					latest = removedNonDigitChar2;
					break;
				}
			}
		} else {

		}
		return latest;
	}

	public static String removeNonDigitChar(String str) {
		String afterReplace = null;
		if (str != null) {
			if (str.contains(" ")) {
				afterReplace = str.split(" ")[0];
				afterReplace = afterReplace.replaceAll("[^\\d.]", "");
			} else {
				afterReplace = str;
			}

		}

		return afterReplace;
	}
	
	public static String generateId(String hostId, String adapterId, String controller) {
		return hostId + "_" + adapterId + "_" + controller;
	}
}
