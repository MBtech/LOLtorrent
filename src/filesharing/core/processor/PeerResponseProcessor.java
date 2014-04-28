package filesharing.core.processor;

import java.io.IOException;

import filesharing.exception.PeerErrorException;
import filesharing.message.peer.response.*;

/**
 * Interface for objects that want to process responses from peers
 */
public interface PeerResponseProcessor {
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException;
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) throws IOException;
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg);
	public void processFileBlockResponseMessage(FileBlockResponseMessage msg) throws IOException;
}
