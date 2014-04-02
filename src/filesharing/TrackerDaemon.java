package filesharing;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * At the moment the purpose of this Tracker Daemon is to listen to peers and
 * add information about them and the files that they have.
 * @author Muhammad Bilal, João Neto
 */
public class TrackerDaemon {

	public final static int SOCKET_PORT = 30000;  // you may change this
	public final static int FILE_NAME_SIZE = 128;
	private static int connections = 0;

	// contains a list of peers for each file
	static Hashtable<String,Set<SocketAddress>> peerrecord = new Hashtable<String,Set<SocketAddress>>();

	/**
	 * Refreshes the list of peers for a given file
	 * This method is thread-safe
	 * @param Strfilename name of the file
	 * @param sockadd socket address for the peer
	 */
	public static void updatelist(String Strfilename, SocketAddress sockadd) {
		// if this is a new file, we need a new list to store peers (It is containsKey not just contain())
		if(!peerrecord.containsKey(Strfilename)) {
			peerrecord.put(Strfilename, Collections.synchronizedSet(new HashSet<SocketAddress>()));
		}
		// add the peer to the list of peers for the filename
		peerrecord.get(Strfilename).add(sockadd);
		System.out.println(peerrecord.get(Strfilename).toString());
	}

	public static String getlist(String Strfilename) {
		// if this is a new file, we need a new list to store peers (It is containsKey not just contain())
		if(!peerrecord.containsKey(Strfilename)) {
			//peerrecord.put(Strfilename, Collections.synchronizedSet(new HashSet<SocketAddress>()));
			return "";
		}
		// add the peer to the list of peers for the filename
		return peerrecord.get(Strfilename).toString();
	}

	public static void main (String [] args) throws IOException {
		//Variable initialization
		Socket sock = null;
		int b = 0;
		ServerSocket servsock = null;
		List <TrackerThread> trackers  = new ArrayList <TrackerThread>();
		List <Thread> threads  = new ArrayList <Thread>();
		List <SeedInfoThread> seedinfo  = new ArrayList <SeedInfoThread>();
		List <Thread> infothreads  = new ArrayList <Thread>();
		try {
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				System.out.println("Tracker waiting.... on Port " + SOCKET_PORT);
				try {
					sock = servsock.accept();
					System.out.println("Accepted connection : " + sock);
					InputStream is = sock.getInputStream();
					b = is.read();
					if (b == 1){
						trackers.add(new TrackerThread(sock));
						connections = threads.size();
						threads.add(new Thread(trackers.get(connections)));
						threads.get(connections).start();
					}
					else{
						seedinfo.add(new SeedInfoThread(sock));
						connections = infothreads.size();
						infothreads.add(new Thread(seedinfo.get(connections)));
						infothreads.get(connections).start();
					}
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally {
			if (servsock != null) servsock.close();
		}
	}
}