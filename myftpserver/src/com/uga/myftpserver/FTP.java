package com.uga.myftpserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FTP implements Runnable {

	private static final Logger logger = Logger.getGlobal();

    private Socket socket;
    public FTP(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
    	
    	LogManager.getLogManager().reset();
    	logger.setLevel(Level.ALL);
    	
    	try {
			FileHandler fh = new FileHandler("myLogger-"+Thread.currentThread().getName()+".log");
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);
			logger.addHandler(fh);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "File logger not working- ", e);
		}

        logger.log(Level.INFO, "Connected: " + socket);
        try {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            PrintWriter socketOutPw = new PrintWriter(out,true);
            Scanner socketInSc = new Scanner(in);
            boolean loggedin = false;
            boolean loggedOut = false;
            String[] commandArr;
            String fileStorage = AppConstants.SERVER_FILE_STORAGE;
            AppUtil.makeDirectory(fileStorage, logger);
            socketOutPw.println("Provide your username \n");
 
            while (true) {
                String line = socketInSc.nextLine();
                commandArr = line.split(" ", 3);
                // Check whether the user is logged in?
                if (loggedin == true){
                    
                    switch(commandArr[0]) {
                        case AppConstants.FTP_GET:
                            if(commandArr.length == 2){
                                File myFile = new File(fileStorage+"/"+commandArr[1]);
                                if(myFile.exists()){
                                    socketOutPw.println("Ready");
                                    socketOutPw.println(myFile.getName());
                                    // bytes array for holding the file information
                                    byte [] byteArray  = new byte [(int)myFile.length()];
                                    // send the size of the file
                                    socketOutPw.println(byteArray.length);
                                    FileInputStream fis = new FileInputStream(myFile);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    bis.read(byteArray,0,byteArray.length);
                                    socketOutPw.println(new String(byteArray));
                                    bis.close();
                                    fis.close();
                                    socketOutPw.println("Ready\n");
                                }
                                else {
                                    socketOutPw.println("This file does not exist!");
                                }
                            } else {
                            	 socketOutPw.println("Invalid Command..\n");
                            }
                            break;
                            
                        case AppConstants.FTP_PUT:
                            if(commandArr.length == 2){
                                line = socketInSc.nextLine();
                                int current = socketInSc.nextInt();
                                byte [] byteArray  = new byte [current];
                                FileOutputStream fos = new FileOutputStream(fileStorage+"/"+line);
                                while(current >=1 ){
                                    //read current amount of bytes
                                    byteArray = socketInSc.nextLine().getBytes();
                                    int bytesRead = byteArray.length;
                                	// write that many received bytes
                                    fos.write(byteArray, 0 , bytesRead);
                                    current -= bytesRead;
                                    byteArray  = new byte [0];
                                }
                                fos.flush();
                                fos.close();
                                socketOutPw.println("Ready\n");
                               
                            } else {
                                socketOutPw.println("Invalid Command..\n");
                            }
                            socketInSc.nextLine();
                            break;
                            
                        case AppConstants.FTP_LS:
                            socketOutPw.println(dir(fileStorage));
                            break;
                            
                        case AppConstants.FTP_HELP:
                            socketOutPw.println(help());
                            break;
                            
                        case AppConstants.FTP_PRINT_WORKING_DIRECTORY:
                        	socketOutPw.println(fileStorage);
                        	socketOutPw.println();
                            break;
                            
                        case AppConstants.FTP_DELETE:
                            if(commandArr.length == 2){
                                if(delete(fileStorage+"/"+commandArr[1])){
                                    socketOutPw.println("done\n");
                                }
                                else{
                                    socketOutPw.println("Could not find the file\n");
                                }
                            } else {
                                socketOutPw.println("Invalid Command..\n");
                            }
                            break;
                            
                        case AppConstants.FTP_RENAME:
                            if(commandArr.length == 3){
                                if(rename(fileStorage+"/"+commandArr[1],fileStorage+"/"+commandArr[2])){
                                    socketOutPw.println("done\n");
                                }
                                else{
                                    socketOutPw.println("could not find\n");
                                }
                            } else {
                                socketOutPw.println("Invalid Command..\n");
                            }
                            break;
                            
                        case AppConstants.FTP_MAKE_DIRECTORY:
                        	if(commandArr.length == 2){
                        		if(AppUtil.makeDirectory(fileStorage+"/"+commandArr[1], logger)) {
                        			socketOutPw.println("done\n");
                        		} else {
                        			socketOutPw.println("Failed to create directory\n");
								}
                            } else {
                                socketOutPw.println("Invalid Command..\n");
                            }
                        	break;
                        
                        case AppConstants.FTP_CHANGE_DIRECTORY:
							if (commandArr.length == 2) {
								if (new File(fileStorage + "/" + commandArr[1]).exists()) {
									fileStorage = fileStorage + "/" + commandArr[1];
									socketOutPw.println("done\n");
								} else {
									socketOutPw.println("Failed! this directory does not exist\n");
								}
							} else {
								socketOutPw.println("Invalid Command..\n");
							}
                        	break;    
                            
                        case AppConstants.FTP_QUIT:
                            socketOutPw.println("goodbye");
                            out.close();
                            in.close();
                            socketOutPw.close();
                            socketInSc.close();
                            quit();
                            loggedOut = true;
                            break;
                            
                        default:
                            socketOutPw.println("Invalid Command..\n");
                    }
                }
                else{
                    //login
                    switch(line) {
                        case "anonymous":
                            socketOutPw.println("Please enter password \n");
                            String pasword = socketInSc.nextLine();
                            if (pasword.equals("anonymous")) {
                            	socketOutPw.println("You are now logged in \n");
                                loggedin = true;
							}
                            break;
                            
                        default:{
                            socketOutPw.println("Invalid Username Or Password\n");
                        }
                    }
                }
                if (loggedOut == true) {
                	break;
                }
            }
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Error:" + socket, e);
        }
    } //End of the thread job (i.e the Run method)
    
    //list of files in directory
    private String dir (String fileStorage){
        String files ="";
        File folder = new File(fileStorage);
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                files +=("File " + listOfFile.getName()+"\n");
            } else if (listOfFile.isDirectory()) {
                files += ("Directory " + listOfFile.getName()+"\n");
            }
        }
        return files;
    }
    // get a String of commands
    private String help () {
        return "commands \n"
                + "get <filename> - " + "retrieves a file with specified name\n"
                + "put <filename> - " + "put a file in designated directory,  with an absolute path\n"
                + "ls - " + "shows files list located in directory\n"
                + "help - " + "shows menu of commands\n"
                + "delete <filename> - " + "deletes the file from directory with a specified name\n"
                + "rename <from> <to> - " + "rename a file from <from> <to>\n"
                + "mkdir <directoryname> - " + "creates a new directory within current working directory\n"
                + "pwd - " + "prints the name of the current working directory of remote machine\n"
                + "cd <directoryname> - " + "change the current working directory of remote machine\n"
                + "quit - " + "closes the server socket\n"
                + "";
    }
    // delete a file
    private boolean delete(String filename){
        try{
            File myObj = new File(filename);
            if (myObj.delete()) {
                logger.log(Level.INFO, "File deleted " + filename);
            }
            else {
            	logger.log(Level.WARNING, "File not found.");
                return false;
            }
        }
        catch (Exception e) {
        	logger.log(Level.WARNING, "An error occurred.", e);
            return false;
        }
        return true;
    }
    // rename a file
    private boolean rename (String from , String to){
        try {
            File f1 = new File(from);
            File f2 = new File(to);
            return f1.renameTo(f2);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "An error occurred.", e);
            return false;
        }
    }
    // close the socket
    private void quit (){
		try {
			socket.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "An error occurred.", e);
		}
		logger.log(Level.INFO, "Closed: " + socket);
    }

}

