/**
 * @author James Meeker
 * 
 * Class which holds all data necessary for the operation of the Clype server
 * 
 * closeConnection 			Indicates whether the server is connected to clients
 * dataToReceiveFromClient 	Holds the ClypeData object received from a connected client
 * dataToSendToClient 		Holds the ClypeData object which will be sent to a client
 * inFromClient				ObjectInputStream which receives data from the client
 * outToClient				ObjectoutputStream which sends data to the client
 * server					ClypeServer object which spawned this class
 * clientSocket				Socket object holding the connection to the client
 * userName					String holding the username of the client this class is connected to
 */

package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.ClypeData;
import data.MessageClypeData;

public class ServerSideClientIO implements Runnable
{
	private boolean closeConnection;
	private ClypeData dataToReceiveFromClient;
	private ClypeData dataToSendToClient;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private ClypeServer server;
	private Socket clientSocket;
	private String userName;
	
	public ServerSideClientIO(ClypeServer server, Socket clientSocket)
	{
		this.server  = server;
		this.clientSocket = clientSocket;
		
		this.closeConnection = false;
		
		this.dataToReceiveFromClient = null;
		this.dataToSendToClient = null;
		this.inFromClient = null;
		this.outToClient = null;
		this.userName = new String();
	}
	
	public String getUserName()
	{
		return userName;
	}
	
	public void receiveData()
	{
		try {
			dataToReceiveFromClient = (ClypeData) inFromClient.readObject();
			
			
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found exception");
		} catch (IOException ioe) {
			System.err.println("Error receiving data from client");
		}
	}
	
	public void run() {
		
		try
		{
			inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			
			receiveData();
			userName = dataToReceiveFromClient.getUserName();
			server.broadcast(dataToReceiveFromClient);
		
			while(closeConnection == false)
			{
				receiveData();
				if(dataToReceiveFromClient.getType() == 0)
				{
					dataToSendToClient = new MessageClypeData("User List:", server.getUserList(), 3);
					sendData();
				}
				else
				{
					server.broadcast(dataToReceiveFromClient);
					if(dataToReceiveFromClient.getType() == 1)
					{
						closeConnection = true;
						server.remove(this);
					}
				}
			}
		}
		catch(IOException ioe)
		{
			System.err.println("Error starting server IO.");
		}
		
	}
	
	public void sendData()
	{
		try {
			outToClient.writeObject(dataToSendToClient);
		} catch (IOException ioe) {
			System.err.println("Error writing data to client");
		}
	}

	public void setDataToSendToClient(ClypeData dataToSendToClient)
	{
		this.dataToSendToClient = dataToSendToClient;
	}

}
