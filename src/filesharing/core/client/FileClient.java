package filesharing.core.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.common.io.Files;

import filesharing.core.connection.TrackerConnection;

/**
 * This is a client - it can seed and download files from other clients
 * It connects to a tracker for peer discovery
 */
public class FileClient {
	
	/**
	 * A local identifier for the client
	 */
	private String id;
	
	/**
	 * The working directory for the client
	 */
	private File working_dir = Files.createTempDir();
	
	/**
	 * List of trackers to connect by default (should it be here...?)
	 */
	private List<TrackerConnection> tracker_list = new ArrayList<TrackerConnection>();
	
	/**
	 * List of files in the client
	 * Indexed by filename for faster searching
	 */
	private Map<String, FileTransfer> file_list = new HashMap<String, FileTransfer>();
	
	/**
	 * Add a tracker to the list
	 * @param address tracker hostname
	 * @param port tracker port
	 */
	public void addTracker(String address, int port) {
		tracker_list.add(new TrackerConnection(address, port));
	}
	
	/**
	 * Creates a client with a random ID
	 */
	public FileClient() {
		this(RandomStringUtils.randomAlphabetic(5));
	}
	
	/**
	 * Creates a client with the specified ID
	 * @param id a string identifier
	 */
	public FileClient(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the ID of this client
	 * @return the string identifier
	 */
	public String id() {
		return id;
	}
	
	/**
	 * Adds a file to the list of files, if it is not present
	 * @param filename name of the file
	 * @param file path to local file
	 * @throws IOException
	 */
	private void addFile(String filename, File file, Collection<TrackerConnection> trackers) throws IOException {
		if(!file_list.containsKey(filename)) {
			// nope - add a new entry to the list
			FileTransfer file_info = new FileTransfer(this, filename, file);
			file_list.put(filename, file_info);
		}
		file_list.get(filename).addTrackers(trackers);
	}
	
	/**
	 * Adds a file for download, specifying tracker list
	 * @param filename the name of the file to download
	 * @param trackers the list of trackers to be used for the file
	 * @throws IOException 
	 */
	public void downloadFile(String filename, Collection<TrackerConnection> trackers) throws IOException {
		// resolve path to store file into
		File file = new File(working_dir + File.separator + filename);
		// add file to list
		addFile(filename, file, trackers);
		// start download
		file_list.get(filename).loadMetadataFromPeers();
		file_list.get(filename).startDownload();
	}
	
	/**
	 * Adds a file for download, using the default tracker list
	 * @param filename name of the file to download
	 * @throws IOException 
	 */
	public void downloadFile(String filename) throws IOException {
		downloadFile(filename, tracker_list);
	}
	
	/**
	 * Add file for seeding in specified list of trackers
	 * @param path local path to file to be seeded
	 * @throws IOException 
	 */
	public void seedFile(String path, int block_size, Collection<TrackerConnection> trackers) throws IOException {
		// initialize stuff
		File file = new File(path);
		String filename = file.getName();
		// add file to list
		addFile(filename, file, trackers);
		// start seeding
		file_list.get(filename).loadMetadataFromDisk();
		file_list.get(filename).startSeeder();
	}
	
	/**
	 * Add file for seeding with default tracker list
	 * @param path path to file to be seeded
	 * @throws IOException
	 */
	public void seedFile(String path, int block_size) throws IOException {
		seedFile(path, block_size, tracker_list);
	}
	
	/**
	 * Changes working directory for this client
	 * @param path new working directory
	 */
	public void setWorkingDirectory(String path) {
		working_dir = new File(path);
	}
	
	/**
	 * Returns the working directory for this client
	 * @return current working directory
	 */
	public String workingDirectory() {
		return working_dir.getAbsolutePath();
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
		Iterator<Entry<String, FileTransfer>> it = file_list.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, FileTransfer> entry = it.next();
			files += nl + "- " + entry.getValue();
		}
		return "[CLIENT" + client + "]" + files;
	}

	/**
	 * Logs a message into console
	 * @param msg message to log
	 */
	protected void log(String msg) {
		System.out.println("[CLIENT id=" + id + "] " + msg);
	}
	
}
