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
		FileClient s1 = new FileClient("/tmp/c1", "S1"); // XXX
		s1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		s1.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		s1.seedFile("hello.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		s1.seedFile("test.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// client 1 seeds file
		FileClient s2 = new FileClient("/tmp/c1", "S2"); // XXX
		s2.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		s2.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		s2.seedFile("hello.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		s2.seedFile("test.txt", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
		
		// must wait a bit - seedFile method calls are asynchronous
		Thread.sleep(1000);
		
		// client 2 downloads the file from client 1
		FileClient c1 = new FileClient("/tmp/c2", "C1"); // XXX
		c1.loadState();
		c1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
		c1.downloadFile("bigfile"); // XXX
		c1.downloadFile("hello.txt"); // XXX
		c1.downloadFile("test.txt"); // XXX
		
//		try {
//			System.out.println();
//			System.out.println("C2");
//			System.out.println(c1);
//			System.out.println();
//			
//			c1.saveState();
//			FileClient c3 = new FileClient("/tmp/c2", "2");
//			c3.loadState();
//			
//			System.out.println();
//			System.out.println("C2 copy");
//			System.out.println(c3);
//			System.out.println();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//		}

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
		Thread.sleep(100);
		c1.saveState();
		System.exit(0);
		
	}
}
