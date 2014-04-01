package filesharing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * Daemon at the remote server side, listening to incoming file download request and assigns them a separate FileServer thread(object)
 * @author Muhammad Bilal
 *
 */
public class SimpleServerDaemon {
	private static ServerSocket servsock = null;
	public static int SOCKET_PORT = 13264;  //Port for Server Sock
	public static String FILE_TO_SEND = "";  // you may change this
	public final static int FILE_NAME_SIZE = 128;
	private static int connections = 0;
	static Object lock = new Object();
	static Object recordlock = new Object();
	public static void iconnect(){
		synchronized(lock){
			connections ++;
		}
	}
	public static void dconnect(){
		synchronized(lock){
			connections --;
		}
	}	
	
	public static int getServerAddress(){
		//System.out.println(servsock.getLocalSocketAddress());
		//SocketAddress sockadd = servsock.getLocalSocketAddress();
		return SOCKET_PORT;		
	}

	public static void main (String [] args ) throws IOException {
		//Variable initialization
		Socket sock = null;
		FILE_TO_SEND = "data/Amazon-DynamoDB.gif";
		FileSplitter splitter = new FileSplitter(FILE_TO_SEND);
		byte nparts = (byte)splitter.split();
		Hashtable<String,Byte> filechunkrecord = new Hashtable<String,Byte>();
		Hashtable<String,String> filepath = new Hashtable<String,String>();
		filechunkrecord.put("Amazon-DynamoDB.gif", nparts);
		splitter = new FileSplitter("data/Amazon-DynamoDB.pptx");
		nparts = (byte)splitter.split();
		filechunkrecord.put("Amazon-DynamoDB.pptx", nparts);
		filepath.put("Amazon-DynamoDB.gif", "data/Amazon-DynamoDB.gif");
		filepath.put("Amazon-DynamoDB.pptx", "data/Amazon-DynamoDB.pptx");
		try {
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				//System.out.println(peerrecord.get("Amazon-DynamoDB.gif"));
				System.out.println("Waiting.... on Port " + SOCKET_PORT);
				try {
					sock = servsock.accept();
					System.out.println("Accepted connection : " + sock);
					List <FileServer> fileservers  = new ArrayList <FileServer>();
					List <Thread> threads  = new ArrayList <Thread>();
					fileservers.add(new FileServer(SOCKET_PORT+1,sock,filechunkrecord,filepath));
					//System.out.println(fileservers);
					System.out.println("Number of connections to the server are: " + connections);
					connections = threads.size(); // this method can cause some problems because it's not atomic
					threads.add(new Thread(new FileServer(SOCKET_PORT+1,sock,filechunkrecord,filepath))); 
					threads.get(connections).start();
					//iconnect();
					System.out.println("Number of connections to the server are: " + connections);
					SOCKET_PORT = SOCKET_PORT + 1;
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
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