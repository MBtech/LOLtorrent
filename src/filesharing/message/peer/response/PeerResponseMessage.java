package filesharing.message.peer.response;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;
import filesharing.exception.PeerErrorException;
import filesharing.message.peer.PeerMessage;

public abstract class PeerResponseMessage extends PeerMessage {
	/**
	 * Tracker response message type codes
	 */
	public static enum ResponseType {
		/**
		 * List of valid response codes
		 */
		INVALID_REQUEST (1),
		OK              (0);

		private final short message_code;

		ResponseType(short message_code) {
			this.message_code = message_code;
		}

		ResponseType(int message_code) {
			this.message_code = (short)message_code;
		}

		public int messageCode() {
			return this.message_code;
		}
	}
	
	public abstract void accept(PeerResponseProcessor proc) throws PeerErrorException, IOException;
}
