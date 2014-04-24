package filesharing.core.message.tracker.response;

import java.io.IOException;

import filesharing.core.TrackerResponseProcessor;

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
