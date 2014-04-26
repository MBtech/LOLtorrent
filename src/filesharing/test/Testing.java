package filesharing.test;

import java.io.IOException;

import filesharing.core.client.FileClient;
import filesharing.core.client.FileTransfer;
import filesharing.core.tracker.TrackerDaemon;

/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author Muhammad Bilal
 *
 */
public class Testing {
	
	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		
		// spawn a tracker
		TrackerDaemon t = new TrackerDaemon("T");
		t.setWorkingDirectory("/tmp/t"); // XXX
		t.start();
		
		// client 0 seeds file
		FileClient c0 = new FileClient("/tmp/c1", "0"); // XXX
		c0.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c0.seedFile("irs.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// client 1 seeds file
		FileClient c1 = new FileClient("/tmp/c1", "1"); // XXX
		c1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c1.seedFile("irs.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// must wait a bit - seedFile method calls are asynchronous
		Thread.sleep(1000);
		
		// client 2 downloads the file from client 1
		FileClient c2 = new FileClient("/tmp/c2", "2"); // XXX
		c2.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c2.downloadFile("irs.txt"); // XXX
		try {
			System.out.println();
			System.out.println("C2");
			System.out.println(c2);
			System.out.println();
			
			c2.saveState();
			FileClient c3 = new FileClient("/tmp/c2", "2");
			c3.loadState();
			
			System.out.println();
			System.out.println("C2 copy");
			System.out.println(c3);
			System.out.println();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

//		// print information every second
//		for(int i=0; i<=2; i++) {
////			System.out.println("");
////			System.out.println("@@ << t = " + i + " seconds >> @@");
////			System.out.println(c0);
////			System.out.println(c1);
////			System.out.println(c2);
////			System.out.println("");
//			Thread.sleep(1000);
//		}
//		
//		// force quit of the application
		Thread.sleep(100);
		System.exit(0);
		
	}
}
