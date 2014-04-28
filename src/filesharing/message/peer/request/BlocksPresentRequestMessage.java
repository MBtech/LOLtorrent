package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;

/**
 * Peer request: asks peer what blocks he has of a given file
 */
public class BlocksPresentRequestMessage extends PeerRequestMessage {
	
	/**
	 * Name of the file
	 */
	String filename;
	
	/**
	 * Message constructor
	 */
	public BlocksPresentRequestMessage(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Name of the file
	 * @return name of the file
	 */
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
