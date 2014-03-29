package filesharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**At the moment the purpose of this Tracker Daemon is to listen to peers and add information about them and the 
 * files that they have.
 * @author Muhammad Bilal
*/
public class TrackerDaemon {

	public final static int SOCKET_PORT = 30000;  // you may change this
	public final static int FILE_NAME_SIZE = 128;
	private static int connections = 0;
	static Hashtable<String,List<SocketAddress>> peerrecord = new Hashtable<String,List<SocketAddress>>();
	static List<SocketAddress> addrecord =	new ArrayList<SocketAddress>();
	static Object recordlock = new Object();
	
	public static void updatelist(String Strfilename, SocketAddress sockadd){
		synchronized(recordlock){
			if (peerrecord.get(Strfilename)!=null){
				addrecord = peerrecord.get(Strfilename);
			}
			else{
				addrecord.add(sockadd);
				peerrecord.put(Strfilename, addrecord);
			}
		}
		System.out.println(peerrecord);
	}
	
	public static void main (String [] args ) throws IOException {
		//Variable initialization
		ServerSocket servsock = null;
		Socket sock = null;
		try {
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				System.out.println("Tracker waiting.... on Port " + SOCKET_PORT);
				try {
					sock = servsock.accept();
					System.out.println("Accepted connection : " + sock);
					List <TrackerThread> trackers  = new ArrayList <TrackerThread>();
					List <Thread> threads  = new ArrayList <Thread>();
					trackers.add(new TrackerThread(sock));
					threads.add(new Thread(trackers.get(connections)));
					threads.get(connections).start();
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