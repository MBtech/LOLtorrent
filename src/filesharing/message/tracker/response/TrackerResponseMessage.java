package filesharing.message.tracker.response;

import java.io.IOException;

import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.TrackerErrorException;
import filesharing.message.tracker.TrackerMessage;

/**
 * Superclass for all tracker response messages
 * Represents a message to be sent as a response from a tracker
 */
public abstract class TrackerResponseMessage extends TrackerMessage {

	/**
	 * Accept a processor for the message
	 * @param proc processor for the tracker response
	 * @throws TrackerErrorException if tracker replies with an error message
	 * @throws IOException
	 */
	public abstract void accept(TrackerResponseProcessor proc) throws IOException, TrackerErrorException;
}
