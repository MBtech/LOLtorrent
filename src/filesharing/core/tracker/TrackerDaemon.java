package filesharing.core.tracker;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.io.Files;

import filesharing.core.connection.PeerConnection;

/**
 * This represents a tracker instance - stores lists of files and lists of peers
 * for each of the files
 */
public class TrackerDaemon implements Runnable {
	
	/**
	 * Default tracker listen port
	 */
	public static final short DEFAULT_TRACKER_PORT = 30000;
	
	/**
	 * An identifier for the tracker
	 */
	private String id;
	
	/**
	 * The working directory for the client
	 */
	private File workingDir = Files.createTempDir();
	
	/**
	 * The thread that runs this tracker instance
	 */
	private Thread trackerThread = new Thread(this);
	
	/**
	 * A pool of tracker request handler threads
	 */
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Tracker listen socket
	 */
	private ServerSocket serverSocket;
	
	/**
	 * Contains a list of peers for each file
	 */
	static Hashtable<String,Set<PeerConnection>> peerRecord = new Hashtable<String,Set<PeerConnection>>();
	
	/**
	 * Setup a tracker in user defined port
	 * @param port tracker listen port
	 * @throws IOException
	 */
	public TrackerDaemon(String id, int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.id = id;
	}
	
	/**
	 * Setup a tracker with a specified ID in the default port
	 * @param id tracker identifier
	 * @throws IOException
	 */
	public TrackerDaemon(String id) throws IOException {
		this(id, DEFAULT_TRACKER_PORT);
	}
	
	/**
	 * Setup a tracker with a random ID in user defined port
	 * @param port tracker listen port
	 * @throws IOException
	 */
	public TrackerDaemon(int port) throws IOException {
		this(RandomStringUtils.randomAlphabetic(5), port);
	}
	
	/**
	 * Creates a tracker with a random ID in the default port
	 * @throws IOException
	 */
	public TrackerDaemon() throws IOException {
		this(RandomStringUtils.randomAlphabetic(5), DEFAULT_TRACKER_PORT);
	}
	
	/**
	 * Returns the tracker identifier
	 * @return tracker identifier
	 */
	public String id() {
		return id;
	}
	
	/**
	 * Returns the list of peers for all files currently being tracked
	 * @return the peer records for all files
	 */
	public Hashtable<String, Set<PeerConnection>> peerRecord() {
		return peerRecord;
	}
	
	/**
	 * Changes working directory for this client
	 * @param path new working directory
	 */
	public void setWorkingDirectory(String path) {
		workingDir = new File(path);
	}
	
	/**
	 * Returns the working directory for this client
	 * @return current working directory
	 */
	public String workingDirectory() {
		return workingDir.getAbsolutePath();
	}

	/**
	 * Tracker thread - listens for new connections
	 */
	@Override
	public void run() {
		log("Running on port " + serverSocket.getLocalPort());
		while(true) {
			try {
				// accept incomming connections
				Socket client_socket = serverSocket.accept();
				// create a new connection handler and run in a separate thread
				TrackerRequestHandler handler = new TrackerRequestHandler(this, client_socket);
				executor.execute(handler);
			} catch (IOException e) {
				log("Problem accepting a connection");
			}
		}
	}
	
	/**
	 * Starts the tracker in its own thread
	 */
	public void start() {
		trackerThread.start();
	}
	
	/**
	 * Logs a message to console
	 * @param msg message to log
	 */
	protected void log(String msg) {
		System.out.println("[TRACKER id=" + id() + "] " + msg);
	}
	
}
