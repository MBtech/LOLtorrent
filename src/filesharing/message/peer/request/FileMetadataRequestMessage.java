package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;

/**
 * Peer request: asks peer metadata for a given file
 */
public class FileMetadataRequestMessage extends PeerRequestMessage {
	
	/**
	 * Name of the file
	 */
	String filename;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 */
	public FileMetadataRequestMessage(String filename) {
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
		proc.processFileMetadataRequestMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Request for metadata of file " + filename;
	}

}
