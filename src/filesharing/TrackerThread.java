package filesharing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 


//TODO Currently only the ip recording path of the tracker is implemented.
//TODO Implement the peer address returning path of the thread as well!
public class TrackerThread implements Runnable{
	private Socket sock = null;
	public final static int FILE_NAME_SIZE = 128;
	static FileOutputStream fos;
	static BufferedOutputStream bos;
	static Hashtable<String,List<SocketAddress>> peerrecord = new Hashtable<String,List<SocketAddress>>();
	static List<SocketAddress> addrecord =	new ArrayList<SocketAddress>();
	static Object recordlock = new Object();

	public TrackerThread(Socket sock){
		this.sock = sock;
	}

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
	}


	public void run () {

		//Variable initialization
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		InputStream is = null;
		ServerSocket servsock = null;
		byte [] mybytearray = new byte [FILE_NAME_SIZE];
		String Filename;
		while(true){
			try{
				is = sock.getInputStream();
				is.read(mybytearray, 0, (mybytearray.length));
				Filename = mybytearray.toString();
				fos = new FileOutputStream("Recordfile-"+ Filename);
				bos = new BufferedOutputStream(fos);
				bos.write(Filename.getBytes());
				bos.flush();
				bos.write(sock.getRemoteSocketAddress().toString().getBytes());
				bos.flush();
				sock.close();
				bos.close();			
				TrackerThread.updatelist(Filename, sock.getRemoteSocketAddress());
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			finally {
				try{
					if (bis != null) bis.close();
					if (sock!=null) sock.close();
					if (fis!=null) fis.close();
					if(servsock!=null) servsock.close();
					break;
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
