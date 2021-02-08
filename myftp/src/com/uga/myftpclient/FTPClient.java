package com.uga.myftpclient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;

//username: anonymous

public class FTPClient {
    public static void main(String[] args) throws IOException {

//        if (args.length != 1) {
//            System.err.println("Pass the server IP as the sole command line argument");
//            return;
//        }
        Socket socket = null;
        PrintWriter socketOutPw = null;
        Scanner socketInSc = null;
        Scanner sc = null;
        try {
        	//socket = new Socket(args[0], 8080);
        	socket = new Socket("192.168.1.9", 8080);
            socketOutPw = new PrintWriter(socket.getOutputStream(), true);
        	socketInSc = new Scanner(socket.getInputStream());
        	sc = new Scanner(System.in);
            String[] commandArr; // command holder
            String line = ""; // String init.
            String fileStorage = "ClientFileStorage";
            AppUtil.makeDirectory(fileStorage);
            line = AppUtil.getPrintWriterResponse(line, socketInSc);
            while (!line.equals("goodbye")) {
                System.out.print("myftp> ");
                line = sc.nextLine();  // what we entered
                System.out.println("You entered " + line);
                commandArr = line.split(" ", 3); // split string up

                // handling commands on a client side <(._.)>
                switch (commandArr[0]) {
                    case "get":
                        socketOutPw.println(line);
                        if ((commandArr.length == 2) && socketInSc.nextLine().equals("Ready")) {
                            line = socketInSc.nextLine();
                            int current = socketInSc.nextInt();
                            byte[] byteArray = new byte[current];
                            FileOutputStream fos = new FileOutputStream(fileStorage + "/" + line);
                            //while on till we get all the info in file
                            while (current >= 1) {
                                //read current amount of bits
                                int bytesRead = socket.getInputStream().read(byteArray, 0, current);
                                // write that many bits
                                fos.write(byteArray, 0, bytesRead);
                                current -= bytesRead;
                                byteArray = new byte[current];
                            }
                            fos.flush();
                            socketInSc.nextLine();
                        }
                        line = AppUtil.getPrintWriterResponse(line, socketInSc);
                        break;
                        
                    case "put":
                        if (commandArr.length == 2) {
                            try {
                                //get the file we want to send over
                                File myFile = new File(commandArr[1]);
                                if (myFile.exists()) {
                                    //Send the command to the server via socket's output stream
                                    socketOutPw.println(line);
                                    // get the name of the file and send it over
                                    socketOutPw.println(myFile.getName());
                                    byte[] byteArray = new byte[(int) myFile.length()];
                                    // send the size of the file
                                    socketOutPw.println(byteArray.length);
                                    FileInputStream fis = new FileInputStream(myFile);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    bis.read(byteArray, 0, byteArray.length);
                                    System.out.println("Sending " + commandArr[1] + "(" + byteArray.length + " bytes)");
                                    // send byte array over Stream
                                    socket.getOutputStream().write(byteArray, 0, byteArray.length);
                                    socket.getOutputStream().flush();
                                    bis.close();
                                    //look for response
                                    line = AppUtil.getPrintWriterResponse(line, socketInSc);
                                } else {
									throw new FileNotFoundException();
								}
                            } catch (IOException e) {
                            	e.printStackTrace();
                                System.out.println("An error occurred.");
                            }
                        }
                        break;
                        
                    default:
                        socketOutPw.println(line);
                        line = AppUtil.getPrintWriterResponse(line, socketInSc);

                }
            }
            
        } catch (Exception e) {
			e.printStackTrace();
		}
        finally {
        	socketOutPw.close();
            socketInSc.close();
            sc.close();
            socket.close();
		}
    }
    
    
    
}

