package filesharing.message.tracker.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import filesharing.core.connection.PeerConnection;
import filesharing.core.processor.TrackerResponseProcessor;

public class PeerListResponseMessage extends TrackerResponseMessage {
	
	private List<PeerConnection> peer_list;
	
	public PeerListResponseMessage(Collection<PeerConnection> peer_list) {
		this.peer_list = new ArrayList<PeerConnection>(peer_list);
	}
	
	public PeerListResponseMessage() {
		this.peer_list = new ArrayList<PeerConnection>();
	}

	@Override
	public String toString() {
		return super.toString() + "Peer list response: " + peerList();
	}
	
	public List<PeerConnection> peerList() {
		return peer_list;
	}

	@Override
	public void accept(TrackerResponseProcessor proc) throws IOException {
		proc.processPeerListResponseMessage(this);
	}

}
