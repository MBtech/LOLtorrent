package filesharing.core.message.peer.request;

import java.io.IOException;

import filesharing.core.PeerRequestProcessor;

public class FileMetadataRequestMessage extends PeerRequestMessage {
	
	String filename;
	
	public FileMetadataRequestMessage(String filename) {
		this.filename = filename;
	}
	
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
