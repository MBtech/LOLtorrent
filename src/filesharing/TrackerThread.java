package filesharing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * Thread for the tracker to handle registration of a single peer.
 * @author Muhammad Bilal
 *
 */
//TODO Currently only the ip recording path of the tracker is implemented.
//TODO Implement the peer address returning path of the thread as well!
public class TrackerThread implements Runnable{
	private Socket sock = null;
	public final static int FILE_NAME_SIZE = 128;
	static FileOutputStream fos;
	static BufferedOutputStream bos;

	

	public TrackerThread(Socket sock){
		this.sock = sock;
	}


	public void run () {

		//Variable initialization
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		InputStream is = null;
		ServerSocket servsock = null;
		byte [] mybytearray = new byte [FILE_NAME_SIZE];
		String Filename;
		int bytesRead = 0, current = 0;
		while(true){
			try{
				is = sock.getInputStream();
				do {
					try{
						bytesRead =	is.read(mybytearray, current, (mybytearray.length-current));
					}
					catch(SocketTimeoutException e){
						break;
					}
					System.out.println(bytesRead);
					if(bytesRead >= 0) current += bytesRead;
				} while(bytesRead > -1);
				Filename = new String(mybytearray,0,current);
				System.out.println(Filename);
				current = 0;
				fos = new FileOutputStream("Recordfile-"+ Filename);
				bos = new BufferedOutputStream(fos);
				bos.write(Filename.getBytes());
				bos.flush();
				bos.write(sock.getRemoteSocketAddress().toString().getBytes());
				bos.flush();
				sock.close();
				bos.close();			
				TrackerDaemon.updatelist(Filename, sock.getRemoteSocketAddress());
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
