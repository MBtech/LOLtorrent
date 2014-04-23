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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Hashtable;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * This is actually a thread class that takes responsibility of a single connection for a client and serves the requested file to that client.
 * Have to select better name.
 * @author Muhammad Bilal
 *
 */
public class FileServer implements Runnable{
	private Socket sock = null;
	public int DATA_SOCKET_PORT = 0;
	Hashtable<String,Byte> filechunkrecord = null;
	Hashtable<String,String> filepath = null;
	public static String FILE_TO_SEND = "";  // you may change this
	public final static int FILE_NAME_SIZE = 128;

	public FileServer(int Dataport, Socket sock,Hashtable<String,Byte> filechunkrecord,Hashtable<String,String> filepath){
		this.sock = sock;
		this.filechunkrecord = filechunkrecord;
		this.filepath = filepath;
		this.DATA_SOCKET_PORT = Dataport;
	}

	public static void sendNumOfChunks(byte nparts, OutputStream os){
		byte[] mybytearray = new byte[1];
		mybytearray[0] = nparts;
		//System.out.println(mybytearray[0]);
		try{
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sendChunk (int reqpart, byte[] mybytearray, String FILE_TO_SEND, OutputStream os, Socket sock){
		try
		{
			File myFile = new File (FILE_TO_SEND+"."+reqpart);
			mybytearray  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray,0,mybytearray.length);
			os = sock.getOutputStream();
			System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
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
	
	public static void sendSeedRecord (String seedfile, byte[] mybytearray, OutputStream os, Socket sock){
		try
		{
			File myFile = new File (seedfile);
			mybytearray  = new byte [(int)myFile.length()];
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray,0,mybytearray.length);
			os = sock.getOutputStream();
			System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
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
	public static int getChunkNumber(DataInputStream idos, Socket sock){
		try{
			idos = new DataInputStream(sock.getInputStream());
			return idos.readInt();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public void run () {
		//Variable initialization
		FileInputStream fis = null;
		byte nparts = 0;
		int reqpart = 0, size =0;
		BufferedInputStream bis = null;
		InputStream is = null;
		OutputStream os = null;
		ServerSocket servsock = null;
		byte [] filename = new byte[FILE_NAME_SIZE];
		byte [] mybytearray = null;
		while(true){
		try{
			os = sock.getOutputStream();
			DataOutputStream iods = new DataOutputStream(os);
			is = sock.getInputStream();
			size = is.read(filename, 0, FILE_NAME_SIZE);
			String Strfilename = new String(filename,0,size);
			System.out.println("Request for " + Strfilename + " Received on sock" + sock);
			//filechunkrecord.get
			nparts = filechunkrecord.get(Strfilename);
			System.out.println("Total number of chunks for " + Strfilename + " are " + nparts);
			sendNumOfChunks(nparts, iods); // pass os here instead of iods creates problems. 
			iods.flush();
			//System.out.println(TrackerDaemon.peerrecord.get(Strfilename).toString()); //Problem in printing this.
			System.out.println("Socket for the data connection is " + DATA_SOCKET_PORT);
			sock.close();
			os.close();
			servsock = new ServerSocket(DATA_SOCKET_PORT);
			//servsock.getLocalSocketAddress(...) doesn't work!! Why???
			//Remove the previous instance of server as it doesn't work after serving the previous request
			TrackerDaemon.remove_element(Strfilename, new InetSocketAddress("127.0.0.1", DATA_SOCKET_PORT-1)); // Stupid solution for works for some cases
			TrackerDaemon.updatelist(Strfilename, new InetSocketAddress("127.0.0.1", DATA_SOCKET_PORT));
			System.out.println("Write record to file " + Strfilename );
			FileOutputStream fos = new FileOutputStream("log/Recordfile-"+ Strfilename, false); //overwrite the previous file
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(TrackerDaemon.getlist(Strfilename).getBytes());
			bos.flush();
			sock = servsock.accept();
			DataInputStream idos = new DataInputStream(sock.getInputStream());
			System.out.println("Accepted connection : " + sock);
			os = sock.getOutputStream();
			reqpart = getChunkNumber(idos, sock);
			while(reqpart<=nparts && reqpart!=-1){
				// Read the chunk number that is requested
				System.out.println("send part "+reqpart);
				sendChunk (reqpart,mybytearray, filepath.get(Strfilename), os, sock);
				System.out.println("Done.");
				reqpart = idos.readInt();

			}
		}
		catch (IOException e) {
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
		break;
		}
		System.out.println("Exiting after serving file");
		//SimpleServerDaemon.dconnect();
	}
}
