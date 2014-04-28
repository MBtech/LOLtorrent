package filesharing.message.peer.response;

import java.io.IOException;

import filesharing.core.processor.PeerResponseProcessor;
/**
 * Peer response: peer replies with the metadata of a given file
 */
public class FileMetadataResponseMessage extends PeerResponseMessage {
	
	/**
	 * Name of the file
	 */
	private String filename;
	
	/**
	 * Size of the file
	 */
	private long fileSize;
	
	/**
	 * Block size for the file transfer
	 */
	private int blockSize;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 * @param fileSize size of the file
	 * @param blockSize block size for the file transfer
	 */
	public FileMetadataResponseMessage(String filename, long fileSize, int blockSize) {
		this.filename = filename;
		this.fileSize = fileSize;
		this.blockSize = blockSize;
	}
	
	/**
	 * Name of the file
	 * @return filename
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * Size of the file
	 * @return size of the file
	 */
	public long fileSize() {
		return fileSize;
	}
	
	/**
	 * Block size for the file transfer
	 * @return block size
	 */
	public int blockSize() {
		return blockSize;
	}

	@Override
	public void accept(PeerResponseProcessor proc) throws IOException {
		proc.processFileMetadataResponseMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Request for metadata of file " + filename;
	}

}
