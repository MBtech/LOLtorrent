package filesharing.core;

import java.io.IOException;

import filesharing.core.message.tracker.request.*;

public interface TrackerRequestProcessor {
	public void processPeerListRequestMessage(PeerListRequestMessage msg) throws IOException;
	public void processRegisterPeerRequestMessage(RegisterPeerRequestMessage msg) throws IOException;
}
