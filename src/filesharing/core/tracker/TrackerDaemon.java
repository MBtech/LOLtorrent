package filesharing.core.tracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import filesharing.core.client.PeerInformation;

public class TrackerDaemon implements Runnable {
	
	/**
	 * Default tracker listen port
	 */
	public static final short DEFAULT_TRACKER_PORT = 30000;
	
	/**
	 * The thread that runs this tracker instance
	 */
	private Thread tracker_thread = new Thread(this);
	
	/**
	 * A pool of tracker request handler threads
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Tracker listen socket
	 */
	private ServerSocket server_socket;
	
	/**
	 * Contains a list of peers for each file
	 */
	static Hashtable<String,Set<PeerInformation>> peer_record = new Hashtable<String,Set<PeerInformation>>();
	
	/**
	 * Setup a tracker in user defined port
	 * @param port tracker listen port
	 * @throws IOException
	 */
	public TrackerDaemon(int port) throws IOException {
		server_socket = new ServerSocket(port);
	}
	
	/**
	 * Setup a tracker in the default port
	 * @throws IOException
	 */
	public TrackerDaemon() throws IOException {
		this(DEFAULT_TRACKER_PORT);
	}
	
	/**
	 * Returns the list of peers for all files currently being tracked
	 * @return the peer records for all files
	 */
	public Hashtable<String, Set<PeerInformation>> peerRecord() {
		return peer_record;
	}

	/**
	 * Tracker thread - listens for new connections
	 */
	@Override
	public void run() {
		log("Running on port " + server_socket.getLocalPort());
		while(true) {
			try {
				// accept incomming connections
				Socket client_socket = server_socket.accept();
				//log(client_socket.getRemoteSocketAddress() + ": connected");
				// create a new connection handler and run in a separate thread
				TrackerRequestHandler handler = new TrackerRequestHandler(this, client_socket);
				executor.execute(handler);
			} catch (IOException e) {
				log("Oops there was some problem accepting a connection");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Starts the tracker in its own thread
	 */
	public void start() {
		tracker_thread.start();
	}
	
	protected void log(String msg) {
		System.out.println("[TRACKER] " + msg);
	}
	
}
