package filesharing.message.tracker.request;

import java.io.IOException;

import filesharing.core.processor.TrackerRequestProcessor;

/**
 * Tracker request: peer requests tracker a list of peers for a given file
 */
public class PeerListRequestMessage extends TrackerRequestMessage {
	
	/**
	 * Name of the file
	 */
	private String filename;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 */
	public PeerListRequestMessage(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Name of the file
	 * @return name of the file
	 */
	public String filename() {
		return this.filename;
	}

	@Override
	public void accept(TrackerRequestProcessor proc) throws IOException {
		proc.processPeerListRequestMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "peer list request for " + filename();
	}
}
