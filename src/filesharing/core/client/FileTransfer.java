package filesharing.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.Files;

import filesharing.core.connection.TrackerConnection;
import filesharing.exception.NoMetadataException;

/**
 * This represents a file transfer - information about the file (file size,
 * block size, etc.) and the current state of the file transfer (how many blocks
 * have been downloaded, if it is currently downloading/seeding)
 */
public class FileTransfer implements Serializable {
	
	/**
	 * Extension for metadata files with transfer data
	 */
	public static final String FILE_EXTENSION = ".metadata";
	
	/**
	 * Default block size in bytes
	 */
	public static final int DEFAULT_BLOCK_SIZE = 1;//(int) (1024*1024*1.4);
	
	/**
	 * The client with this file transfer
	 */
	private transient FileClient client;
	
	/**
	 * The filename for this file
	 */
	private String filename;
	
	/**
	 * Where the file is stored in local storage
	 */
	private File localFile;
	
	/**
	 * A seeder thread
	 */
	private transient FileSeeder seeder = new FileSeeder(this);
	
	/**
	 * Checks if is currently downloading
	 */
	private boolean isDownloading = false;
	
	/**
	 * Checks if it is currently seeding
	 */
	private boolean isSeeding = false;
	
	/**
	 * A download thread
	 */
	private transient FileDownloader downloader = new FileDownloader(this);
	
	/**
	 * Checks if metadata for the file is loaded
	 */
	private boolean hasMetadata = false;
	
	/**
	 * List of trackers to connect
	 */
	private Set<TrackerConnection> trackerList = new HashSet<TrackerConnection>();
	
	/**
	 * File metadata: file size (in bytes)
	 */
	private long fileSize;
	
	/**
	 * File metadata: file block size (in bytes)
	 */
	private int blockSize = DEFAULT_BLOCK_SIZE;
	
	/**
	 * A bitmap to check for blocks present
	 */
	private BitSet blocksPresent = new BitSet();
	
	/**
	 * Creates a new file transfer
	 * @param filename the global name of the file
	 * @param local_file pointer to local file
	 * @throws IOException 
	 */
	public FileTransfer(FileClient client, String filename, File local_file) throws IOException {
		this.client = client;
		this.filename = filename;
		this.localFile = local_file;
		this.seeder = new FileSeeder(this);
		this.downloader = new FileDownloader(this);
	}
	
	/**
	 * Add a list of trackers for this file
	 * @param new_trackers a collection of tracker information objects
	 */
	public synchronized void addTrackers(Collection<TrackerConnection> new_trackers) {
		trackerList.addAll(new_trackers);
	}
	
	/**
	 * Add a tracker for this file
	 * @param address tracker address
	 * @param port tracker port
	 */
	public synchronized void addTracker(String address, int port) {
		trackerList.add(new TrackerConnection(address, port));
	}
	
	/**
	 * Sets metadata for the file transfer
	 * @param file_size the size of the file
	 * @param block_size the size of the transfer blocks
	 * @throws IOException
	 */
	protected synchronized void setMetadata(long file_size, int block_size) throws IOException {
		// dont set metadata if already present
		if(hasMetadata()) return;
		
		// we now have metadata
		hasMetadata = true;
		
		// initialize metadata
		this.fileSize = file_size;
		this.blockSize = block_size;
	}
	
	/**
	 * Generates file metadata from the local disk.
	 * Blocking method - blocks until metadata is loaded or error occurs.
	 * @throws IOException 
	 */
	public synchronized void loadMetadataFromDisk() throws IOException {
		// check if metadata already loaded
		if(hasMetadata()) return;
		
		// create metadata
		setMetadata(localFile.length(), blockSize);
		
		// set all blocks as present
		blocksPresent.set(0, numBlocks());
	}
	
	/**
	 * Fetches file metadata from remote peers
	 * This method blocks until metadata is loaded or an error occurs.
	 */
	public synchronized void loadMetadataFromPeers() throws IOException {
		// check if metadata already loaded
		if(hasMetadata()) return;

		// fetch metadata from peers
		downloader.fetchMetadata();
		
		// create file if doesn't exist and allocate disk space
		localFile.createNewFile();
		RandomAccessFile file_access = new RandomAccessFile(localFile, "rws");
		file_access.setLength(fileSize());
		file_access.close();
	}
	
	/**
	 * Starts the seeding of the file
	 */
	public synchronized void startSeeder() throws IOException {
		// check if metadata is present
		if(!hasMetadata()) {
			throw new NoMetadataException("file " + filename() + " has no metadata");
		}
		
		// check if already seeding
		if(isSeeding()) {
			return;
		}
		
		// start seeder thread
		this.isSeeding = true;
		seeder.start();
		
	}
	
	/**
	 * Starts downloading the file
	 * @throws IOException 
	 */
	public synchronized void startDownload() throws IOException {
		// check if metadata is present
		if(!hasMetadata()) {
			throw new NoMetadataException("file " + filename() + " has no metadata");
		}
		
		// check if already downloading
		if(isDownloading()) {
			return;
		}
		
		// start downloader thread
		this.isDownloading = true;
		downloader.start();
	}
	
