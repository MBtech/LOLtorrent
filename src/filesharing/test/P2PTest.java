package filesharing.test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;

import filesharing.core.client.FileClient;
import filesharing.core.client.FileTransfer;
import filesharing.core.tracker.TrackerDaemon;
import filesharing.exception.NoMetadataException;


/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author
 *
 */
public class P2PTest {
	
	private static void createFile(String filePath, int fileSize) throws IOException {
		File f = new File(filePath);
		ObjectOutputStream Oos;
		//Check if the file exists. If not, create it
		if(!f.exists())
		{
			Oos = new ObjectOutputStream(new FileOutputStream(f));
			for (int i=0; i<fileSize*16330; i++)
			{
				Oos.write("This is a test file and this is a test line to file up 64 bytes.".getBytes());
			}
			Oos.close();
		}
	}

	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		int nargs = args.length;
		String ptype, trackerIP = "127.0.0.1";
		int fileSize = 32;
		if (nargs<1)
		{
			System.out.println("Please provide more arguments");
			System.out.println("Usage: Program PeerType Tracker_IP[Optional] File_Size[Optional]");
			return;
		}
		//Convert input arguments from string to integers
		ptype = args[0];
		if (ptype.compareTo("tracker")!=0){
			if (nargs<2)
			{
			System.out.println("Please provide Tracker IP to run seeder or leecher");
			System.out.println("Usage: Program PeerType Tracker_IP[Optional] File_Size[Optional]");
			return;
			}
			else
			{
				// Extract the trackerIP from input arguments
				trackerIP = args[1];
			}
		}
		if (nargs>2){
			fileSize = Integer.parseInt(args[2]);
		}
		File tmpdir = new File(new String("./tmp"));
		tmpdir.mkdir();
		if (ptype.compareTo("tracker")==0){
			File dir = new File(new String("./tmp/t"));
			dir.mkdir();
			// spawn a tracker with the specified tracker ID and working directory
			TrackerDaemon t = new TrackerDaemon("./tmp/t", "T1"); // XXX
			t.setLogging(true);
			t.start();
		}
		else if (ptype.compareTo("seeder")==0){
			// Instantiate required amount of seeders 
			File dir = new File(new String("./tmp/c1"));
			dir.mkdir();
			createFile("./tmp/c1/bigfile_"+fileSize, fileSize);
			FileClient s = new FileClient("./tmp/c1", new String("S")); // XXX
			s.setLogging(true);
			s.addTracker(trackerIP, TrackerDaemon.DEFAULT_TRACKER_PORT);
			s.seedFile("bigfile_"+fileSize, FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		}
		else if (ptype.compareTo("leecher")==0){
			File dir = new File(new String("./tmp/c"));
			FileUtils.deleteDirectory(dir);
			dir.mkdir();
			FileClient c1 = new FileClient("./tmp/c", "C");

			// load client state from previous session
			try{
				c1.loadState();
			}
			catch(IOException | NoMetadataException e){/*File not found*/}
			
			// add tracker
			c1.addTracker(trackerIP, TrackerDaemon.DEFAULT_TRACKER_PORT);

			// try to download and then seed the file
			while(true) {
				try {
					c1.downloadFile("bigfile_"+fileSize); // XXX
					c1.seedFile("bigfile_"+fileSize, FileTransfer.DEFAULT_BLOCK_SIZE);
					break;
				}
				catch(NoMetadataException e) {
					// unable to retrieve file metadata, wait just a bit and try again
					System.out.println("no metadata yet");
					Thread.sleep(200);
				}
			}
			Thread.sleep(1300);
		}
		else
		{
			System.out.println("Incorrect node type. Node type can only be: torrent, seeder or leecher");
			System.exit(-1);
		}

		// force quit of the application
		Thread.yield();
		while(true){}

	}
}
