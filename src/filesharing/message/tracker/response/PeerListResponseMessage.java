package filesharing.message.tracker.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import filesharing.core.connection.PeerConnection;
import filesharing.core.processor.TrackerResponseProcessor;

/**
 * Tracker response: tracker replies with the list of peers for a given file
 */
public class PeerListResponseMessage extends TrackerResponseMessage {
	
	/**
	 * List of peer connections
	 */
	private List<PeerConnection> peerList;
	
	/**
	 * Message constructor
	 * @param peerList a collection of peers
	 */
	public PeerListResponseMessage(Collection<PeerConnection> peerList) {
		this.peerList = new ArrayList<PeerConnection>(peerList);
	}
	
	/**
	 * Message constructor without any peers provided
	 */
	public PeerListResponseMessage() {
		this.peerList = new ArrayList<PeerConnection>();
	}
	
	/**
	 * List of peers for the file
	 * @return a list of peer connections
	 */
	public List<PeerConnection> peerList() {
		return peerList;
	}

	@Override
	public void accept(TrackerResponseProcessor proc) throws IOException {
		proc.processPeerListResponseMessage(this);
	}

	@Override
	public String toString() {
		return super.toString() + "Peer list response: " + peerList();
	}

}
