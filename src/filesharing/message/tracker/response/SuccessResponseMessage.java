package filesharing.message.tracker.response;

import java.io.IOException;

import filesharing.core.processor.TrackerResponseProcessor;

/**
 * Tracker response: tracker replies the request was successful
 */
public class SuccessResponseMessage extends TrackerResponseMessage {

	@Override
	public String toString() {
		return super.toString() + "Success!";
	}

	@Override
	public void accept(TrackerResponseProcessor proc) throws IOException {
		proc.processSuccessResponseMessage(this);
	}

}
