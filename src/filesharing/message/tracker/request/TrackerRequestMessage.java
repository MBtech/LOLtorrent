package filesharing.message.tracker.request;

import java.io.IOException;

import filesharing.core.processor.TrackerRequestProcessor;
import filesharing.message.tracker.TrackerMessage;

/**
 * Superclass for all tracker request messages
 * Represents a message to be sent as a request to a tracker
 */
public abstract class TrackerRequestMessage extends TrackerMessage {

	/**
	 * Accept a processor for the message
	 * @param proc processor for the tracker request
	 * @throws IOException
	 */
	public abstract void accept(TrackerRequestProcessor proc) throws IOException;
	
}
