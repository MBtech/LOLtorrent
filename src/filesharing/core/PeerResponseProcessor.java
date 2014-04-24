package filesharing.core;

import java.io.IOException;

import filesharing.core.exception.PeerErrorException;
import filesharing.core.message.peer.response.*;

public interface PeerResponseProcessor {
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException;
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) throws IOException;
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg);
	public void processFileBlockResponseMessage(FileBlockResponseMessage msg) throws IOException;
}
