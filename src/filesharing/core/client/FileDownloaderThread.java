package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.BitSet;

import filesharing.core.PeerResponseProcessor;
import filesharing.core.exception.DownloadCompleteException;
import filesharing.core.exception.NoNewBlocksForDownloadException;
import filesharing.core.exception.PeerErrorException;
import filesharing.core.message.peer.request.BlocksPresentRequestMessage;
import filesharing.core.message.peer.request.FileBlockRequestMessage;
import filesharing.core.message.peer.response.BlocksPresentResponseMessage;
import filesharing.core.message.peer.response.FileBlockResponseMessage;
import filesharing.core.message.peer.response.FileMetadataResponseMessage;
import filesharing.core.message.peer.response.PeerErrorResponseMessage;
import filesharing.core.message.peer.response.PeerResponseMessage;

public class FileDownloaderThread implements Runnable, PeerResponseProcessor {
	
	/**
	 * The parent downloader
	 */
	private FileDownloader downloader;
	
	/**
	 * The peer to connect to and make requests
	 */
	private PeerInformation peer_information;
	
	/**
	 * Set of blocks present in the peer
	 */
	private BitSet peer_blocks_present;
	
	/**
	 * Constructs a new downloader thread
	 * @param downloader the parent downloader
	 * @param peer_information information about the peer to connect to
	 */
	public FileDownloaderThread(FileDownloader downloader, PeerInformation peer_information) {
		this.downloader = downloader;
		this.peer_information = peer_information;
		this.peer_blocks_present = new BitSet(downloader.getFileTransfer().numBlocks());
	}
	
	protected BitSet getPeerBlocksPresent() {
		return peer_blocks_present;
	}
	
	/**
	 * updates peer information about the blocks he has
	 * FIXME: passing a socket is awful!!
	 * @param sock an open socket to the client
	 * @throws ClassNotFoundException if message received was garbage
	 */
	private void updatePeerBlocks(Socket sock) throws IOException, ClassNotFoundException {
		String filename = downloader.getFileTransfer().filename();
		ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
		ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
		os.writeObject(new BlocksPresentRequestMessage(filename));
		PeerResponseMessage msg = (PeerResponseMessage) is.readObject();
		msg.accept(this);
	}

	/**
	 * The main loop - requesting pieces until download is complete
	 */
	@Override
	public void run() {
		try {
			// setup
			String filename = downloader.getFileTransfer().filename();
			PeerResponseMessage msg;

			// connect to peer
			Socket sock = peer_information.connect();
			ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
			
			// check which blocks are present in peer
			os.writeObject(new BlocksPresentRequestMessage(filename));
			msg = (PeerResponseMessage) is.readObject();
			msg.accept(this);
			
			// download the blocks
			while(true) {
				try {
					int block_index = downloader.getBlockIndexForDownload(this);
					os.writeObject(new FileBlockRequestMessage(filename, block_index));
					msg = (PeerResponseMessage) is.readObject();
					msg.accept(this);
				}
				catch(DownloadCompleteException e) {
					// download is complete, our work here is done
					System.out.println("FILE DOWNLOAD COMPLETE YAY!");
					break;
				}
				catch(NoNewBlocksForDownloadException e) {
					// peer is useless, at least for now
					updatePeerBlocks(sock);
					continue;
				}
			}
			
			// close connection to peer
			sock.close();
		} catch (ClassNotFoundException | IOException e) {
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
	 */
	@Override
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) {
		// nothing to do - this is not being dealt with here
		// FIXME but probably should!
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
