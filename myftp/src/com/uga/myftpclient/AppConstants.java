package com.uga.myftpclient;

public class AppConstants {
	
	/*-- Defining the FTP server IP and Port --*/
	public static final String FTP_SERVER_IP = "192.168.1.9";
	public static final int FTP_SERVER_PORT = 8080;
	
	/*-- Defining the FTP server & Client File Storage Directory --*/
	public static final String CLIENT_FILE_STORAGE = "ClientFileStorage";
	public static final String SERVER_FILE_STORAGE = "ServerFileStorage";

	/*-- The personalized cursor for console commands --*/ 
	public static final String MY_FTP_CURSOR = "myftp> ";
	
	/*-- The response from the server --*/
	public static final String GOOD_BYE_RESPONSE = "goodbye";
	public static final String READY = "Ready";
	
	/*-- The FTP Commands --*/
	public static final String FTP_GET = "get";
	public static final String FTP_PUT = "put";
	 
	
	
}
