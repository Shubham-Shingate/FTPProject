package com.uga.myftpserver;

import java.util.HashMap;
import java.util.Map;

public class FileStatus {

	private static Map<String, String> fileStatusMap;

	static {
		fileStatusMap = new HashMap<String, String>();
	}

	public static synchronized boolean lockFile(String filePath) {
		if (fileStatusMap.containsKey(filePath) && fileStatusMap.get(filePath).equals("Released")) {
			fileStatusMap.put(filePath, "Engaged");
			return true;
		} else if (fileStatusMap.containsKey(filePath) && fileStatusMap.get(filePath).equals("Engaged")) {
			return false;
		} else {
			fileStatusMap.put(filePath, "Engaged");
			return true;
		}
	}

	public static synchronized boolean releaseFile(String filePath) {
		if (fileStatusMap.containsKey(filePath) && fileStatusMap.get(filePath).equals("Engaged")) {
			fileStatusMap.put(filePath, "Released");
			return true;
		}
		return false;
	}

}
