package com.uga.myftpserver;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
    public static void main(String[] args) throws Exception {
    	
    	if (args.length != 1) {
			System.err.println("Pass the port number for server execution as the command line argument only");
			return;
		}
    	
        try (ServerSocket listener = new ServerSocket(Integer.parseInt(args[0]))) {
            System.out.println("The FTP-server has started!!!");
			ExecutorService pool = Executors.newFixedThreadPool(10);
			while (true) {
				pool.execute(new FTP(listener.accept()));
			}
        }
    }

}