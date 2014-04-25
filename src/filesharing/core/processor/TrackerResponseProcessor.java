package filesharing.core.processor;

import filesharing.exception.TrackerErrorException;
import filesharing.message.tracker.response.PeerListResponseMessage;
import filesharing.message.tracker.response.SuccessResponseMessage;
import filesharing.message.tracker.response.TrackerErrorResponseMessage;

public interface TrackerResponseProcessor {
	public void processSuccessResponseMessage(SuccessResponseMessage msg);
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) throws TrackerErrorException;
	public void processPeerListResponseMessage(PeerListResponseMessage msg);
}
