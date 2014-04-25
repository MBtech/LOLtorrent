package filesharing.core.client;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filesharing.core.connection.PeerConnection;
import filesharing.core.connection.TrackerConnection;
import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.DownloadCompleteException;
import filesharing.exception.NoMetadataException;
import filesharing.exception.NoNewBlocksForDownloadException;
import filesharing.message.tracker.request.PeerListRequestMessage;
import filesharing.message.tracker.request.TrackerRequestMessage;
import filesharing.message.tracker.response.PeerListResponseMessage;
import filesharing.message.tracker.response.SuccessResponseMessage;
import filesharing.message.tracker.response.TrackerErrorResponseMessage;

/**
 * File metadata and content downloader - makes requests to peers and handles
 * responses
 * @author anatoly
 *
 */
public class FileDownloader implements Runnable, TrackerResponseProcessor {
	
	/**
	 * Downloader thread
	 */
	private Thread runner_thread = new Thread(this);
	
	/**
	 * Pool of downloading threads
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * File transfer associated with this downloader
	 */
	private FileTransfer file_transfer;
	
	/**
	 * Set of blocks assigned for downloading
	 */
	private BitSet blocks_for_download;
	
	/**
	 * Constructor
	 * @param file_info information about file to download
	 */
	public FileDownloader(FileTransfer file_info) {
		this.file_transfer = file_info;
	}
	
	/**
	 * Starts execution of the file downloader in a new thread
	 */
	public void start() {
		runner_thread.start();
	}
	
	/**
	 * Returns the file transfer instance associated with this downloader
	 * @return file transfer
	 */
	protected FileTransfer getFileTransfer() {
		return file_transfer;
	}
	
	/**
	 * Returns the bit set with the blocks that have been assigned for download
	 * @return blocks for download
	 */
	protected BitSet getBlocksForDownload() {
		return blocks_for_download;
	}
	
	/**
	 * Returns the list of seeders
	 * @return set of information of peers
	 */
	protected Set<PeerConnection> seedList() {
		return seed_list;
	}
	
	/**
	 * List of seeders
	 */
	private Set<PeerConnection> seed_list = new HashSet<PeerConnection>();
	
	/**
	 * Returns a block index for a thread to download
	 * @return block index
	 */
	protected synchronized int getBlockIndexForDownload(FileDownloaderThread peer) {
		String filename = file_transfer.filename();
		BitSet local_blocks = file_transfer.getBlocksPresent();
		BitSet peer_blocks = peer.getPeerBlocksPresent();
		
		// check if we already have all the blocks
		if(local_blocks.cardinality() == file_transfer.numBlocks()) {
			throw new DownloadCompleteException("download of file " + filename + " is finished");
		}
		
		// check which blocks peer has that we dont have
		BitSet peer_new_blocks = new BitSet();
		peer_new_blocks.or(peer_blocks);
		peer_new_blocks.andNot(local_blocks);
		
		// check which blocks peer has that we dont have
		// and that we have not requested for download yet
		BitSet blocks_interest = new BitSet();
		blocks_interest.or(peer_new_blocks);
		blocks_interest.andNot(this.blocks_for_download);
		
		//check if peer does not have any new blocks at all
		if(peer_new_blocks.cardinality() == 0) {
			throw new NoNewBlocksForDownloadException("peer has no new blocks for file " + filename);
		}
		
		// check if peer does not have any blocks that are not present or assigned
		if(blocks_interest.cardinality() == 0) {
			// assign one that has already been assigned then ("endgame"?)
			return peer_new_blocks.nextSetBit(0);
		}
		else {
			// assign a block that has never been requested for download
			int block_index = blocks_interest.nextSetBit(0);
			this.blocks_for_download.set(block_index);
			return block_index;
		}
		
	}
	
	/**
	 * Updates peer list
	 */
	private void updatePeerList() {
		String filename = file_transfer.filename();
		
		// connect to trackers and ask for peers for this file
		for(TrackerConnection tracker : file_transfer.getTrackers()) {
			try {
				// send request
				TrackerRequestMessage msg = new PeerListRequestMessage(filename);
				tracker.sendMessage(msg, this);
			}
			catch (IOException e) {
				// it failed... that's life, ignore it - neeext!
				log(e.getMessage());
			}
		}
	}
	
	public void fetchMetadata() throws IOException {
		// setup
		String filename = file_transfer.filename();
		
		// update the peer list of peers
		updatePeerList();
		
		// connect to peers and ask for metadata (one at a time)
		for(PeerConnection peer : seedList()) {
			try {
				FileDownloaderThread fdt = new FileDownloaderThread(this, peer);
				fdt.requestMetadata();
			}
			catch(IOException | ClassNotFoundException e) {
				// communication failed or bad response from peer - ignore it
				// do nothing and move on to next peer
			}
		}
		
		// check if after all this we indeed have the metadata
		if(!file_transfer.hasMetadata()) {
			// no?! die miserably then
			log("could not fetch metadata for file " + filename);
			throw new NoMetadataException("all the peers were mean to me: could not fetch metadata");
		}
	}
	
	/**
	 * Logs a message to console
	 * @param msg message to log
	 */
	protected void log(String msg) {
		file_transfer.log("[DOWN] " + msg);
	}

	/**
	 * Starts the downloading of the file
	 */
	@Override
	public void run() {
		// initialize
		blocks_for_download = new BitSet(file_transfer.numBlocks());
		// start a downloader thread for every peer
		for(PeerConnection peer_info : seedList()) {
			executor.execute(new FileDownloaderThread(this, peer_info));
		}
	}
	
	/**
	 * process success response from tracker
	 */
	@Override
	public void processSuccessResponseMessage(SuccessResponseMessage msg) {
		/* success! nothing to do... */
	}

	/**
	 * process error response from tracker
	 */
	@Override
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) {
		/* hmm... just ignore errors from tracker */
		/* or should we also throw tracker error exceptions? */
		log("tracker returned " + msg.reason());
	}

	/**
	 * Process peer list response from tracker
	 */
	@Override
	public void processPeerListResponseMessage(PeerListResponseMessage msg) {
		// check which are new peers (remove existing ones from retrieved list)
		msg.peerList().removeAll(seedList());
		
		// add all peers to our set
		seedList().addAll(msg.peerList());
		
		//TODO: notify downloader of newly retrieved peers
	}
}
