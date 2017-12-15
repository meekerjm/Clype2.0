/**
 * @author James Meeker
 * 
 * Class which holds all data necessary for the operation of the Clype server
 * 
 * port 					Holds the server's port number
 * closeConnection 			Indicates whether the server is connected to clients
 * serverSideClientIOList	List of ServerSideClientIO objects each of which correspond to a connected client
 */

package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import data.ClypeData;

public class ClypeServer {
	private int port;
	private boolean closeConnection;
	private ArrayList<ServerSideClientIO> serverSideClientIOList;
	
	public ClypeServer(int port)
	{
		if(port < 1024)
			throw new IllegalArgumentException("Port less than 1024");
		
		this.port = port;
		this.closeConnection = false;
		this.serverSideClientIOList = new ArrayList<ServerSideClientIO>(0);
	}
	
	public ClypeServer()
	{
		this(7000);
	}
	
	public void start()
	{
		try
		{
			ServerSocket sskt = new ServerSocket(port);
			while(closeConnection == false)
			{
				Socket clientSkt = sskt.accept();
				ServerSideClientIO clientIO = new ServerSideClientIO(this, clientSkt);
				serverSideClientIOList.add(clientIO);
				(new Thread(clientIO)).start();
			}
			sskt.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IO Error starting server");
		}
	}
	
	public synchronized void broadcast(ClypeData dataToBroadcastToClients)
	{
		for(int i=0; i<serverSideClientIOList.size(); i++)
		{
			serverSideClientIOList.get(i).setDataToSendToClient(dataToBroadcastToClients);
			serverSideClientIOList.get(i).sendData();
		}
	}
	
	public synchronized void remove(ServerSideClientIO serverSideClientToRemove)
	{
		serverSideClientIOList.remove(serverSideClientToRemove);
	}
	
	public String getUserList()
	{
		String users = new String();
		
		for(int i=0; i<serverSideClientIOList.size(); i++)
		{
			users += (serverSideClientIOList.get(i).getUserName() + ", ");
		}
		
		return users;
	}

	public int getPort() {
		return port;
	}
	
	public static void main(String[] args)
	{
		if(args.length == 1)
		{
			ClypeServer server = new ClypeServer(Integer.parseInt(args[0]));
			server.start();
		}
		else if(args.length == 0)
		{
			ClypeServer server = new ClypeServer();
			server.start();
		}
		else
		{
			System.out.println("Too many arguments provided");
		}
	}
	
	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = hash*5 + port;
		return hash;
	}
	
	public boolean equals(Object o)
	{
		ClypeServer otherServer = (ClypeServer)o;
		return this.hashCode() == otherServer.hashCode();
	}
	
	public String toString()
	{
		return "Port: " + this.port;
	}

}
