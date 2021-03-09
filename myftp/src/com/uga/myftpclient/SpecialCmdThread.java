package com.uga.myftpclient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;

public class SpecialCmdThread implements Runnable {

	private Socket socket;
	private PrintWriter socketOutPw;
	private Scanner socketInSc;
	private String line;
	private String [] commandArr;
	private String fileStorage;

	public SpecialCmdThread(Socket socket, PrintWriter socketOutPw, Scanner socketInSc, String line, String fileStorage) {
		this.socket = socket;
		this.socketOutPw = socketOutPw;
		this.socketInSc = socketInSc;
		this.line = line;
		this.commandArr = line.split(" ", 3);
		this.fileStorage = fileStorage; 
	}
	
	public SpecialCmdThread() {
		
	}
	
	@Override
	public void run() {
		
			try {
				synchronized (socket) {
					if (commandArr[0].equals(AppConstants.FTP_GET)) { //Condition when special GET command was given by user
						socketOutPw.println(line);
						String commandId = socketInSc.nextLine();
						System.out.print("\nServer Response: Command ID= "+commandId +"\nmyftp> ");
						if ((commandArr.length == 3) && socketInSc.nextLine().equals(AppConstants.READY)) {
							line = socketInSc.nextLine();
							int current = Integer.parseInt(socketInSc.nextLine());
							byte[] byteArray = new byte[current];
							FileOutputStream fos = new FileOutputStream(fileStorage + "/" + line);
							//while to be executed until we get all the info in file
							String terminateORFileDataResp = null;
							while (current >= 1) {
								//read current amount of bytes
								terminateORFileDataResp = socketInSc.nextLine(); 	
								if (terminateORFileDataResp.equals("Terminated")) {
									break;
								} else {
									byteArray = terminateORFileDataResp.getBytes();
								}
								
								int bytesRead = byteArray.length;
								// write that many received bytes
								fos.write(byteArray, 0, bytesRead);
								current -= bytesRead;
								byteArray = new byte[0];
							}
							fos.flush();
							fos.close();
							if (terminateORFileDataResp.equals("Terminated")) {
								delete(fileStorage + "/" + line);
							}
						}
						System.out.print("\n");
						line = AppUtil.getPrintWriterResponse(line, socketInSc);
						System.out.print("myftp> ");

					} else {  //Condition when special PUT command was given by user
						if (commandArr.length == 3) {
                            try {
                                //get the file that is to be send to the server
                                File myFile = new File(commandArr[1]);
                                if (myFile.exists()) {
                                    //Send the command to server via socket's output stream
                                    socketOutPw.println(line);
                                    //Receive the command Id from the server
                                    String commandId = socketInSc.nextLine();
                                    System.out.print("\nServer Response: Command ID= "+commandId +"\nmyftp> ");
                                    // get the name of the file and send it to server
                                    socketOutPw.println(myFile.getName());
                                    byte[] byteArray = new byte[(int) myFile.length()];
                                    // send the size of the file
                                    socketOutPw.println(String.valueOf(byteArray.length));
                                    FileInputStream fis = new FileInputStream(myFile);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    bis.read(byteArray, 0, byteArray.length);
                                    System.out.print("\nSending " + commandArr[1] + "(" + byteArray.length + " bytes)" +"\nmyftp> ");
                                    // send byte array over Stream
                                    socketOutPw.println(new String(byteArray));
                                    bis.close();
                                    fis.close();
                                    //wait for response
                                    line = AppUtil.getPrintWriterResponse(line, socketInSc);
                                    System.out.print("myftp> ");
                                } else {
									throw new FileNotFoundException();
								}
                            } catch (IOException e) {
                            	System.out.println("An error has occurred."+ e);
                            }
                        }
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
	}

	private void delete(String filename) {
		try{
            File myObj = new File(filename);
            if (myObj.delete()) {
                //logger.log(Level.INFO, "File deleted " + filename);
            }
            else {
            	//logger.log(Level.WARNING, "File not found.");
                //return false;
            }
        }
        catch (Exception e) {
        	//logger.log(Level.WARNING, "An error occurred.", e);
            //return false;
        	e.printStackTrace();
        }
		
	}

	
}
