package com.uga.myftpserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class FTPServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(8080)) {
            System.out.println("The FTP-server server is running...");
            ExecutorService pool = Executors.newFixedThreadPool(10);
            while (true) {
                pool.execute(new FTP(listener.accept()));
            }
        }
    }
    // class for Socket - Please work (>_<)
    private static class FTP implements Runnable {
        private Socket socket;
        FTP(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {

            System.out.println("Connected: " + socket);

            try {
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                PrintWriter socketOutPw = new PrintWriter(out,true);
                Scanner socketInSc = new Scanner(in);
                int bytesRead = 0;
                int current = 0;
                boolean loggedin = false;
                String[] commandArr;
                String line;
                String fileStorage="ServerFileStorage";
                AppUtil.makeDirectory(fileStorage);
                socketOutPw.println("Provide your username \n");
                while (true) {
                    line = socketInSc.nextLine();
                    commandArr = line.split(" ", 3);
                    // checking if the user is logged in
                    if (loggedin == true){
                        //get, put,dir,
                        switch(commandArr[0]) {
                            case "get":
                                if(commandArr.length == 2){
                                    File myFile = new File(fileStorage+"/"+commandArr[1]);
                                    if(myFile.exists()){
                                        socketOutPw.println("Ready");

                                        socketOutPw.println(myFile.getName());
                                        // bytes array for fill info
                                        byte [] byteArray  = new byte [(int)myFile.length()];
                                        // send the size of the file
                                        socketOutPw.println(byteArray.length);
                                        FileInputStream fis = new FileInputStream(myFile);
                                        BufferedInputStream bis = new BufferedInputStream(fis);
                                        bis.read(byteArray,0,byteArray.length);

                                        out.write(byteArray,0,byteArray.length);

                                        out.flush();
                                        bis.close();
                                        fis.close();

                                        socketOutPw.println("Ready\n");
                                    }
                                    else {
                                        socketOutPw.println("does not exist");
                                        socketOutPw.println("...huh?...\n");
                                    }
                                }
                                break;
                                
                            case "put":
                                if(commandArr.length == 2){
                                    line = socketInSc.nextLine();
                                    current = socketInSc.nextInt();
                                    byte [] byteArray  = new byte [current];
                                    FileOutputStream fos = new FileOutputStream(fileStorage+"/"+line);
                                    while(current >=1 ){
                                        //read current amount of bits
                                        bytesRead = in.read(byteArray,0,current);
                                        // write that many bits
                                        fos.write(byteArray, 0 , bytesRead);
                                        current -= bytesRead;
                                        byteArray  = new byte [current];
                                    }
                                    fos.flush();
                                    socketOutPw.println("Ready\n");
                                }
                                else {
                                    socketOutPw.println("...huh?...\n");
                                }
                                socketInSc.nextLine();
                                break;
                            case "dir":
                                socketOutPw.println(dir(fileStorage));
                                break;
                            case "help":
                                socketOutPw.println(help());
                                break;
                            case "del":
                                if(commandArr.length == 2){
                                    if(del(fileStorage+"/"+commandArr[1])){
                                        socketOutPw.println("done\n");
                                    }
                                    else{
                                        socketOutPw.println("could not find the file\n");
                                    }
                                }
                                else {
                                    socketOutPw.println("...huh?...\n");
                                }
                                break;
                            case "rename":
                                if(commandArr.length == 3){
                                    if(rename(fileStorage+"/"+commandArr[1],fileStorage+"/"+commandArr[2])){
                                        socketOutPw.println("done\n");
                                    }
                                    else{
                                        socketOutPw.println("could not find\n");
                                    }
                                }
                                else {
                                    socketOutPw.println("...huh?...\n");
                                }
                                break;
                                
                            case "mkdir": // To be implemented
                            	
                            	
                            	
                            	break;
                            	
                            case "bye": // This comment is self explanatory.
                                socketOutPw.println("goodbye");
                                out.close();
                                in.close();
                                socketOutPw.close();
                                socketInSc.close();
                                bye();
                                break;
                            default:
                                socketOutPw.println("...huh?...\n");
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
                }
            }
            catch (IOException e) {
                System.out.println("Error:" + socket);
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
                    + "dir - " + "shows files list located in directory\n"
                    + "help - " + "shows menu of commands\n"
                    + "del <filename> - " + "deletes the file from directory with a specified name\n"
                    + "rename <from> <to> - " + "rename a file from <from> <to>\n"
                    + "bye - " + "closes the server socket\n"
                    + "";
        }
        // delete a file
        private boolean del (String filename){
            try{
                File myObj = new File(filename);
                if (myObj.delete()) {
                    System.out.println("File deleted" + filename);
                }
                else {
                    System.out.println("File not found.");
                    return false;
                }
            }
            catch (Exception e) {
            	e.printStackTrace();
                System.out.println("An error occurred.");
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
            	e.printStackTrace();
                System.out.println("An error occurred.");
                return false;
            }
        }
        // close the socket
        private void bye (){
            try {
                socket.close();
            }
            catch (IOException e) {}
            System.out.println("Closed: " + socket);
        }
    
		/*
		 * private boolean makeDirectory(String name){ try { File myObj = new
		 * File(name); if (myObj.mkdir()) { System.out.println("Folder created: " +
		 * myObj.getName()); } else { System.out.println("Folder already exists.");
		 * return false; } } catch (Exception e) {
		 * System.out.println("An error occurred."); return false; } return true; }
		 */
    }
}