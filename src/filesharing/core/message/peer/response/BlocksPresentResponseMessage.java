package filesharing.core.message.peer.response;

import java.io.IOException;
import java.util.BitSet;

import filesharing.core.PeerResponseProcessor;

public class BlocksPresentResponseMessage extends PeerResponseMessage {
	
	private String filename;
	private BitSet blocks_present;
	
	public BlocksPresentResponseMessage(String filename, BitSet blocks_present) {
		this.filename = filename;
		this.blocks_present = blocks_present;
	}
	
	public String filename() {
		return filename;
	}
	
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
