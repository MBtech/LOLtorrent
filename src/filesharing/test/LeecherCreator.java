package filesharing.test;

import java.io.IOException;

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
public class LeecherCreator implements Runnable {
	private String dirPath, instName;
	public LeecherCreator(String Dirpath, String Name) throws IOException, InterruptedException, ClassNotFoundException{
		this.dirPath = Dirpath;
		this.instName = Name;
	}

	@Override
	public void run(){
		// TODO Auto-generated method stub
		
			// client 1 downloads and seeds the file
			FileClient c1 = new FileClient(dirPath, instName);

			// load client state from previous session
			try {
				c1.loadState();
			}
			catch(IOException e) { /* failed */ } catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// add tracker
			c1.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);

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
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(1300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
