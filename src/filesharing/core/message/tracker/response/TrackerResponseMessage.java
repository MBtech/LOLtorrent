package filesharing.core.message.tracker.response;

import java.io.IOException;

import filesharing.core.TrackerResponseProcessor;
import filesharing.core.exception.TrackerErrorException;
import filesharing.core.message.tracker.TrackerMessage;

public abstract class TrackerResponseMessage extends TrackerMessage {
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
	
	public abstract void accept(TrackerResponseProcessor proc) throws IOException, TrackerErrorException;
}
