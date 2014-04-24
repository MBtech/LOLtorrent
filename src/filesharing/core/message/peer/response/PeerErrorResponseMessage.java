package filesharing.core.message.peer.response;

import filesharing.core.PeerResponseProcessor;
import filesharing.core.exception.PeerErrorException;

public class PeerErrorResponseMessage extends PeerResponseMessage {
	
	private String reason;
	
	public PeerErrorResponseMessage(String error) {
		this.reason = error;
	}
	
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
