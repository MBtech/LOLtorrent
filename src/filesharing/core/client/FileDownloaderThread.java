package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import filesharing.core.PeerResponseProcessor;
import filesharing.core.exception.PeerErrorException;
import filesharing.core.message.peer.request.BlocksPresentRequestMessage;
import filesharing.core.message.peer.response.BlocksPresentResponseMessage;
import filesharing.core.message.peer.response.FileMetadataResponseMessage;
import filesharing.core.message.peer.response.PeerErrorResponseMessage;
import filesharing.core.message.peer.response.PeerResponseMessage;

public class FileDownloaderThread implements Runnable, PeerResponseProcessor {
	
	/**
	 * The parent downloader
	 */
	FileDownloader downloader;
	
	/**
	 * The peer to connect to and make requests
	 */
	PeerInformation peer_information;
	
	/**
	 * Constructs a new downloader thread
	 * @param downloader the parent downloader
	 * @param peer_information information about the peer to connect to
	 */
	public FileDownloaderThread(FileDownloader downloader, PeerInformation peer_information) {
		this.downloader = downloader;
		this.peer_information = peer_information;
	}

	/**
	 * The main loop - requesting pieces until download is complete
	 */
	@Override
	public void run() {
		try {
			// setup
			String filename = downloader.getFileTransfer().filename();

			// connect to peer
			Socket sock = peer_information.connect();
			ObjectOutputStream os = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
			// do stuff
			os.writeObject(new BlocksPresentRequestMessage(filename));
			PeerResponseMessage msg = (PeerResponseMessage)is.readObject();
			msg.accept(this);
			
			// close connection to peer
			sock.close();
		} catch (ClassNotFoundException | IOException e) {
			// something bad happened
			e.printStackTrace();
			downloader.log(e.getMessage());
		}
	}

	@Override
	public void processPeerErrorResponseMessage(PeerErrorResponseMessage msg) throws PeerErrorException {
		throw new PeerErrorException(msg.reason());
	}

	@Override
	public void processFileMetadataResponseMessage(FileMetadataResponseMessage msg) {
		// nothing to do - this is not being dealt with here
	}

	@Override
	public void processBlocksPresentResponseMessage(BlocksPresentResponseMessage msg) {
		System.out.println("got blocks!");
		System.out.println(msg);
	}

}
