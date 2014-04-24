package filesharing.core.message.tracker.request;

import java.io.IOException;

import filesharing.core.TrackerRequestProcessor;

public class PeerListRequestMessage extends TrackerRequestMessage {
	
	private String filename;
	
	public PeerListRequestMessage(String filename) {
		this.filename = filename;
	}
	
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
