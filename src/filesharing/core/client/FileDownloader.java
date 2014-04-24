package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filesharing.core.PeerResponseProcessor;
import filesharing.core.TrackerResponseProcessor;
import filesharing.core.exception.PeerErrorException;
import filesharing.core.exception.RequestFailedException;
import filesharing.core.message.peer.request.FileMetadataRequestMessage;
import filesharing.core.message.peer.response.BlocksPresentResponseMessage;
import filesharing.core.message.peer.response.FileMetadataResponseMessage;
import filesharing.core.message.peer.response.PeerErrorResponseMessage;
import filesharing.core.message.peer.response.PeerResponseMessage;
import filesharing.core.message.tracker.response.PeerListResponseMessage;
import filesharing.core.message.tracker.response.SuccessResponseMessage;
import filesharing.core.message.tracker.response.TrackerErrorResponseMessage;

/**
 * File metadata and content downloader - makes requests to peers and handles
 * responses
 * @author anatoly
 *
 */
public class FileDownloader implements Runnable, PeerResponseProcessor, TrackerResponseProcessor {
	
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
	
	protected FileTransfer getFileTransfer() {
		return file_transfer;
	}
	
	/**
	 * Updates peer list
	 */
	public void updatePeerList() {
		String filename = file_transfer.filename();
		
		// connect to trackers and ask for peers for this file
		Iterator<TrackerInformation> tracker_iter = file_transfer.getTrackers().iterator();
		while(tracker_iter.hasNext()) {
			TrackerInformation tinfo = tracker_iter.next();
			try {
				tinfo.getPeerList(filename, this);
			}
			catch (IOException | RequestFailedException e) {
				// it failed... that's life, ignore it - neeext!
			}
		}
	}
	
	public void fetchMetadata() throws IOException {
		// setup
		String filename = file_transfer.filename();
		
		// update the peer list of peers
		updatePeerList();
		
		// connect to peers and ask for metadata
		Iterator<PeerInformation> seed_iter = file_transfer.seedList().iterator();
		while(!file_transfer.hasMetadata() && seed_iter.hasNext()) {
			PeerInformation peer_address = seed_iter.next();
			// ask peer for metadata
			try {
				// connect to the peer
				Socket sock = peer_address.connect();
				ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());			
				ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
				
				// send message
				os.writeObject(new FileMetadataRequestMessage(filename));
				
				// receive response
				PeerResponseMessage msg = (PeerResponseMessage) is.readObject();
				msg.accept(this);
				
				sock.close();
			}
			catch(IOException | ClassNotFoundException e) {
				// it failed... that's life, ignore it - neeext!
			}
		}
		
		// check if after all this we indeed have the metadata
		if(!file_transfer.hasMetadata()) {
			// no?! die miserably then
			log("could not fetch metadata for file " + filename);
			throw new RequestFailedException("all the peers were mean to me: could not fetch metadata");
		}
	}
	
	protected void log(String msg) {
		file_transfer.log("[DOWN] " + msg);
	}

	@Override
	public void run() {
		// start a downloader thread for every peer
		for(PeerInformation peer_info : file_transfer.seedList()) {
			executor.execute(new FileDownloaderThread(this, peer_info));
		}
	}

	/*****************************
	 * PROCESS TRACKER RESPONSES *
	 *****************************/
	
	/**
	 * process success response from tracker
	 */
	@Override
	public void processSuccessResponseMessage(SuccessResponseMessage msg) {
		/* nothing to do */
	}

	/**
	 * process error response from tracker
	 */
	@Override
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) {
		/* nothing to do - just ignore errors from tracker */
		/* or should we also throw tracker error exceptions? */
		log("tracker returned " + msg.reason());
	}

	/**
	 * Process peer list response from tracker
	 */
	@Override
	public void processPeerListResponseMessage(PeerListResponseMessage msg) {
		// check which are new peers (remove existing ones from retrieved list)
		msg.peerList().removeAll(file_transfer.seedList());
		
		// add all peers to our set
		file_transfer.seedList().addAll(msg.peerList());
	}
	
	/**************************
	 * PROCESS PEER RESPONSES *
	 **************************/

	/**
	 * Process error responses from peers
	 */
	@Override
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException {
		throw new PeerErrorException(msg.reason());
	}

	/**
	 * Process file metadata response from peer
	 * @throws IOException 
	 */
	@Override
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) throws IOException {
		file_transfer.setMetadata(msg.fileSize(), msg.blockSize());
	}

	@Override
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg) {
		//TODO
		throw new UnsupportedOperationException("not implemented yet");
	}
}
