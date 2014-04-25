package filesharing.message.tracker.response;

import java.io.IOException;

import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.TrackerErrorException;

public class TrackerErrorResponseMessage extends TrackerResponseMessage {
	
	private String reason;
	
	public TrackerErrorResponseMessage(String error) {
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
	public void accept(TrackerResponseProcessor proc) throws IOException, TrackerErrorException {
		proc.processTrackerErrorResponseMessage(this);
	}

}
