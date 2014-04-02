package filesharing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * This is actually a thread class that takes responsibility of a single connection for a client and serves the requested file to that client.
 * Have to select better name.
 * @author Muhammad Bilal
 *
 */
public class SeedInfoThread implements Runnable{
	private Socket sock = null;
	private int FILE_NAME_SIZE = 128;

	public SeedInfoThread(Socket sock){
		this.sock = sock;
	}

	
	public static void sendSeedRecord (String seedfile, byte[] mybytearray, OutputStream os, Socket sock){
		try
		{
			File myFile = new File (seedfile);
			mybytearray  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray,0,mybytearray.length);
			os = sock.getOutputStream();
			System.out.println("Sending " + seedfile + "(" + mybytearray.length + " bytes)");
			os.write(mybytearray,0,mybytearray.length);
			os.flush();
			bis.close();
			fis.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int size = 0;
		OutputStream os = null;
		while(true){
		try{
			os = sock.getOutputStream();
			is = sock.getInputStream();
			size = is.read(mybytearray, 0, FILE_NAME_SIZE);
			Filename = new String(mybytearray,0,size);
			System.out.println(Filename);
			sendSeedRecord("log/Recordfile-" + Filename, mybytearray, os, sock); 
			//System.out.println(TrackerDaemon.peerrecord.get(Strfilename).toString()); //Problem in printing this.
			this.wait(5000);
		}
		catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			continue;
		}
		finally {
			try{
				
				if (bis != null) bis.close();
				if (os != null) os.close();
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
