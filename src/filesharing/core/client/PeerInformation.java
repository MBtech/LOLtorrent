package filesharing.core.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * Represents information about a peer
 */
public class PeerInformation implements Serializable {
	
	/**
	 * Peer address
	 */
	private String host;
	
	/**
	 * Peer port
	 */
	private int data_port;
	
	/**
	 * Constructs a new peer information
	 * @param host peer address
	 * @param data_port peer data port
	 */
	public PeerInformation(String host, int data_port) {
		this.host = host;
		this.data_port = data_port;
	}
	
	/**
	 * Returns the peer's address
	 * @return address of the peer
	 */
	public String host() {
		return host;
	}
	
	/**
	 * Returns the peer's data port
	 * @return data port for the peer
	 */
	public int dataPort() {
		return data_port;
	}
	
	/**
	 * Returns a new connection to the peer
	 * @return a new socket connected to the peer
	 * @throws IOException
	 */
	public Socket connect() throws IOException {
		return new Socket(host(), dataPort());
	}
	
	/**
	 * Compares this peer with another peer to check if they are the same
	 */
	public boolean equals(Object o) {
		if(!(o instanceof PeerInformation)) {
			return false;
		}
		PeerInformation pinfo = (PeerInformation) o;
		return this.host().equals(pinfo.host)
		    && this.data_port == pinfo.data_port;
	}
	
}
