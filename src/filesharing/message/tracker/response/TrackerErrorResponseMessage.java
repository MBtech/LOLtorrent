package filesharing.message.tracker.response;

import java.io.IOException;

import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.TrackerErrorException;

/**
 * Tracker response: tracker replies with an error message
 */
public class TrackerErrorResponseMessage extends TrackerResponseMessage {

	/**
	 * Reason for the error
	 */
	private String reason;
	
	/**
	 * Message constructor
	 * @param reason reason for failure
	 */
	public TrackerErrorResponseMessage(String reason) {
		this.reason = reason;
	}
	
	/**
	 * A message describing the reason for the error
	 * @return reason for the error
	 */
	public String reason() {
		return reason;
	}

	@Override
	public String toString() {
		return super.toString() + "An error occurred: " + reason();
	}

	@Override
	public void accept(TrackerResponseProcessor proc) throws IOException, TrackerErrorException {
		proc.processTrackerErrorResponseMessage(this);
	}

}
