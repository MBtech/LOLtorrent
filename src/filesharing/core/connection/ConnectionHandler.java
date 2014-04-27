package filesharing.core.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * This is a connection handler - handles connectivity for a single socket
 */
public class ConnectionHandler implements Serializable, Comparable<ConnectionHandler> {

	/**
	 * Tracker hostname
	 */
	private String host;
	
	/**
	 * Tracker port
	 */
	private int port;
	
	/**
	 * Connection socket
	 */
	protected transient Socket sock;
	protected transient ObjectOutputStream os;
	protected transient ObjectInputStream is;
	
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
	 * Establishes a connection with the tracker
	 * @throws IOException
	 */
	protected void connect() throws IOException {
		if(!sock.isConnected()) {
			sock = new Socket(host(), port());
			os = new ObjectOutputStream(sock.getOutputStream());
			is = new ObjectInputStream(sock.getInputStream());
			return;
		}
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
	}
	
	/**
	 * Returns a textual representation of the object
	 */
	public String toString() {
		return "[CONNECTIONHANDLER]" + host() + ":" + port();
	}

}
