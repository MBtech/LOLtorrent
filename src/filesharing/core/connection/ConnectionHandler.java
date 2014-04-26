package filesharing.core.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * This is a connection handler - handles connectivity for a single socket
 */
public abstract class ConnectionHandler implements Serializable {

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
	 * Compares itself to another object
	 * The two objects are the same if they are both connection handlers and both
	 * the host and port are the same.
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TrackerConnection)) {
			return false;
		}
		TrackerConnection tinfo = (TrackerConnection) obj;
		return host().equals(tinfo.host())
		    && port() == tinfo.port();
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
