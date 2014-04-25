package filesharing.core.processor;

import java.io.IOException;

import filesharing.message.peer.request.*;

public interface PeerRequestProcessor {
	public void processFileMetadataRequestMessage(FileMetadataRequestMessage msg) throws IOException;
	public void processFileBlockRequestMessage(FileBlockRequestMessage msg) throws IOException;
	public void processBlocksPresentRequestMessage(BlocksPresentRequestMessage msg) throws IOException;
}
