package com.uga.myftpclient;

import java.io.File;
import java.util.Scanner;

public class AppUtil {
	
	//To make a new directory
	public static boolean makeDirectory(String name) {
        try {
            File myObj = new File(name);
            if (myObj.mkdir()) {
                System.out.println("Folder created: " + myObj.getName());
            } else {
                System.out.println("Folder already exists.");
                return false;
            }
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("An error occurred.");
            return false;
        }
        return true;
    }
	
	// To get the response from the server
	public static String getPrintWriterResponse(String line, Scanner in) {
        while (true) {
            line = in.nextLine();
            if (line.equals("") || line.equals("goodbye")) {
                return line;
            }
            System.out.println("Server response: " + line);
        }
    }
}
