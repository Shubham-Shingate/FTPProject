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

		
		if (args.length != 3) {
			System.err.println("Pass the server IP, nPort, tPort all three as the command line argument only");
			return;
		}

        Socket socket = null;
        PrintWriter socketOutPw = null;
        Scanner socketInSc = null;
        Scanner sc = null;
        
        Socket terminateSocket = null;
        PrintWriter terminateSocketOutPw = null;
        Scanner terminateSocketInSc = null;
        
        try {
        	socket = new Socket(args[0], Integer.parseInt(args[1]));
        	//socket = new Socket("192.168.1.9", Integer.parseInt("8080"));
            socketOutPw = new PrintWriter(socket.getOutputStream(), true);
        	socketInSc = new Scanner(socket.getInputStream());
        	sc = new Scanner(System.in);
            String[] commandArr; // command holder
            String line = ""; // String init.
            String fileStorage = AppConstants.CLIENT_FILE_STORAGE;
            AppUtil.makeDirectory(fileStorage);
            line = AppUtil.getPrintWriterResponse(line, socketInSc);
            while (!line.equals(AppConstants.GOOD_BYE_RESPONSE)) {
                System.out.print(AppConstants.MY_FTP_CURSOR);
                line = sc.nextLine();  // what we entered
                System.out.println("You entered " + line);
                commandArr = line.split(" ", 3); // split string up

                // handling commands- client related tasks only.
                switch (commandArr[0]) {
                    case AppConstants.FTP_GET:
                    	
                    	if (commandArr.length == 3 && commandArr[2].equals("&")) {
                    		//This is a special command should be handled in a separate thread by passing the socket into it.
                    		SpecialCmdThread specialCmd = new SpecialCmdThread(socket, socketOutPw, socketInSc, new String(line), new String(fileStorage));
                    		Thread specialCmdThread = new Thread(specialCmd);
                    		specialCmdThread.start();
                    		break;
                    	}
                    	
					synchronized (socket) {
						socketOutPw.println(line);
						if ((commandArr.length == 2) && socketInSc.nextLine().equals(AppConstants.READY)) {
							line = socketInSc.nextLine();
							int current = Integer.parseInt(socketInSc.nextLine());
							byte[] byteArray = new byte[current];
							FileOutputStream fos = new FileOutputStream(fileStorage + "/" + line);
							//while to be executed until we get all the info in file
							while (current >= 1) {
								//read current amount of bytes
								byteArray = socketInSc.nextLine().getBytes();
								int bytesRead = byteArray.length;
								// write that many received bytes
								fos.write(byteArray, 0, bytesRead);
								current -= bytesRead;
								byteArray = new byte[0];
							}
							fos.flush();
							fos.close();
							//socketInSc.nextLine();
						}
						line = AppUtil.getPrintWriterResponse(line, socketInSc);
					}
					break;
                        
                    case AppConstants.FTP_PUT:
                    	
                    	if (commandArr.length == 3 && commandArr[2].equals("&")) {
                    		//This is a special command should be handled in a separate thread by passing the socket into it.
                    		SpecialCmdThread specialCmd = new SpecialCmdThread(socket, socketOutPw, socketInSc, new String(line), new String(fileStorage));
                    		Thread specialCmdThread = new Thread(specialCmd);
                    		specialCmdThread.start();
                    		break;
                    	}
                    	
					synchronized (socket) {
						if (commandArr.length == 2) {
							try {
								//get the file that is to be send to the server
								File myFile = new File(commandArr[1]);
								if (myFile.exists()) {
									//Send the command to server via socket's output stream
									socketOutPw.println(line);
									// get the name of the file and send it to server
									socketOutPw.println(myFile.getName());
									byte[] byteArray = new byte[(int) myFile.length()];
									// send the size of the file
									socketOutPw.println(String.valueOf(byteArray.length));
									FileInputStream fis = new FileInputStream(myFile);
									BufferedInputStream bis = new BufferedInputStream(fis);
									bis.read(byteArray, 0, byteArray.length);
									System.out.println("Sending " + commandArr[1] + "(" + byteArray.length + " bytes)");
									// send byte array over Stream
									socketOutPw.println(new String(byteArray));
									bis.close();
									fis.close();
									//wait for response
									line = AppUtil.getPrintWriterResponse(line, socketInSc);
								} else {
									throw new FileNotFoundException();
								}
							} catch (IOException e) {
								System.out.println("An error has occurred." + e);
							}
						}
					}
					break;
                    
					case AppConstants.FTP_TERMINATE:
						/* Create a new socket connection from the main thread of client to the tPort of
						 * server where TerminateThread is waiting.
						 */
						if (commandArr.length == 2 && commandArr[0].startsWith(AppConstants.FTP_TERMINATE)) {
							
							try {
								terminateSocket = new Socket(args[0], Integer.parseInt(args[2]));
								//terminateSocket = new Socket("192.168.1.9", Integer.parseInt("8081"));
								terminateSocketOutPw = new PrintWriter(terminateSocket.getOutputStream(), true);
								terminateSocketInSc = new Scanner(terminateSocket.getInputStream());
								terminateSocketOutPw.println(line);
								line = AppUtil.getPrintWriterResponse(line, terminateSocketInSc);
							
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								terminateSocketOutPw.close();
								terminateSocketInSc.close();
								terminateSocket.close();
							}
							
						}
						
					break;
					
                    default:
					synchronized (socket) {
						socketOutPw.println(line);
						line = AppUtil.getPrintWriterResponse(line, socketInSc);
					}
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

