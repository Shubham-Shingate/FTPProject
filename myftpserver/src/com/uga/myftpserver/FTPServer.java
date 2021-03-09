package com.uga.myftpserver;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
	
	public static Map<String, String> commandIdStatusMap;
	
	static {
		commandIdStatusMap = new HashMap<String, String>();
	}
	
    public static void main(String[] args) throws Exception {
    	
    	if (args.length != 2) {
			System.err.println("Pass the port numbers nPort and tPort for server execution as the command line argument only");
			return;
		}
    	
    	TerminateThread terThread = new TerminateThread(Integer.parseInt(args[1]));
    	//TerminateThread terThread = new TerminateThread(Integer.parseInt("8081"));
    	Thread terminateThread = new Thread(terThread);
    	terminateThread.start();
    	
        try (ServerSocket listener = new ServerSocket(Integer.parseInt(args[0]))) {
        //try (ServerSocket listener = new ServerSocket(Integer.parseInt("8080"))) {
            System.out.println("The FTP-server has started!!!");
			ExecutorService pool = Executors.newFixedThreadPool(10);
			while (true) {
				pool.execute(new FTP(listener.accept()));
			}
        }
    }
    
	public static synchronized String generateCommandId() {

		String commandId = String.valueOf((int) (Math.random() * 900000) + 100000);
		
		while(FTPServer.commandIdStatusMap.containsKey(commandId)) {
			commandId = String.valueOf((int) (Math.random() * 900000) + 100000);
		}
		FTPServer.commandIdStatusMap.put(commandId, "A");
		return commandId;
	}

    

}