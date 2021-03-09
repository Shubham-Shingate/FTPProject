package com.uga.myftpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TerminateThread implements Runnable {
	
	private static final Logger logger = Logger.getGlobal();
	
	private int tPort;
	
	public TerminateThread(int tPort) {
		this.tPort = tPort;
	}
	
	public TerminateThread() {
		
	}

	@Override
	public void run() {
		
		Socket socket = null;
		PrintWriter terminateSocketOutPw = null;
		Scanner terminateSocketInSc = null;
		String[] commandArr;
		
		/**-----Define the thread name for terminate thread and set the logging levels----*/
		Thread.currentThread().setName("Terminate-Thread");
		LogManager.getLogManager().reset();
    	logger.setLevel(Level.ALL);
    	
    	try {
			FileHandler fh = new FileHandler("myLogger-"+Thread.currentThread().getName()+".log");
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);
			logger.addHandler(fh);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "File logger not working in TerminateThread.java- ", e);
		}
    	
    	
		/**---- Create a ServerSocket to listen for an incoming socket connection for terminate command. ----*/
		try (ServerSocket listener = new ServerSocket(tPort)) {
			
			while (true) {
				socket = listener.accept();  //This thread will wait until it receives any socket connection for terminate command
				//Perform the status update in CommandIdStatus Map for given commandId by the client
				logger.log(Level.INFO, "Connected: " + socket);

				terminateSocketOutPw = new PrintWriter(socket.getOutputStream(),true);
				terminateSocketInSc = new Scanner(socket.getInputStream());
				
				String line = terminateSocketInSc.nextLine();
                commandArr = line.split(" ", 3);
                FTPServer.commandIdStatusMap.put(commandArr[1], "T");
                terminateSocketOutPw.println("done\n");
                
                terminateSocketOutPw.close();
    			terminateSocketInSc.close();
    			socket.close();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}



}