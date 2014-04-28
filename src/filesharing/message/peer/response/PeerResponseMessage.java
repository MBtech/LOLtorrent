package filesharing.message.peer.response;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;
import filesharing.exception.PeerErrorException;
import filesharing.message.peer.PeerMessage;

/**
 * Superclass for all peer response messages
 * Represents a message to be sent as a response from a peer
 */
public abstract class PeerResponseMessage extends PeerMessage {
	
	/**
	 * Accept a processor for the message
	 * @param proc processor for the peer response
	 * @throws PeerErrorException if peer replies with an error message
	 * @throws IOException
	 */
	public abstract void accept(PeerResponseProcessor proc) throws PeerErrorException, IOException;
}
