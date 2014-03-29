package filesharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

public class TrackerDaemon {

	public final static int SOCKET_PORT = 20000;  // you may change this
	public final static int FILE_NAME_SIZE = 128;
	private static int connections = 0;

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