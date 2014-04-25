package filesharing.core.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filesharing.core.connection.TrackerConnection;
import filesharing.core.processor.TrackerResponseProcessor;
import filesharing.exception.RequestFailedException;
import filesharing.exception.TrackerErrorException;
import filesharing.message.tracker.request.RegisterPeerRequestMessage;
import filesharing.message.tracker.request.TrackerRequestMessage;
import filesharing.message.tracker.response.PeerListResponseMessage;
import filesharing.message.tracker.response.SuccessResponseMessage;
import filesharing.message.tracker.response.TrackerErrorResponseMessage;

/**
 * This is spawned by FileSeeder instances
 * Processes requests from a single peer
 */
public class FileSeeder implements Runnable, TrackerResponseProcessor {

	/**
	 * The thread that runs this seeder instance
	 */
	Thread runner_thread = new Thread(this);
	
	/**
	 * Pool of peer request handler threads
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * File transfer associated with this downloader
	 */
	FileTransfer file_transfer;
	
	/**
	 * A socket waiting for peer requests
	 */
	ServerSocket server_socket;
	
	/**
	 * Constructor of a file seeder
	 * @param file_info information of the file to be seeded
	 * @throws IOException
	 */
	public FileSeeder(FileTransfer file_info) throws IOException {
		this.file_transfer = file_info;
		server_socket = new ServerSocket(0); // bind to a random port
	}
	
	/**
	 * Starts execution of the file seeder in a new thread
	 */
	public void start() {
		runner_thread.start();
	}
	
	/**
	 * Returns the port number for this seeder
	 * @return port number
	 */
	public int getDataPort() {
		return server_socket.getLocalPort();
	}

	/**
	 * Main method for the file seeder
	 */
	@Override
	public void run() {
		log("Running on port " + server_socket.getLocalPort());
		
		// register with trackers
		for(TrackerConnection tracker : file_transfer.getTrackers()) {
			try {
				TrackerRequestMessage msg = new RegisterPeerRequestMessage(file_transfer.filename(), getDataPort());
				tracker.sendMessage(msg, this);
			} catch (IOException | RequestFailedException e) {
				log("failed to register with " + tracker);
			}
		}
		
		// listen for requests
		while(true) {
		try {
				// accept incomming connections
				Socket client_socket = server_socket.accept();
				log("new connection from " + client_socket.getRemoteSocketAddress());
				// create a new connection handler and run in a separate thread
				FileSeederThread handler = new FileSeederThread(file_transfer, client_socket);
				executor.execute(handler);
			} catch (IOException e) {
				log("Problem accepting a connection. " + e.getMessage());
			}
		}
	}
	
	protected void log(String msg) {
		file_transfer.log("[SEED] " + msg);
	}

	@Override
	public void processSuccessResponseMessage(SuccessResponseMessage msg) {
		// success! nothing to do
	}

	@Override
	public void processTrackerErrorResponseMessage(TrackerErrorResponseMessage msg) throws TrackerErrorException {
		/* hmm... just ignore errors from tracker */
		/* or should we also throw tracker error exceptions? */
		log("tracker returned " + msg.reason());
	}

	@Override
	public void processPeerListResponseMessage(PeerListResponseMessage msg) {
		log("tracker returned " + msg);
	}

}
