package com.uga.myftpserver;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppUtil {
	
	//To make a new directory
	public static synchronized boolean makeDirectory(String name, Logger logger) {
        try {
            File myObj = new File(name);
            if (myObj.mkdir()) {
                logger.log(Level.INFO, "Folder created: " + myObj.getName());
            } else {
            	logger.log(Level.INFO, "Folder already exists.");
                return false;
            }
        } catch (Exception e) {
        	logger.log(Level.WARNING, "An error occurred.", e);
            return false;
        }
        return true;
    }
	
	// To get the response from the server
	public static synchronized String getPrintWriterResponse(String line, Scanner in, Logger logger) {
        while (true) {
            line = in.nextLine();
            if (line.equals("") || line.equals("goodbye")) {
                return line;
            }
            logger.log(Level.INFO , "Server response: " + line);
        }
    }
}
