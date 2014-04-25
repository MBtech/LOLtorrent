package filesharing.core.client;

import java.io.IOException;
import java.util.BitSet;

import filesharing.core.connection.PeerConnection;
import filesharing.core.processor.PeerResponseProcessor;
import filesharing.exception.DownloadCompleteException;
import filesharing.exception.NoNewBlocksForDownloadException;
import filesharing.exception.PeerErrorException;
import filesharing.message.peer.request.BlocksPresentRequestMessage;
import filesharing.message.peer.request.FileBlockRequestMessage;
import filesharing.message.peer.request.FileMetadataRequestMessage;
import filesharing.message.peer.request.PeerRequestMessage;
import filesharing.message.peer.response.BlocksPresentResponseMessage;
import filesharing.message.peer.response.FileBlockResponseMessage;
import filesharing.message.peer.response.FileMetadataResponseMessage;
import filesharing.message.peer.response.PeerErrorResponseMessage;

public class FileDownloaderThread implements Runnable, PeerResponseProcessor {
	
	/**
	 * The parent downloader
	 */
	private FileDownloader downloader;
	
	/**
	 * The peer to connect to and make requests
	 */
	private PeerConnection peer_information;
	
	/**
	 * Set of blocks present in the peer
	 */
	private BitSet peer_blocks_present = new BitSet();
	
	/**
	 * Constructs a new downloader thread
	 * @param downloader the parent downloader
	 * @param peer_information information about the peer to connect to
	 */
	public FileDownloaderThread(FileDownloader downloader, PeerConnection peer_information) {
		this.downloader = downloader;
		this.peer_information = peer_information;
	}
	
	/**
	 * Returns a set of which blocks are present in the remote peer
	 * @return blocks present in remote peer
	 */
	protected BitSet getPeerBlocksPresent() {
		return peer_blocks_present;
	}
	
	/**
	 * Requests metadata from remote peer
	 * @throws IOException if there is a problem with the connection
	 * @throws ClassNotFoundException if response from peer is malformed
	 */
	protected void requestMetadata() throws IOException, ClassNotFoundException {
		String filename = downloader.getFileTransfer().filename();
		PeerRequestMessage msg = new FileMetadataRequestMessage(filename);
		peer_information.sendMessage(msg, this);
	}

	/**
	 * The main loop - requesting pieces until download is complete
	 */
	@Override
	public void run() {
		try {
			// setup
			String filename = downloader.getFileTransfer().filename();
			PeerRequestMessage msg;
			
			// request the blocks the peer has
			msg = new BlocksPresentRequestMessage(filename);
			peer_information.sendMessage(msg, this);
			
			// download the blocks
			while(true) {
				try {
					int block_index = downloader.getBlockIndexForDownload(this);
					msg = new FileBlockRequestMessage(filename, block_index);
					peer_information.sendMessage(msg, this);
				}
				catch(DownloadCompleteException e) {
					// download is complete, our work here is done
					downloader.log("download complete! thread exiting!");
					break;
				}
				catch(NoNewBlocksForDownloadException e) {
					// peer has no new blocks
					// request which blocks peer has again and try again
					msg = new FileMetadataRequestMessage(filename);
					peer_information.sendMessage(msg, this);
					continue;
				}
			}
		} catch (IOException e) {
			// something really bad happened
			//e.printStackTrace();
			downloader.log(e.getMessage());
		}
	}

	/**
	 * processes error responses from peers
	 */
	@Override
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException {
		throw new PeerErrorException(msg.reason());
	}

	/**
	 * processes metadata received from peers
	 * @throws IOException 
	 */
	@Override
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) throws IOException {
		downloader.getFileTransfer().setMetadata(msg.fileSize(), msg.blockSize());
	}

	/**
	 * processes lists of blocks present received from peers
	 */
	@Override
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg) {
		this.peer_blocks_present = msg.blocksPresent();
	}

	/**
	 * processes file blocks received from peers
	 */
	@Override
	public void processFileBlockResponseMessage(FileBlockResponseMessage msg) throws IOException {
		downloader.getFileTransfer().writeBlock(msg.blockIndex(), msg.block());
	}

}
