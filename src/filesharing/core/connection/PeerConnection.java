package filesharing.core.connection;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;
import filesharing.exception.PeerErrorException;
import filesharing.message.peer.request.PeerRequestMessage;
import filesharing.message.peer.response.PeerResponseMessage;

/**
 * This is a connection handler for peer connections
 * Extends connection handler with the capability of making requests to peers
 */
public class PeerConnection extends ConnectionHandler {
	
	/**
	 * Constructs a new peer information
	 * @param host peer address
	 * @param port peer data port
	 */
	public PeerConnection(String host, int port) {
		super(host, port);
	}
	
	/**
	 * Synchronously send a message to the peer
	 * @param msg request message
	 * @param processor response processor
	 * @throws IOException on communication failure
	 */
	public synchronized void sendMessage(PeerRequestMessage msg, PeerResponseProcessor processor) throws IOException {
		
		// connect (if not connected yet)
		connect();
		
		// send message
		os.writeObject(msg);
		
		// receive message
		try {
			PeerResponseMessage response = (PeerResponseMessage) is.readObject();
			response.accept(processor);
		}
		catch (ClassNotFoundException e) {
			// malformed response
			throw new PeerErrorException("malformed response from peer");
		}
		
	}
	
	/**
	 * Returns a textual representation of the object
	 */
	@Override
	public String toString() {
		return "[PEER]" + host() + ":" + port();
	}
	
}
