package filesharing.core.message.peer.request;

import java.io.IOException;

import filesharing.core.PeerRequestProcessor;
import filesharing.core.message.peer.PeerMessage;

public abstract class PeerRequestMessage extends PeerMessage {

	/**
	 * Tracker request message type codes
	 */
	public static enum RequestType {
		/**
		 * List of valid codes
		 */
		REGISTER_PEER (0),
		GET_PEER_LIST (1);

		private final short message_code;

		RequestType(short message_code) {
			this.message_code = message_code;
		}

		RequestType(int message_code) {
			this.message_code = (short)message_code;
		}

		public int messageCode() {
			return this.message_code;
		}
	}
	
	public abstract void accept(PeerRequestProcessor proc) throws IOException;
	
}
