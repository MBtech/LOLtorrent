package filesharing.message.tracker.request;

import java.io.IOException;

import filesharing.core.processor.TrackerRequestProcessor;

/**
 * Tracker request: peer requests to register him as a peer for a given file
 */
public class RegisterPeerRequestMessage extends TrackerRequestMessage {
	
	/**
	 * Filename to register the peer to
	 */
	private String filename;
	
	/**
	 * Port for other peers to connect to
	 */
	private int dataPort;
	
	/**
	 * Message constructor
	 * @param filename name of the file
	 * @param dataPort port for other peers to connect to
	 */
	public RegisterPeerRequestMessage(String filename, int dataPort) {
		this.filename = filename;
		this.dataPort = dataPort;
	}
	
	/**
	 * name of the file
	 * @return name of the file
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * Data port for incomming peer connections
	 * @return data port
	 */
	public int dataPort() {
		return dataPort;
	}

	@Override
	public void accept(TrackerRequestProcessor proc) throws IOException {
		proc.processRegisterPeerRequestMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Register peer for file " + filename() + " on port " + dataPort(); 
	}

}
