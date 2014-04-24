package filesharing.core.message.tracker.request;

import java.io.IOException;

import filesharing.core.TrackerRequestProcessor;

public class RegisterPeerRequestMessage extends TrackerRequestMessage {
	
	/**
	 * Filename to register the peer to
	 */
	private String filename;
	
	/**
	 * Port for other peers to connect to
	 */
	private int data_port;
	
	public RegisterPeerRequestMessage(String filename, int data_port) {
		this.filename = filename;
		this.data_port = data_port;
	}

	@Override
	public void accept(TrackerRequestProcessor proc) throws IOException {
		proc.processRegisterPeerRequestMessage(this);
	}
	
	public String filename() {
		return filename;
	}
	
	public int dataPort() {
		return data_port;
	}

	@Override
	public String toString() {
		return super.toString() + "Register peer for file " + filename() + " on port " + dataPort(); 
	}

}
