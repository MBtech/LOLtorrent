package filesharing.core;

import java.io.IOException;

import filesharing.core.exception.PeerErrorException;
import filesharing.core.message.peer.response.BlocksPresentResponseMessage;
import filesharing.core.message.peer.response.FileMetadataResponseMessage;
import filesharing.core.message.peer.response.PeerErrorResponseMessage;

public interface PeerResponseProcessor {
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException;
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) throws IOException;
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg);
}