	/**
	 * Saves file transfer state in persistent storage
	 * @throws IOException
	 */
	public void saveState() throws IOException {
		// compute file location
		File file = new File(localFile.getAbsolutePath() + FILE_EXTENSION);
		
		// create new file if it doesnt exist
		file.createNewFile();
		
		// dump file transfer object into file
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
		os.writeObject(this);
		os.close();
	}

	/**
	 * Loads file transfer state from persistent storage
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void loadState() throws IOException, ClassNotFoundException {
		// compute file location
		File file = new File(localFile.getAbsolutePath() + FILE_EXTENSION);
		
		// read object from file
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
		FileTransfer file_transfer = (FileTransfer) is.readObject();
		is.close();
		
		// rebuild transient variables
		seeder = new FileSeeder(this);
		downloader = new FileDownloader(this);
		
		// copy state variables
		isDownloading = file_transfer.isDownloading();
		isSeeding = file_transfer.isSeeding();
		hasMetadata = file_transfer.hasMetadata();
		trackerList = file_transfer.getTrackers();
		fileSize = file_transfer.fileSize();
		blockSize = file_transfer.blockSize();
		blocksPresent = file_transfer.getBlocksPresent();
		
		// resume file transfer
		if(isDownloading()) {
			downloader.start();
		}
		
		if(isSeeding()) {
			seeder.start();
		}
	}
	
	/**
	 * Returns the file size for the file
	 * @return file size
	 */
	public long fileSize() {
		// check if metadata is present
		if(!hasMetadata()) {
			throw new NoMetadataException("file " + filename() + " has no metadata");
		}
		return fileSize;
	}
	
	/**
	 * Returns the block size for the file transfer
	 * @return transfer block size
	 */
	public int blockSize() {
		// check if metadata is present
		if(!hasMetadata()) {
			throw new NoMetadataException("file " + filename() + " has no metadata");
		}
		return blockSize;
	}

	/**
	 * Gets the number of blocks for this file
	 * @return number of blocks
	 */
	public int numBlocks() {
		// check if metadata is present
		if(!hasMetadata()) {
			throw new NoMetadataException("file " + filename() + " has no metadata");
		}
		// compute number of blocks from file size and block size
		// how many "full" blocks are there?
		int num_full_sized_blocks = (int) (fileSize() / blockSize());
		// check if there is a smaller block in the end
		int num_smaller_blocks = ((fileSize()%blockSize() != 0) ? 1 : 0);
		return num_full_sized_blocks + num_smaller_blocks;
	}
	
	/**
	 * Returns the filename
	 * @return filename
	 */
	public String filename() {
		return filename;
	}
	
	/**
	 * Returns the local file
	 * @return file object
	 */
	protected File getLocalFile() {
		return localFile;
	}
	
	/**
	 * Check if metadata has been loaded for this file
	 * @return true if metadata has been loaded, false otherwise
	 */
	public boolean hasMetadata() {
		return hasMetadata;
	}
	
	/**
	 * Check if is currently downloading
	 * @return true if downloading, false otherwise
	 */
	public boolean isDownloading() {
		return isDownloading;
	}
	
	/**
	 * Check if we have all the blocks of the file
	 * @return true if file complete, false otherwise
	 */
	public boolean haveAllBlocks() {
		return numBlocks() == getBlocksPresent().cardinality();
	}
	
	/**
	 * Check if is currently seeding
	 * @return true if seeding, false otherwise
	 */
	public boolean isSeeding() {
		return isSeeding;
	}
	
	/**
	 * Returns the list of trackers
	 * @return set of tracker information objects
	 */
	protected Set<TrackerConnection> getTrackers() {
		return trackerList;
	}
	
	/**
	 * Returns the set of blocks present
	 * @return a bit set of block indices
	 */
	protected BitSet getBlocksPresent() {
		return blocksPresent;
	}
	
	/**
	 * Return a textual representation of this object
	 */
	public String toString() {
		String metadata;
		int num_blocks_present = getBlocksPresent().cardinality();
		if(hasMetadata()) {
			metadata = "downloading? " + (isDownloading() ? "yes" : "no") + ", " +
			           "seeding? " + (isSeeding() ? "yes" : "no") + ", " +
			           "filesize=" + fileSize() + " Bytes, " +
			           "blocksize=" + blockSize() + " Bytes, " +
			           "blocks present=" + num_blocks_present + "/" + numBlocks() + ", " +
			           "numTrackers=" + trackerList.size() + ", " +
			           "numSeeds=" + downloader.seedList().size();
		}
		else {
			metadata = "no metadata";
		}
		return "[FILE] filename=" + filename() + ", " + metadata;
	}

	/**
	 * Log message to console
	 * @param msg
	 */
	protected void log(String msg) {
		client.log("[FILE filename=" + filename + "] " + msg);
	}
	
	/**
	 * Customize Java built-in serialization process
	 * Sockets are not serializable - therefore must be transient
	 * When building back the object, we need to create a new socket
	 * @param ois object input stream
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		this.client = new FileClient(Files.createTempDir().getAbsolutePath());
		this.seeder = new FileSeeder(this);
		this.downloader = new FileDownloader(this);
	}
	
}
