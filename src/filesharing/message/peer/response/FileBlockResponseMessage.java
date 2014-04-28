package filesharing.message.peer.response;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;

/**
 * Peer response: peer replies with the contents of a given block
 */
public class FileBlockResponseMessage extends PeerResponseMessage {
	
	/**
	 * Name of the file
	 */
	private String filename;
	
	/**
	 * Index of the block
	 */
	private int blockIndex;
	
	/**
	 * Block contents
	 */
	private byte[] block;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 * @param blockIndex block index
	 * @param block block contents
	 */
	public FileBlockResponseMessage(String filename, int blockIndex, byte[] block) {
		this.filename = filename;
		this.blockIndex = blockIndex;
		this.block = block;
	}
	
	/**
	 * Filename
	 * @return filename
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * Block index
	 * @return block index
	 */
	public int blockIndex() {
		return blockIndex;
	}
	
	/**
	 * Block contents
	 * @return block contents
	 */
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
