package filesharing.core.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import filesharing.message.FileSharingMessage;

/**
 * This is a connection handler - handles connectivity for a single socket
 */
public class ConnectionHandler implements Serializable, Comparable<ConnectionHandler> {
	
	/**
	 * Default time to wait with an idle connection before taking action
	 */
	public static final int DEFAULT_IDLE_TIMEOUT = 3; // seconds

	/**
	 * Tracker hostname
	 */
	private String host;
	
	/**
	 * Tracker port
	 */
	private int port;
	
	/**
	 * How much time to wait with an idle connection before closing it
	 */
	private transient int idleTimeout = DEFAULT_IDLE_TIMEOUT; // seconds
	
	/**
	 * Last time the socket got a write or a read, miliseconds since unix time
	 */
	private transient long lastAccessTime = System.currentTimeMillis();
	
	/**
	 * Connection socket
	 */
	private transient Socket sock;
	private transient ObjectOutputStream os;
	private transient ObjectInputStream is;
	
	/**
	 * Constructor
	 * @param host tracker hostname
	 * @param port tracker port
	 */
	public ConnectionHandler(String host, int port) {
		this.host = host;
		this.port = port;
		sock = new Socket();
	}
	
	/**
	 * Establishes a connection, if not already connected
	 * @throws IOException
	 */
	public synchronized void connect() throws IOException {
		if(!sock.isConnected()) {
			sock = new Socket(host(), port());
			os = new ObjectOutputStream(sock.getOutputStream());
			is = new ObjectInputStream(sock.getInputStream());
			GlobalConnectionMonitor.getManager().watch(this);
		}
	}
	
	/**
	 * Closes the connection, if established
	 * @throws IOException
	 */
	public synchronized void disconnect() {
		if(sock.isConnected()) {
			try {
				sock.close();
			} catch (IOException e) {}
		}
	}
	
	/**
	 * Checks if it is currently connected to the remote host
	 * @return true if connected, false otherwise
	 */
	public boolean isConnected() {
		return sock.isConnected();
	}
	
	/**
	 * Writes a message on socket
	 * @param msg domain message to be sent
	 * @throws IOException on connection failure
	 */
	public void writeMessage(FileSharingMessage msg) throws IOException {
		connect();
		refreshAccessTime();
		os.writeObject(msg);
		refreshAccessTime();
	}
	
	/**
	 * Reads a message from socket
	 * @return domain message received
	 * @throws IOException on connection failure
	 * @throws ClassNotFoundException if message does not belong to the domain
	 */
	public FileSharingMessage readMessage() throws IOException, ClassNotFoundException {
		connect();
		refreshAccessTime();
		FileSharingMessage message = (FileSharingMessage) is.readObject();
		refreshAccessTime();
		return message;
	}
	
	/**
	 * Returns the tracker hostname
	 * @return hostname
	 */
	public String host() {
		return host;
	}
	
	/**
	 * Returns the tracker port
	 * @return port
	 */
	public int port() {
		return port;
	}
	
	/**
	 * Sets the connection timeout value
	 * @param seconds
	 */
	public void setIdleTimeout(int seconds) {
		this.idleTimeout = seconds;
	}
	
	/**
	 * Returns the current idle timeout for the connection
	 * @return timeout in seconds
	 */
	public int idleTimeout() {
		return idleTimeout;
	}
	
	/**
	 * Sets the last accessed time to current time
	 */
	public void refreshAccessTime() {
		lastAccessTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns the time the socket has been idle, in seconds
	 */
	public int getIdleTime() {
		return (int) (System.currentTimeMillis() - lastAccessTime)/1000;
	}

	/**
	 * Compares itself to another connection handler
	 * Required by sets to check if two objects are the same
	 * Returns 0 if the same, negative if "smaller", positive if "bigger"
	 */
	@Override
	public int compareTo(ConnectionHandler handler) {
		return (this.host()+":"+this.port()).compareTo(handler.host()+":"+handler.port);
	}
	
	/**
	 * Customize Java built-in serialization process
	 * Sockets are not serializable - therefore must be transient
	 * When building back the object, we need to create a new socket
	 * @param ois object input stream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		sock = new Socket();
		idleTimeout = DEFAULT_IDLE_TIMEOUT;
		lastAccessTime = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return "[CONNECTIONHANDLER]" + host() + ":" + port();
	}

}
