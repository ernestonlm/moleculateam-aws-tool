package com.moleculateam.aws.dynamodb;

import com.amazonaws.regions.Regions;

public class APIStatus {
	
	public static String env;
	public static Regions region;
	public static boolean debug;
	public static long loadTime;
	public static boolean disableOutput;
	
	public static void loadParameters(Regions reg, boolean deb) {
		region = reg;
		debug = deb;
		loadTime = System.currentTimeMillis();
		String envaux = System.getProperty("com.moleculateam.aws.env");
		if (envaux == null) {
			env = "";
		} else {
			env = envaux.trim();
		}		
	}

	public static boolean parametersLoaded() {
		return !(env == null);
	}
	
	public static String getEnv() {
		if (env == null) {
			String envaux = System.getProperty("com.moleculateam.aws.env");
			if (envaux == null) {
				env = "";
			} else {
				env = envaux.trim();
			}
		}
		return env;
	}
}
