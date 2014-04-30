package filesharing.core.connection;

import java.io.IOException;

import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.TrackerErrorException;
import filesharing.message.tracker.request.TrackerRequestMessage;
import filesharing.message.tracker.response.TrackerResponseMessage;

/**
 * This is a connection handler for tracker connections
 * Extends connection handler with the capability of making requests to trackers
 */
public class TrackerConnection extends ConnectionHandler {
	
	/**
	 * Constructor
	 * @param host tracker hostname
	 * @param port tracker port
	 */
	public TrackerConnection(String host, int port) {
		super(host, port);
	}
	
	/**
	 * Synchronously send a message to the tracker
	 * @param msg request message
	 * @param proc response processor
	 * @throws IOException on communication failure
	 */
	public synchronized void sendMessage(TrackerRequestMessage msg, TrackerResponseProcessor proc) throws IOException {
		
		// send message
		writeMessage(msg);
		
		// receive message
		try {
			TrackerResponseMessage response = (TrackerResponseMessage) readMessage();
			response.accept(proc);
		}
		catch (ClassNotFoundException e) {
			// malformed response
			throw new TrackerErrorException("malformed response from tracker");
		}
		
	}
	
	/**
	 * Returns a textual representation of the object
	 */
	@Override
	public String toString() {
		return "[TRACKER]" + host() + ":" + port();
	}
	
}
