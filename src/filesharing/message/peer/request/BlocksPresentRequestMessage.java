package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;

public class BlocksPresentRequestMessage extends PeerRequestMessage {
	
	String filename;
	
	public BlocksPresentRequestMessage(String filename) {
		this.filename = filename;
	}
	
	public String filename() {
		return filename;
	}

	@Override
	public void accept(PeerRequestProcessor proc) throws IOException {
		proc.processBlocksPresentRequestMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Request for which blocks are present for file " + filename();
	}

}
