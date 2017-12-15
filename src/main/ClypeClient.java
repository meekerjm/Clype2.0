package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import data.ClypeData;
import data.FileClypeData;
import data.MessageClypeData;

/**
 * @author pawlactb
 *
 */
public class ClypeClient {
	
	private static final int    DEFAULT_PORT =  7000;
	private static final int    MIN_PORT     =  1024;
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_USER = "Anon";
	
	private static final String key = null;
	
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			ClypeClient client = new ClypeClient();
			client.start();			
		}
		else if(args.length == 1)
		{
			String[] arguments = args[0].split("@");
			if(arguments.length == 2)
			{
				String[] port = arguments[1].split(":");
				if(port.length == 2)
				{
					ClypeClient client = new ClypeClient(arguments[0], port[0], Integer.parseInt(port[1]));
					client.start();
				}
				else
				{
					ClypeClient client = new ClypeClient(arguments[0], port[0]);
					client.start();
				}
			}
			else
			{
				ClypeClient client = new ClypeClient(arguments[0]);
				client.start();
			}
		}
		else
		{
			System.out.println("Too many arguments provided");
		}
	}
	//members
	private String    userName, hostName;
	private int       port;
	private boolean   closedConnection;
	
	private ClypeData dataToSendToServer, dataToReceiveFromServer;
	
	private Scanner inFromStd = null;
	private ObjectInputStream inFromServer;
	
	
	private ObjectOutputStream outToServer;
	
	public ClypeClient() {
		this(DEFAULT_USER, DEFAULT_HOST, DEFAULT_PORT);
	}
	
	/**
	 * @param userName Username of client.
	 */
	public ClypeClient(String userName) {
		this(userName, DEFAULT_HOST, DEFAULT_PORT);
	}
	
	/**
	 * @param userName Username of client.
	 * @param hostName Hostname of client.
	 */
	public ClypeClient(String userName, String hostName) {
		this(userName, hostName, DEFAULT_PORT);
	}
	
	/**
	 * @param userName Username of client.
	 * @param hostName Hostname of client.
	 * @param port Port number.
	 */
	public ClypeClient(String userName, String hostName, int port) throws IllegalArgumentException {
		this.userName = userName;
		this.hostName = hostName;
		this.port = port;
		
		if(userName == null || hostName == null)
		{
			throw new NullPointerException("Host or Username is null");
		}
		
		if(port < ClypeClient.MIN_PORT) {
			throw new IllegalArgumentException("Port below 1024");
		}
		
		this.closedConnection = false;
		
		this.dataToSendToServer = null;
		this.dataToReceiveFromServer = null;
		
		this.inFromServer = null;
		this.outToServer = null;
	}

	public boolean closed() {
		return this.closedConnection;
	}

	public boolean equals(Object other) {
		boolean userName, hostName, port, connection, data;
		
		if(other instanceof ClypeClient)
		{
			other = ((ClypeClient)other);
		}
		else return false;
		
		userName = this.userName == ((ClypeClient) other).getUserName();
		hostName = this.hostName == ((ClypeClient) other).getHostName();
		port = this.port == ((ClypeClient) other).getPort();
		connection = this.closedConnection == ((ClypeClient) other).closedConnection;
		
		data = this.dataToReceiveFromServer == ((ClypeClient) other).dataToReceiveFromServer && 
				this.dataToSendToServer == ((ClypeClient) other).dataToSendToServer;
		
		return userName && hostName && port && connection && data;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (closedConnection ? 1231 : 1237);
		result = prime
				* result
				+ ((dataToReceiveFromServer == null) ? 0
						: dataToReceiveFromServer.hashCode());
		result = prime
				* result
				+ ((dataToSendToServer == null) ? 0 : dataToSendToServer
						.hashCode());
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + port;
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}
	
	public void printData() {
		if (this.dataToReceiveFromServer.getType() == ClypeData.SENDFILE) {
			((FileClypeData) dataToReceiveFromServer).writeFileContents();
		}
		else{
			System.out.println(((MessageClypeData) dataToReceiveFromServer).getData());
		}
	}
	
	public void readClientData()
	{
		String input = inFromStd.nextLine();
		if(input.equals("DONE"))
		{
			System.out.println("Logging out.");
			dataToSendToServer = new MessageClypeData(userName, userName + " has left the server", 1);
			closedConnection = true;
		}
		else if(input.equals("SENDFILE"))
		{
			String fileName = inFromStd.next();
			dataToSendToServer = new FileClypeData(userName, fileName, 2);
			try
			{
				((FileClypeData)dataToSendToServer).readFileContents();		
			}
			catch(IOException ioe)
			{
				dataToSendToServer = null;
				System.err.println("File could not be read.");
			}
		}
		else if(input.equals("LISTUSERS"))
		{
			dataToSendToServer = new MessageClypeData(userName, "", 0);
		}
		else
		{
			dataToSendToServer = new MessageClypeData(userName, input, 3);
		}
		
	}
	
	public ClypeData receiveData() {
		
		try {
			this.dataToReceiveFromServer = (ClypeData)this.inFromServer.readObject();
		}
		
		catch (ClassNotFoundException e) {
			System.err.println("Strange error!");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("File error (receiveData)!");
			e.printStackTrace(System.err);
		}
		
		return this.dataToReceiveFromServer;
	}

	public void sendData() {
		try {
			outToServer.writeObject(dataToSendToServer);
		} catch (IOException e) {
			System.err.println("File error (sendData)!");
			e.printStackTrace(System.err);
		}
		
	}
	
	private void sendUserName() {
		this.dataToSendToServer = new MessageClypeData(userName, userName, ClypeData.SENDMESSAGE);
		sendData();
	}
	
	
        // Forms the connection for the GUI
        public void connect()
        {
            try
            {
                Socket skt = new Socket(hostName, port);
		outToServer = new ObjectOutputStream(skt.getOutputStream());
		inFromServer = new ObjectInputStream(skt.getInputStream());
            }
            catch (BindException ex) {
			System.err.println("Unable to bind a socket.");
			ex.printStackTrace(System.err);
			
		} catch (ConnectException ex) {
			System.err.println("Unable to connect to port.");
			ex.printStackTrace(System.err);
			
		} catch (NoRouteToHostException ex) {
			System.err.println("Routing Error.");
			ex.printStackTrace(System.err);
			
		} catch (UnknownHostException ex) {
			System.err.println("Unknown host.");
			ex.printStackTrace(System.err);
			
		} catch (SocketException ex) {
			System.err.println("Socket error");
			ex.printStackTrace(System.err);
			
		} catch (IOException ex) {
			System.err.println("IO Error");
			ex.printStackTrace(System.err);
        }
        }
        
        
        public void start() {
		try {
			inFromStd = new Scanner(System.in);
			Socket skt = new Socket(hostName, port);
			outToServer = new ObjectOutputStream(skt.getOutputStream());
			inFromServer = new ObjectInputStream(skt.getInputStream());
			System.out.println("Connection Established");
			
			ClientSideServerListener listener = new ClientSideServerListener(this);
			Thread lst = new Thread(listener);
			lst.start();
			
			dataToSendToServer = new MessageClypeData(userName, userName + " has joined the server.", 3);
			sendData();
			
			while(closedConnection == false)
			{
				readClientData();
				sendData();
			}
			lst.join();
			outToServer.close();
			inFromServer.close();
			inFromStd.close();
			skt.close();
			
		} catch (BindException ex) {
			System.err.println("Unable to bind a socket.");
			ex.printStackTrace(System.err);
			
		} catch (ConnectException ex) {
			System.err.println("Unable to connect to port.");
			ex.printStackTrace(System.err);
			
		} catch (NoRouteToHostException ex) {
			System.err.println("Routing Error.");
			ex.printStackTrace(System.err);
			
		} catch (UnknownHostException ex) {
			System.err.println("Unknown host.");
			ex.printStackTrace(System.err);
			
		} catch (SocketException ex) {
			System.err.println("Socket error");
			ex.printStackTrace(System.err);
			
		} catch (IOException ex) {
			System.err.println("IO Error");
			ex.printStackTrace(System.err);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

}
	
	public String toString() {
		return "ClypeClient\n User" + this.userName + "\n" +
				"host: " + this.hostName + "\n" +
				"port: " + this.port + "\n" +
				this.dataToSendToServer + this.dataToReceiveFromServer;
	}

	public boolean isClosedConnection() {
		return closedConnection;
	}

	public void setClosedConnection(boolean closedConnection) {
		this.closedConnection = closedConnection;
	}

	public ClypeData getDataToSendToServer() {
		return dataToSendToServer;
	}

	public void setDataToSendToServer(ClypeData dataToSendToServer) {
		this.dataToSendToServer = dataToSendToServer;
	}

	public ClypeData getDataToReceiveFromServer() {
		return dataToReceiveFromServer;
	}

	public void setDataToReceiveFromServer(ClypeData dataToReceiveFromServer) {
		this.dataToReceiveFromServer = dataToReceiveFromServer;
	}
}

