package filesharing.message.peer.response;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;

public class FileBlockResponseMessage extends PeerResponseMessage {
	
	private String filename;
	private int block_index;
	private byte[] block;
	
	public FileBlockResponseMessage(String filename, int block_index, byte[] block) {
		this.filename = filename;
		this.block_index = block_index;
		this.block = block;
	}
	
	public String filename() {
		return filename;
	}
	
	public int blockIndex() {
		return block_index;
	}
	
	public byte[] block() {
		return block;
	}

	@Override
	public void accept(PeerResponseProcessor proc) throws IOException {
		proc.processFileBlockResponseMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Peer response for block " + blockIndex() + " of filename";
	}

}
