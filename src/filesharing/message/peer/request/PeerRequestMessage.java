package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;
import filesharing.message.peer.PeerMessage;

/**
 * Superclass for all peer request messages
 * Represents a message to be sent as a request to a peer
 */
public abstract class PeerRequestMessage extends PeerMessage {
	
	/**
	 * Accept a processor for the message
	 * @param proc processor for the peer request
	 * @throws IOException
	 */
	public abstract void accept(PeerRequestProcessor proc) throws IOException;
	
}
