package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;

/**
 * Peer request: asks peer for a given block of a given file
 */
public class FileBlockRequestMessage extends PeerRequestMessage {
	
	/**
	 * Name of the file
	 */
	String filename;
	
	/**
	 * Block index
	 */
	int blockNumber;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 * @param blockNumber block index
	 */
	public FileBlockRequestMessage(String filename, int blockNumber) {
		this.filename = filename;
		this.blockNumber = blockNumber;
	}
	
	/**
	 * Name of the file
	 * @return name of the file
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * Block index
	 * @return block index
	 */
	public int blockNumber() {
		return blockNumber;
	}

	@Override
	public void accept(PeerRequestProcessor proc) throws IOException {
		proc.processFileBlockRequestMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Request for block " + blockNumber() + " of file " + filename();
	}

}
