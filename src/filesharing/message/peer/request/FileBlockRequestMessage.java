package filesharing.message.peer.request;

import java.io.IOException;

import filesharing.core.processor.PeerRequestProcessor;

public class FileBlockRequestMessage extends PeerRequestMessage {
	
	String filename;
	int block_number;
	
	public FileBlockRequestMessage(String filename, int block_number) {
		this.filename = filename;
		this.block_number = block_number;
	}
	
	public String filename() {
		return filename;
	}
	
	public int blockNumber() {
		return block_number;
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
