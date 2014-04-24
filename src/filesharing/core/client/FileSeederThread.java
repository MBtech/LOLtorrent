package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.BitSet;

import filesharing.core.PeerRequestProcessor;
import filesharing.core.message.peer.request.BlocksPresentRequestMessage;
import filesharing.core.message.peer.request.FileBlockRequestMessage;
import filesharing.core.message.peer.request.FileMetadataRequestMessage;
import filesharing.core.message.peer.request.PeerRequestMessage;
import filesharing.core.message.peer.response.BlocksPresentResponseMessage;
import filesharing.core.message.peer.response.FileBlockResponseMessage;
import filesharing.core.message.peer.response.FileMetadataResponseMessage;
import filesharing.core.message.peer.response.PeerErrorResponseMessage;
import filesharing.core.message.tracker.response.TrackerErrorResponseMessage;

public class FileSeederThread implements Runnable, PeerRequestProcessor {
		
	/**
	 * The file information for this handler
	 */
	FileTransfer file_transfer;
	
	/**
	 * Client socket to wait for requests
	 */
	Socket sock;
	ObjectInputStream is; // socket object input stream, for convenience
	ObjectOutputStream os; // socket object output stream, for convenience
	
	/**
	 * Create a new tracker request handler listening in the specified socket
	 * @param sock socket for listening
	 * @throws IOException 
	 */
	public FileSeederThread(FileTransfer file_transfer, Socket sock) throws IOException {
		this.file_transfer = file_transfer;
		this.sock = sock;
		this.is = new ObjectInputStream(sock.getInputStream());
		this.os = new ObjectOutputStream(sock.getOutputStream());
	}

	/**
	 * Client request handler main thread - processes requests from a single client
	 */
	@Override
	public void run() {
		try {
			// read requests, process them and return the response
			while(true) {
					// read request
					PeerRequestMessage msg = (PeerRequestMessage) is.readObject();
					// process request
					log(msg.toString());
					msg.accept(this);
			}
		}
		catch (IOException | ClassNotFoundException e) {
			// just exit silently
		}
	}
	
	public FileTransfer fileTransfer() {
		return file_transfer;
	}
	
	protected void log (String msg) {
		file_transfer.log("[SEED] " + sock.getRemoteSocketAddress() + ": " + msg);
	}

	/**
	 * Process requests for file metadata
	 */
	@Override
	public void processFileMetadataRequestMessage(FileMetadataRequestMessage msg) throws IOException {
		String filename = fileTransfer().filename();
		long file_size = fileTransfer().fileSize();
		int block_size = fileTransfer().blockSize();
		
		// check request parameters
		if(!msg.filename().equals(filename)) {
			// request for a different filename
			os.writeObject(new TrackerErrorResponseMessage("im serving file " + filename +", not " + msg.filename()));
			return;
		}
		
		// send response
		os.writeObject(new FileMetadataResponseMessage(filename, file_size, block_size));
	}

	@Override
	public void processFileBlockRequestMessage(FileBlockRequestMessage msg) throws IOException {
		String filename = fileTransfer().filename();
		
		// check request parameters
		if(!msg.filename().equals(filename)) {
			// request for a different filename
			os.writeObject(new TrackerErrorResponseMessage("im serving file " + filename +", not " + msg.filename()));
			return;
		}
		
		// process request
		byte[] block;
		try {
			block = file_transfer.readBlock(msg.blockNumber());
		}
		catch (IOException e) {
			os.writeObject(new PeerErrorResponseMessage("error reading file block, sorry"));
			return;
		}
		
		// send response
		os.writeObject(new FileBlockResponseMessage(filename, msg.blockNumber(), block));
	}

	@Override
	public void processBlocksPresentRequestMessage(BlocksPresentRequestMessage msg) throws IOException {
		String filename = fileTransfer().filename();
		BitSet blocks_present = fileTransfer().getBlocksPresent();
		
		// check request parameters
		if(!msg.filename().equals(filename)) {
			// request for a different filename
			os.writeObject(new PeerErrorResponseMessage("im serving file " + filename +", not " + msg.filename()));
			return;
		}
		
		// send response
		os.writeObject(new BlocksPresentResponseMessage(filename, blocks_present));
	}

}
