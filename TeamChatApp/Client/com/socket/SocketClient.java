package com.socket;

import java.io.*;
import java.net.*;

import javax.swing.DefaultListModel;

import gui.ChatFrame;

public class SocketClient implements Runnable {

	public int port;
	public String serverAddr;
	public Socket socket;
	public ChatFrame gui;
	public ObjectInputStream In;
	public ObjectOutputStream Out;
	
	// LinkedList which handles the messages exchanged within the chat
	public static LinkedList clientMessageList = new LinkedList(false);

	public SocketClient(ChatFrame frame) throws IOException {
		gui = frame;
		this.serverAddr = gui.serverAddr;
		this.port = gui.port;
		socket = new Socket(InetAddress.getByName(serverAddr), port);
		Out = new ObjectOutputStream(socket.getOutputStream());
		Out.flush();
		In = new ObjectInputStream(socket.getInputStream());
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		while (keepRunning) {
			try {

				Message msg = (Message) In.readObject();
				
				if (msg.type.equals("message")) {
					// Verifies if it is the first message sent as to properly populate the original text area
					boolean firstMessage = false;

					if (!msg.actualDestination.equals("")) {
						firstMessage = true;
						msg.recipient = msg.actualDestination;
						msg.actualDestination = "";
					}
					// Adds the message to the client's chat history
					clientMessageList.add(msg);
					if (firstMessage)
						gui.jTextArea1.setText(SocketClient.clientMessageList.getPrivateMessages(gui.username, "All"));
					else {
						if (msg.recipient.equals(gui.username)) {
							gui.jTextArea1.append(msg.sender + ": " + msg.content + "\n");
						} 
						else {
							gui.jTextArea1.append(msg.sender + ": " + msg.content + "\n");
						}
					}
				} else if (msg.type.equals("login")) {
					if (msg.content.equals("TRUE")) {

						gui.btnSearch.setEnabled(true);
						gui.btnSend.setEnabled(true);
						gui.tfTypemessage.setEditable(true);
						gui.tfUsername.setEnabled(false);
						gui.tfSearch.setEnabled(true);
						gui.jTextArea1.setEditable(false);
						gui.jTextArea1.append("Server: Login Successful\n");
					} else {
						gui.jTextArea1.append("Server: Login Failed\n");
					}
					// Statement which adds a new user to the userList
				} else if (msg.type.equals("newuser")) {
					if (!msg.content.equals(gui.username)) {
						boolean exists = false;
						for (int i = 0; i < gui.model.getSize(); i++) {
							if (gui.model.getElementAt(i).equals(msg.content)) {
								exists = true;
								break;
							}
						}
						if (!exists) {
							gui.model.addElement(msg.content);
							System.out.println("Original User list");
							for (int x = 0; x< gui.model.size(); x++)	
								System.out.println(gui.model.getElementAt(x));
							
							quickSort(gui.model);
							
							System.out.println("Sorted User list");
							for (int y = 0; y< gui.model.size(); y++)	
								System.out.println(gui.model.getElementAt(y));
							
							//gui.userList.setModel(gui.model);
							//gui.userList.setSelectedIndex(0);
									
						}
					}
				}

				else {
					gui.jTextArea1.append("Server: Unknown message type\n");
				}
			} catch (Exception ex) {
				keepRunning = false;
				gui.jTextArea1.append("TeamChatApp: Connection Failure\n");
				gui.btnConnect.setEnabled(true);
				gui.tfUsername.setEditable(true);
				gui.btnSend.setEnabled(false);

				for (int i = 1; i < gui.model.size(); i++) {
					gui.model.removeElementAt(i);
				}

				System.out.println("Exception SocketClient run()");
				ex.printStackTrace();
			}
		}
	}

	public void send(Message msg) {
		try {
			Out.writeObject(msg);
			Out.flush();

		} catch (IOException ex) {
			ex.getMessage();
		}
	}

	public void closeThread(Thread t) {
		t = null;
	}
	
	private void quickSort(DefaultListModel<String> dlm)	{
		/** We initialized the index at 0 so that "All" is the option
		 *  on top of the user list
		*/
		for (int x = 1; x < dlm.size(); x++)	{
	
			String tempMinUser = dlm.getElementAt(x);
			int indexMinUser = x;
			String temp = "";
				for (int y = (x+1); y < dlm.size();y++)	{
				int result = dlm.getElementAt(y).toLowerCase().compareTo(tempMinUser.toLowerCase());
				if (result < 0)	{
					tempMinUser = dlm.getElementAt(y);
					indexMinUser = y;
				}
				temp = dlm.getElementAt(x);
				
				dlm.setElementAt(tempMinUser, x);
				dlm.setElementAt(temp, indexMinUser);
				
			}
		}
					
	}
	
}
