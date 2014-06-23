package filesharing.test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import filesharing.core.client.FileClient;
import filesharing.core.client.FileTransfer;
import filesharing.core.tracker.TrackerDaemon;


/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author
 *
 */
public class Testing {

	public static void main (String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		int nargs = args.length;
		int nseeders, nleechers, fileSize;
		if (nargs<4)
		{
			System.out.println("Please provide more arguments");
			System.out.println("Usage: Program FileSize(in MB) Number_of_Seeds Number_of_Leechers");
			return;
		}
		//Convert input arguments from string to integers
		fileSize = Integer.parseInt(args[1]);
		nseeders = Integer.parseInt(args[2]);
		nleechers = Integer.parseInt(args[3]);

		// Path of the Temporary folder 
		Path tmppath = FileSystems.getDefault().getPath("./tmp/");
		File f = new File("./tmp/t/bigfile");
		ObjectOutputStream Oos;
		//Check if the file exists. If not, create it
		if(!f.exists())
		{
			Oos = new ObjectOutputStream(new FileOutputStream(f));
			for (int i=0; i< fileSize*16384; i++)
			{
				Oos.write("This is a test file and this is a test line to file up 64 bytes.".getBytes());
			}
			Oos.close();
		}

		f = new File("./tmp/c1/bigfile");
		//Check if the file exists. If not, copy it from the original tracker file directory
		if(!f.exists())
		{
			Files.copy(tmppath.resolve("./t/bigfile"), tmppath.resolve("./c1/bigfile"), StandardCopyOption.REPLACE_EXISTING);
		}

		// Delete old downloaded files to conduct new tests
		Collection<File> files = FileUtils.listFilesAndDirs(new File("./tmp/"),new NotFileFilter(TrueFileFilter.INSTANCE),DirectoryFileFilter.INSTANCE);
		for (File f1: files){
			if ((f1.getName().compareTo("tmp"))==0 || (f1.getName().compareTo("t"))==0 || (f1.getName().compareTo("c1"))==0){
				continue;
			}
			else
			{
				FileUtils.deleteDirectory(f1);
			}
		}
		// spawn a tracker with the specified tracker ID and working directory
		TrackerDaemon t = new TrackerDaemon("./tmp/t", "T1"); // XXX
		t.setLogging(false);
		t.start();

		// Instantiate required amount of seeders 
		for (int i=0; i<nseeders; i++){
			ArrayList<FileClient> seederList = new ArrayList<FileClient>();
			FileClient s = new FileClient("./tmp/c1", new String("S"+i)); // XXX
			s.setLogging(false);
			s.addTracker("localhost", TrackerDaemon.DEFAULT_TRACKER_PORT);
			s.seedFile("bigfile", FileTransfer.DEFAULT_BLOCK_SIZE); // XXX
			seederList.add(s);
		}

		// Instantiate leechers
		for (int i = 0; i<nleechers; i++){
			File dir = new File(new String("./tmp/c"+(i+2)));
			dir.mkdir();
			Thread tr = new Thread(new LeecherCreator(new String("./tmp/c"+(i+2)),new String("C"+ (i+2))));
			tr.run();
		}


		// force quit of the application
		Thread.sleep(1300);
		System.exit(0);

	}
}
