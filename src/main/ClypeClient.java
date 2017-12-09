package main;

import java.io.FileNotFoundException;
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
	
	//members
	private String    userName, hostName;
	private int       port;
	private boolean   closedConnection;
	private ClypeData dataToSendToServer, dataToReceiveFromServer;
	
	private Scanner inFromStd = null;
	
	private ObjectInputStream inFromServer;
	private ObjectOutputStream outToServer;
	
	
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
		
		if(port < this.MIN_PORT) {
			throw new IllegalArgumentException("Port below 1024");
		}
		
		this.closedConnection = false;
		
		this.dataToSendToServer = null;
		this.dataToReceiveFromServer = null;
		
		this.inFromServer = null;
		this.outToServer = null;
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
	 */
	public ClypeClient(String userName) {
		this(userName, DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public ClypeClient() {
		this(DEFAULT_USER, DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public String getUserName() {
		return userName;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return port;
	}
	
	private void sendUserName() {
		this.dataToSendToServer = new MessageClypeData(userName, userName, ClypeData.MESSAGE);
		sendData();
	}
	
	public void start() {
		try {
			this.inFromStd = new java.util.Scanner(System.in);
			Socket socket = new Socket(this.hostName, this.port);

			this.outToServer = new ObjectOutputStream(socket.getOutputStream());
			this.inFromServer = new ObjectInputStream(socket.getInputStream());
			System.out.println("Connected to server.");

			Thread listener = new Thread(new ClientSideServerListener(this));
			listener.start();

			sendUserName();
			System.out.println("Enter a message to send to other users: ");

			while (!this.closed()) {
				readClientData();
				sendData();
			}

			try {
				listener.join();
			} catch (InterruptedException ie) {
				System.err.println(ie.getMessage());
			}

			this.outToServer.close();
			this.inFromServer.close();
			this.inFromStd.close();
			socket.close();
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
		}

}
	
	public void readClientData() throws FileNotFoundException, IOException {
		System.out.println("Select Session Type");
		System.out.println(ClypeData.USERLIST + ": List users");
		System.out.println(ClypeData.LOGOUT + ": Close Connection");
		System.out.println(ClypeData.FILE + ": Send File");
		System.out.println(ClypeData.MESSAGE + ": Send Message");
		
		int session = inFromStd.nextInt();
		
		if (session == ClypeData.LOGOUT) {
			closedConnection = true; 
		}
		else if (session == ClypeData.FILE) {
			System.out.println("Enter filename");
			String in = inFromStd.next();
			dataToSendToServer = new FileClypeData(this.userName, in, ClypeData.FILE);
			
			try {
				((FileClypeData) dataToSendToServer).readFileContents();
			} catch (IOException ex) {
				System.err.print("File error." + ex.getStackTrace());
				dataToSendToServer = null;
			}
		}
		else if (session == ClypeData.USERLIST) {
			this.dataToSendToServer = new MessageClypeData(userName, "list users", ClypeData.USERLIST);
		}
		
		else { //message
			System.out.println("Message");
			String in = inFromStd.next();
			dataToSendToServer = new MessageClypeData(this.userName, in, ClypeData.MESSAGE);
		}	
	}
	
	public void printData() {
		if (this.dataToReceiveFromServer.getType() == ClypeData.FILE) {
			((FileClypeData) dataToReceiveFromServer).writeFileContents();
		}
		else if (this.dataToReceiveFromServer.getType() == ClypeData.MESSAGE) {
			System.out.println(((MessageClypeData) dataToReceiveFromServer).getData());
		}
	}
	
	public void sendData() {
		try {
			this.outToServer.writeObject(dataToSendToServer);
		} catch (IOException e) {
			System.err.println("File error!");
			e.printStackTrace(System.err);
		}
		
	}
	
	public void receiveData() {
		
		try {
			this.dataToReceiveFromServer = (ClypeData)this.inFromServer.readObject();
		}
		
		catch (ClassNotFoundException e) {
			System.err.println("Strange error!");
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("File error!");
			e.printStackTrace(System.err);
		}
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
	
	public boolean closed() {
		return this.closedConnection;
	}
	
	public String toString() {
		return "ClypeClient\n User" + this.userName + "\n" +
				"host: " + this.hostName + "\n" +
				"port: " + this.port + "\n" +
				this.dataToSendToServer + this.dataToReceiveFromServer;
	}
	
	public static void main(String[] args) {
		ClypeClient client;
		String username = DEFAULT_USER;
		String hostname = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		
		if (args.length > 0) {
			Scanner argReader = new Scanner(args[0]);
			argReader.useDelimiter("[@:]");

			if (argReader.hasNext()) {
				username = argReader.next();
			}
			if (argReader.hasNext()) {
				hostname = argReader.next();
			}
			if (argReader.hasNext()) {
				port = argReader.nextInt();
			}
			else {
				System.out.println("java -jar ClypeClient [username] [hostname] [port]");
				System.exit(-1);
			}
			argReader.close();
		}
		
		client = new ClypeClient(username, hostname, port);
			
		client.start();
	}

	

}
