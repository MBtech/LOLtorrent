package filesharing.test;
import java.io.File;
import java.io.IOException;

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

	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		int nargs = args.length;
		String ptype;
		if (nargs<1)
		{
			System.out.println("Please provide more arguments");
			System.out.println("Usage: Program PeerType");
			return;
		}
		//Convert input arguments from string to integers
		ptype = args[0];

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
			FileClient s = new FileClient("./tmp/c1", new String("S")); // XXX
			s.setLogging(true);
			s.addTracker("209.208.109.252", TrackerDaemon.DEFAULT_TRACKER_PORT);
			s.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
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
			c1.addTracker("209.208.109.252", TrackerDaemon.DEFAULT_TRACKER_PORT);

			// try to download and then seed the file
			while(true) {
				try {
					c1.downloadFile("bigfile"); // XXX
					c1.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE);
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
		}

		// force quit of the application
		Thread.yield();
		while(true){}

	}
}
