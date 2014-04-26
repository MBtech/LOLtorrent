package filesharing.test;

import java.io.IOException;

import filesharing.core.client.FileClient;
import filesharing.core.client.FileTransfer;
import filesharing.core.tracker.TrackerDaemon;
import filesharing.exception.NoMetadataException;

/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author Muhammad Bilal
 *
 */
public class Testing {
	
	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		
		// spawn a tracker
		TrackerDaemon t = new TrackerDaemon("/tmp/t", "T"); // XXX
		t.start();
		System.out.println(t);
		
		// must wait a bit - seedFile method calls are asynchronous
		Thread.sleep(2000);
		
		// seeder 1 seeds file
		FileClient s1 = new FileClient("/tmp/c1", "S1"); // XXX
		s1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		s1.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// seeder 2 seeds file
		FileClient s2 = new FileClient("/tmp/c1", "S2"); // XXX
		s2.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		s2.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// seeder 3 seeds file
		FileClient s3 = new FileClient("/tmp/c1", "S2"); // XXX
		s3.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		s3.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX

		// client 1 downloads the file
		FileClient c1;
		while(true) {
			try {
				c1 = new FileClient("/tmp/c2", "C1"); // XXX
				c1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
				c1.downloadFile("bigfile"); // XXX
				c1.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE);
				break;
			}
			catch(NoMetadataException e) {
				// unable to retrieve file metadata, wait just a bit and try again
				Thread.sleep(100);
			}
		}
		
		// force quit of the application
		Thread.sleep(5000);
		System.exit(0);
		
	}
}
