package filesharing.message.peer.response;

import filesharing.core.processor.PeerResponseProcessor;
import filesharing.exception.PeerErrorException;

/**
 * Peer response: peer replies with an error message
 */
public class PeerErrorResponseMessage extends PeerResponseMessage {
	
	/**
	 * Reason for the error
	 */
	private String reason;
	
	/**
	 * Message constructor
	 * @param reason reason for failure
	 */
	public PeerErrorResponseMessage(String reason) {
		this.reason = reason;
	}
	
	/**
	 * Returns a string stating the reason for failure
	 * @return reason for failure
	 */
	public String reason() {
		return reason;
	}

	@Override
	public String toString() {
		return super.toString() + "An error occurred: " + reason();
	}

	@Override
	public void accept(PeerResponseProcessor proc) throws PeerErrorException {
		proc.processPeerErrorResponseMessage(this);
	}

}
