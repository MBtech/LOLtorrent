package filesharing.message.peer.response;

import java.io.IOException;
import java.util.BitSet;

import filesharing.core.processor.PeerResponseProcessor;

/**
 * Peer response: peer replies with the list of blocks he has for a given file
 */
public class BlocksPresentResponseMessage extends PeerResponseMessage {
	
	/**
	 * Name of the file
	 */
	private String filename;
	
	/**
	 * Blocks present for the file
	 */
	private BitSet blocks_present;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 * @param blocksPresent blocks peer has
	 */
	public BlocksPresentResponseMessage(String filename, BitSet blocksPresent) {
		this.filename = filename;
		this.blocks_present = blocksPresent;
	}
	
	/**
	 * Name of the file
	 * @return name of the file
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * BitSet representing the list of blocks the peer has
	 * @return blocks peer has
	 */
	public BitSet blocksPresent() {
		return blocks_present;
	}

	@Override
	public void accept(PeerResponseProcessor proc) throws IOException {
		proc.processBlocksPresentResponseMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Peer response for blocks present for file " + filename + ": " + blocks_present;
	}

}
