package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import filesharing.core.TrackerResponseProcessor;
import filesharing.core.exception.RequestFailedException;
import filesharing.core.exception.TrackerErrorException;
import filesharing.core.message.tracker.request.PeerListRequestMessage;
import filesharing.core.message.tracker.request.RegisterPeerRequestMessage;
import filesharing.core.message.tracker.response.TrackerErrorResponseMessage;
import filesharing.core.message.tracker.response.PeerListResponseMessage;
import filesharing.core.message.tracker.response.SuccessResponseMessage;
import filesharing.core.message.tracker.response.TrackerResponseMessage;

/**
 * Represents information to connect to a tracker
 */
public class TrackerInformation implements TrackerResponseProcessor {

	/**
	 * Tracker hostname
	 */
	private String host;
	
	/**
	 * Tracker port
	 */
	private int port;
	
	/**
	 * Constructor
	 * @param host tracker hostname
	 * @param port tracker port
	 */
	public TrackerInformation(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Register the local peer with the tracker, for a given file, in a given port
	 * @param filename name of the file
	 * @param data_port seeder port
	 * @throws IOException 
	 * @throws RequestFailedException
	 */
	public void registerPeer(String filename, int data_port) throws IOException, RequestFailedException {
		
		// connect
		Socket sock = connect();
		
		// send message
		ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
		os.writeObject(new RegisterPeerRequestMessage(filename, data_port));
		
		// receive reply
		ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
		try {
			TrackerResponseMessage response = (TrackerResponseMessage) is.readObject();
			response.accept(this);
		} catch (ClassNotFoundException | TrackerErrorException e) {
			throw new RequestFailedException("peer registration failed");
		}
		
		// disconnect
		disconnect(sock);
	}
	
	/**
	 * Get the peer list for a given file from the tracker
	 * The processor for the tracker response must be supplied
	 * @param filename name of the file
	 * @param processor the tracker response processor
	 * @throws IOException
	 * @throws RequestFailedException
	 */
	public void getPeerList(String filename, TrackerResponseProcessor processor) throws IOException, RequestFailedException {
		// connect
		Socket sock = connect();
		
		// send message
		ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
		os.writeObject(new PeerListRequestMessage(filename));
		
		// receive reply
		ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
		TrackerResponseMessage response;
		try {
			response = (TrackerResponseMessage) is.readObject();
			response.accept(processor);
		} catch (ClassNotFoundException | TrackerErrorException e) {
			throw new RequestFailedException("peer list retrieval failed");
		}
		
		// disconnect
		disconnect(sock);
		
	}
	
	/**
	 * Return a new connection to the tracker
	 * @return connected socket
	 * @throws IOException
	 */
	private Socket connect() throws IOException {
		return new Socket(host, port);
	}
	
	/**
	 * Closes a previously open connection
	 * @param sock connected socket
	 * @throws IOException
	 */
	private void disconnect(Socket sock) throws IOException {
		sock.close();
	}
	
	/**
	 * Returns a textual representation of the object
	 */
	@Override
	public String toString() {
		return "[TRACKER]" + host + ":" + port;
	}
	
	/**
	 * Compares itself to another object
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TrackerInformation)) {
			return false;
		}
		TrackerInformation tinfo = (TrackerInformation) obj;
		return host().equals(tinfo.host()) && (port() == tinfo.port());
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
	 * Process success response messages
	 */
	@Override
	public void processSuccessResponseMessage(SuccessResponseMessage msg) {
		// success! be happy!
	}

	/**
	 * Process error response messages
	 */
	@Override
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) throws TrackerErrorException {
		throw new TrackerErrorException(msg.reason());
	}

	/**
	 * Process peer list response messages
	 */
	@Override
	public void processPeerListResponseMessage(PeerListResponseMessage msg) {
		throw new UnsupportedOperationException("TrackerInformation does not process PeerListResponseMessages");
	}
	
}
