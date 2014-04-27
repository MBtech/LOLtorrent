package filesharing.core.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.BitSet;

import filesharing.core.processor.PeerRequestProcessor;
import filesharing.exception.BlockNotPresentException;
import filesharing.message.peer.request.BlocksPresentRequestMessage;
import filesharing.message.peer.request.FileBlockRequestMessage;
import filesharing.message.peer.request.FileMetadataRequestMessage;
import filesharing.message.peer.request.PeerRequestMessage;
import filesharing.message.peer.response.BlocksPresentResponseMessage;
import filesharing.message.peer.response.FileBlockResponseMessage;
import filesharing.message.peer.response.FileMetadataResponseMessage;
import filesharing.message.peer.response.PeerErrorResponseMessage;
import filesharing.message.tracker.response.TrackerErrorResponseMessage;

/**
 * This is a seeder thread, a slave for FileSeeder. It processes requests from
 * a single client.
 */
public class FileSeederThread implements Runnable, PeerRequestProcessor {
		
	/**
	 * The file information for this handler
	 */
	FileTransfer fileTransfer;
	
	/**
	 * An object for random access to the local file
	 */
	private RandomAccessFile fileAccess;
	
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
		this.fileTransfer = file_transfer;
		this.sock = sock;
		this.is = new ObjectInputStream(sock.getInputStream());
		this.os = new ObjectOutputStream(sock.getOutputStream());
		fileAccess = new RandomAccessFile(file_transfer.getLocalFile(), "r");
	}
	
	/**
	 * Reads a block from the local file
	 * @param index block number (zero-indexed)
	 * @return a byte array with the block contents
	 * @throws IOException on read operation failure
	 */
	protected synchronized byte[] readBlock(int index) throws IOException {
		// check if we have the block
		if(!fileTransfer.getBlocksPresent().get(index)) {
			throw new BlockNotPresentException("dont have block " + index);
		}
		
		// initialize
		// the last block's size may be different. if this is the last block, its
		// size is the division remainder between filesize%blocksize
		int num_blocks = fileTransfer.numBlocks();
		int file_size = (int) fileTransfer.fileSize();
		int block_size = fileTransfer.blockSize();
		int size = ((index==num_blocks-1) ? (file_size%block_size) : block_size);
		byte[] block = new byte[size];
		
		// process
		this.fileAccess.seek(block_size*index); // move file pointer
		fileAccess.read(block); // read
		return block;
	}

	/**
	 * Client request handler main thread - processes requests from a single client
	 */
	@Override
	public void run() {
		try {
			// read requests, process them and return the response
			while(fileTransfer.isSeeding()) {
					// read request
					PeerRequestMessage msg = (PeerRequestMessage) is.readObject();
					// process request
					//log(msg.toString());
					msg.accept(this);
			}
		}
		catch (IOException | ClassNotFoundException e) {
			// just exit silently
		}
	}
	
	public FileTransfer fileTransfer() {
		return fileTransfer;
	}
	
	protected void log (String msg) {
		fileTransfer.log("[SEED] " + sock.getRemoteSocketAddress() + ": " + msg);
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
			block = readBlock(msg.blockNumber());
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
