package filesharing.core.message.tracker.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import filesharing.core.TrackerResponseProcessor;
import filesharing.core.client.PeerInformation;

public class PeerListResponseMessage extends TrackerResponseMessage {
	
	private List<PeerInformation> peer_list;
	
	public PeerListResponseMessage(Collection<PeerInformation> peer_list) {
		this.peer_list = new ArrayList<PeerInformation>(peer_list);
	}
	
	public PeerListResponseMessage() {
		this.peer_list = new ArrayList<PeerInformation>();
	}

	@Override
	public String toString() {
		return super.toString() + "Peer list response: " + peerList();
	}
	
	public List<PeerInformation> peerList() {
		return peer_list;
	}

	@Override
	public void accept(TrackerResponseProcessor proc) throws IOException {
		proc.processPeerListResponseMessage(this);
	}

}
