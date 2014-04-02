package filesharing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
				TrackerDaemon.updatelist(Filename, sock.getRemoteSocketAddress());
				//fos = new FileOutputStream
				fos = new FileOutputStream("log/Recordfile-"+ Filename, false); //overwrite the previous file
				bos = new BufferedOutputStream(fos);
				bos.write(TrackerDaemon.getlist(Filename).getBytes());
				bos.flush();				
				sock.close();
				bos.close();
				//Code to take input from the log file and change it into SocketAddresses
//				fis = new FileInputStream("log/Recordfile-" + Filename);
//				bis = new BufferedInputStream(fis);
//				File f= new File("log/Recordfile-"+Filename);
//				mybytearray = new byte[(int) f.length()];
//				int size = bis.read(mybytearray,0,mybytearray.length);
//				String S = new String(mybytearray, 0, size);
//				System.out.println(S);
//				S = S.replaceAll("[^0-9\\.,:]" , "");
//				System.out.println(S);
//				String Sarray[] = S.split(",");
//				SocketAddress sockadd = new InetSocketAddress(Sarray[0].split(":")[0],Integer.parseInt(Sarray[0].split(":")[1]));
//				System.out.println(sockadd);				
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
