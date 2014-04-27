package filesharing.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.RandomStringUtils;

import filesharing.core.connection.TrackerConnection;

/**
 * This is a client - it can seed and download files from other clients
 * It connects to a tracker for peer discovery
 */
public class FileClient implements Serializable {
	
	/**
	 * Extension for files storing client state
	 */
	public static final String FILE_EXTENSION = ".client";
	
	/**
	 * A local identifier for the client
	 */
	private String id;
	
	/**
	 * The working directory for the client
	 */
	private File workingDir;
	
	/**
	 * List of trackers to connect by default (should it be here...?)
	 */
	private Set<TrackerConnection> trackerList = new TreeSet<TrackerConnection>();
	
	/**
	 * List of files in the client
	 * Indexed by filename for faster searching
	 */
	private transient Map<String, FileTransfer> fileList = new HashMap<String, FileTransfer>();
	
	/**
	 * List of file namess in the client
	 * Indexed by filename for faster searching
	 */
	private Set<String> filenameList = new TreeSet<String>();

	/**
	 * Determines if client shall log messages or not
	 */
	private boolean isLogging = true;
	
	/**
	 * Creates a client with the specified ID
	 * @param id a string identifier
	 */
	public FileClient(String workingDir, String id) {
		this.id = id;
		this.workingDir = new File(workingDir);
	}
	
	/**
	 * Creates a client with a random ID
	 */
	public FileClient(String workingDir) {
		this(workingDir, RandomStringUtils.randomAlphabetic(5));
	}
	
	/**
	 * Add a tracker to the list
	 * @param address tracker hostname
	 * @param port tracker port
	 */
	public void addTracker(String address, int port) {
		trackerList.add(new TrackerConnection(address, port));
	}
	
	/**
	 * Returns the ID of this client
	 * @return the string identifier
	 */
	public String id() {
		return id;
	}
	
	/**
	 * Returns the list of filenames being transfered in this client
	 * @return the string identifier
	 */
	public Set<String> filenameList() {
		return filenameList;
	}
	
	/**
	 * Returns the default tracker list for the client
	 * @return tracker list
	 */
	public Set<TrackerConnection> trackerList() {
		return trackerList;
	}
	
	/**
	 * Returns pointer to local file in the workspace
	 * @param filename name of the file
	 * @return local file
	 */
	public File getLocalFile(String filename) {
		return new File(workingDir + File.separator + filename);
	}
	
	/**
	 * Adds a file to the list of files, if it is not present
	 * @param filename name of the file
	 * @throws IOException
	 */
	public void addFile(String filename, Set<TrackerConnection> trackers) throws IOException {
		if(!fileList.containsKey(filename)) {
			// nope - add a new entry to the list
			File file = getLocalFile(filename);
			FileTransfer fileTransfer = new FileTransfer(this, filename, file);
			filenameList.add(filename);
			fileList.put(filename, fileTransfer);
		}
		fileList.get(filename).addTrackers(trackers);
		saveState();
	}
	
	/**
	 * Adds a file to the list of files, if not present
	 * @param filename name of the file
	 * @throws IOException
	 */
	public void addFile(String filename) throws IOException {
		addFile(filename, trackerList);
	}
	
	/**
	 * Adds a file for download, specifying tracker list
	 * @param filename the name of the file to download
	 * @param trackers the list of trackers to be used for the file
	 * @throws IOException 
	 */
	public void downloadFile(String filename, Set<TrackerConnection> trackers) throws IOException {
		// add file to list
		addFile(filename, trackers);
		// start download
		fileList.get(filename).loadMetadataFromPeers();
		fileList.get(filename).startDownload();
	}
	
	/**
	 * Adds a file for download, using the default tracker list
	 * @param filename name of the file to download
	 * @throws IOException 
	 */
	public void downloadFile(String filename) throws IOException {
		downloadFile(filename, trackerList);
	}
	
	/**
	 * Add file for seeding in specified list of trackers
	 * @param filename name of the file to be seeded
	 * @param blockSize size of the file blocks to be used
	 * @param trackers list of trackers
	 * @throws IOException 
	 */
	public void seedFile(String filename, int blockSize, Set<TrackerConnection> trackers) throws IOException {
		// add file to list
		addFile(filename, trackers);
		// start seeding
		fileList.get(filename).loadMetadataFromDisk();
		fileList.get(filename).startSeeder();
	}
	
	/**
	 * Add file for seeding with default tracker list
	 * @param filename name of the file to be seeded
	 * @param blockSize size of the file blocks to be used
	 * @throws IOException
	 */
	public void seedFile(String filename, int blockSize) throws IOException {
		seedFile(filename, blockSize, trackerList);
	}
	
	/**
	 * Returns the working directory for this client
	 * @return current working directory
	 */
	public String workingDirectory() {
		return workingDir.getAbsolutePath();
	}
	
	/**
	 * Saves client state in persistent storage
	 * @throws IOException
	 */
	public void saveState() throws IOException {
		File file = new File(workingDirectory() + File.separator + id() + FILE_EXTENSION);
		
		// write transfer states as well
		for(FileTransfer transfer : fileList.values()) {
			transfer.saveState();
		}
		
		// create new file if it doesnt exist
		file.createNewFile();
		
		// dump client state into file
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
		os.writeObject(this);
		os.close();
	}

	/**
	 * Loads client state from persistent storage
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadState() throws IOException, ClassNotFoundException {
		File file = new File(workingDirectory() + File.separator + id() + FILE_EXTENSION);
		
		// load client state from file
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
		FileClient client = (FileClient) is.readObject();
		is.close();
		
		// copy attributes
		trackerList = client.trackerList();
		filenameList = client.filenameList();
		
		// rebuild transient attributes
		fileList = new HashMap<String, FileTransfer>();
		for(String filename : client.filenameList()) {
			addFile(filename);
		}
	}
	
	/**
	 * Returns a textual representation of the object
	 */
	public String toString() {
		String nl = System.lineSeparator();
		String client = "";
		String files = "";
		
		// get client description
		client += " id=" + id();
		client += " dir=" + workingDirectory();
		
		// get files description
		for(Entry<String, FileTransfer> entry : fileList.entrySet()) {
			files += nl + "- " + entry.getValue();
		}
		
		return "[CLIENT" + client + "]" + files;
	}
	
	/**
	 * Enables or disables logging for this client
	 * @param logging true if enabling, false if disabling
	 */
	public void setLogging(boolean logging) {
		this.isLogging = logging;
	}

	/**
	 * Logs a message into console
	 * @param msg message to log
	 */
	protected void log(String msg) {
		if(isLogging) {
			System.out.println("[CLIENT id=" + id + "] " + msg);
		}
	}
	
}
