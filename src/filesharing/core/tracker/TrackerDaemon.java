package filesharing.core.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
public class TrackerDaemon implements Runnable, Serializable {
	
	/**
	 * Extension for files storing tracker state
	 */
	public static final String FILE_EXTENSION = ".tracker";
	
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
	private transient Thread trackerThread = new Thread(this);
	
	/**
	 * A pool of tracker request handler threads
	 */
	private transient ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Tracker listen socket
	 */
	private transient ServerSocket serverSocket;
	
	/**
	 * Contains a list of peers for each file
	 */
	private Hashtable<String,Set<PeerConnection>> peerRecord = new Hashtable<String,Set<PeerConnection>>();

	/**
	 * Determines if tracker shall log messages or not
	 */
	private boolean isLogging = true;
	
	/**
	 * Setup a tracker in user defined port
	 * @param port tracker listen port
	 * @throws IOException
	 */
	public TrackerDaemon(String working_dir, String id, int port) throws IOException {
		this.workingDir = new File(working_dir);
		this.serverSocket = new ServerSocket(port);
		this.id = id;
	}
	
	/**
	 * Setup a tracker with a specified ID in the default port
	 * @param id tracker identifier
	 * @throws IOException
	 */
	public TrackerDaemon(String working_dir, String id) throws IOException {
		this(working_dir, id, DEFAULT_TRACKER_PORT);
	}
	
	/**
	 * Setup a tracker with a random ID in user defined port
	 * @param port tracker listen port
	 * @throws IOException
	 */
	public TrackerDaemon(String working_dir, int port) throws IOException {
		this(working_dir, RandomStringUtils.randomAlphabetic(5), port);
	}
	
	/**
	 * Creates a tracker with a random ID in the default port
	 * @throws IOException
	 */
	public TrackerDaemon(String working_dir) throws IOException {
		this(working_dir, RandomStringUtils.randomAlphabetic(5), DEFAULT_TRACKER_PORT);
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
	protected Hashtable<String, Set<PeerConnection>> peerRecord() {
		return peerRecord;
	}
	
	/**
	 * Returns the working directory for this client
	 * @return current working directory
	 */
	public String workingDirectory() {
		return workingDir.getAbsolutePath();
	}
	
	/**
	 * Adds a peer to the hashtable
	 * @param filename name of the file the peer is seeding
	 * @param peer information about how to connect to the peer
	 */
	public synchronized void addPeer(String filename, PeerConnection peer) {
		Set<PeerConnection> filePeers = this.peerRecord.get(filename);
		filePeers.add(peer);
	}
	
	/**
	 * Saves client state in persistent storage
	 * @throws IOException
	 */
	public void saveState() throws IOException {
		File f = new File(workingDirectory() + File.separator + id() + FILE_EXTENSION);
		
		// create new file if it doesnt exist
		f.createNewFile();
		
		// dump client state into file
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
		os.writeObject(this);
		os.close();
	}

	/**
	 * Loads client state from persistent storage
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadState() throws IOException, ClassNotFoundException {
		File f = new File(workingDirectory() + File.separator + id() + FILE_EXTENSION);
		
		// load client state from file
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
		TrackerDaemon client = (TrackerDaemon) is.readObject();
		is.close();
		
		// copy attributes
		this.peerRecord = client.peerRecord();
		
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
				//log("New connection from " + client_socket.getRemoteSocketAddress());
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
	 * Enables or disables logging for this client
	 * @param logging true if enabling, false if disabling
	 */
	public void setLogging(boolean logging) {
		this.isLogging = logging;
	}
	
	/**
	 * Logs a message to console
	 * @param msg message to log
	 */
	protected void log(String msg) {
		if(isLogging) {
			System.out.println("[TRACKER id=" + id() + "] " + msg);
		}
	}
	
	/**
	 * Returns a text representation of the object
	 */
	public synchronized String toString() {
		String nl = System.lineSeparator();
		
		// get list of all peers for all files
		String files = "";
		for(String filename : peerRecord.keySet()) {
			files += "- " + filename + ": ";
			for(PeerConnection peer : peerRecord.get(filename)) {
				files += peer + " ";
			}
			files += nl;
			
		}
		return "[TRACKER]" + nl + files;
	}
	
}
