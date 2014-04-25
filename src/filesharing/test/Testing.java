package filesharing.test;

import java.io.IOException;

import filesharing.core.client.Client;
import filesharing.core.client.FileTransfer;
import filesharing.core.tracker.TrackerDaemon;

/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author Muhammad Bilal
 *
 */
public class Testing {
	
	public static void main (String [] args) throws IOException, InterruptedException {
		
		// spawn a tracker
		TrackerDaemon t = new TrackerDaemon();
		t.start();
		
		// client 0 seeds file
		Client c0 = new Client("0");
		c0.setWorkingDirectory("/tmp/c1"); // XXX
		c0.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c0.seedFile("/tmp/c1/irs.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// client 1 seeds file
		Client c1 = new Client("1");
		c1.setWorkingDirectory("/tmp/c1"); // XXX
		c1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c1.seedFile("/tmp/c1/irs.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// client 2 downloads the file from client 1
		Client c2 = new Client("2");
		c2.setWorkingDirectory("/tmp/c2"); // XXX
		c2.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c2.downloadFile("irs.txt"); // XXX

		// print information every second
		for(int i=0; i<=2; i++) {
//			System.out.println("");
//			System.out.println("@@ << t = " + i + " seconds >> @@");
//			System.out.println(c0);
//			System.out.println(c1);
//			System.out.println(c2);
//			System.out.println("");
			Thread.sleep(1000);
		}
		
		// force quit of the application
		System.exit(0);
		
	}
}
