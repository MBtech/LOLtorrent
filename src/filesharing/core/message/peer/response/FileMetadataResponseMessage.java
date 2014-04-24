package filesharing.core.message.peer.response;

import java.io.IOException;

import filesharing.core.PeerResponseProcessor;

public class FileMetadataResponseMessage extends PeerResponseMessage {
	
	private String filename;
	private long file_size;
	private int block_size;
	
	public FileMetadataResponseMessage(String filename, long file_size, int block_size) {
		this.filename = filename;
		this.file_size = file_size;
		this.block_size = block_size;
	}
	
	public String filename() {
		return filename;
	}
	
	public long fileSize() {
		return file_size;
	}
	
	public int blockSize() {
		return block_size;
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
