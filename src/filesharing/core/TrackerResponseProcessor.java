package filesharing.core;

import filesharing.core.exception.TrackerErrorException;
import filesharing.core.message.tracker.response.TrackerErrorResponseMessage;
import filesharing.core.message.tracker.response.PeerListResponseMessage;
import filesharing.core.message.tracker.response.SuccessResponseMessage;

public interface TrackerResponseProcessor {
	public void processSuccessResponseMessage(SuccessResponseMessage msg);
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) throws TrackerErrorException;
	public void processPeerListResponseMessage(PeerListResponseMessage msg);
}
