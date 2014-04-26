package filesharing.core.processor;

import filesharing.exception.TrackerErrorException;
import filesharing.message.tracker.response.*;

/**
 * Interface for objects that want to process responses from trackers
 */
public interface TrackerResponseProcessor {
	public void processSuccessResponseMessage(SuccessResponseMessage msg);
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) throws TrackerErrorException;
	public void processPeerListResponseMessage(PeerListResponseMessage msg);
}
