package filesharing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;


//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;
//if you add this instead of Java.util.Hastable the resultant class will not be generic 

/**
 * Activate by the MultiFileClient Class if seeding is selected. Each FileSeeder objects serves a particular file to the clients that connect to it.
 * @author Muhammad Bilal
 *
 */
public class FileSeeder implements Runnable{

	private int SOCKET_PORT = 0;  // Seeding port
	private String FILE_PATH = "";  // Relative path of the requested file
	private String FILE_NAME = ""; //Name of the file to be seed
	private String TRACKER_IP = ""; // IP and port number of the tracker to register with
	private int TRACKER_PORT = 0;
	public final static int FILE_NAME_SIZE = 128; //Max size of the file name
	public FileSeeder (String Strfilename, String filepath, int seedport, String trackerip, int trackerport){
		this.FILE_NAME = Strfilename;
		this.SOCKET_PORT = seedport;
		this.FILE_PATH = filepath;
		this.TRACKER_IP = trackerip;
		this.TRACKER_PORT = trackerport;
	}
	public static void sendNumOfChunks(byte nparts, OutputStream os){
		byte[] mybytearray = new byte[1];
		mybytearray[0] = nparts;
		System.out.println(mybytearray[0]);
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
		int reqpart = 0;
		BufferedInputStream bis = null;
		InputStream is = null;
		OutputStream os = null;
		ServerSocket servsock = null;
		Socket sock = null;
		byte [] filename = new byte[FILE_NAME_SIZE];
		byte [] mybytearray = null;
		// This record is supposed to be for locating other peers without tracker but currently it's not functional
		List<SocketAddress>myrecord = new ArrayList<SocketAddress>(); 
		
		FileSplitter splitter = new FileSplitter(FILE_PATH);
		byte nparts = 0;
		try {
			nparts = (byte)splitter.split();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sock = new Socket(TRACKER_IP, TRACKER_PORT);
			os = sock.getOutputStream();
			os.write(1);
			os.write(FILE_NAME.getBytes());
			os.flush();
			sock.close();
			servsock = new ServerSocket(SOCKET_PORT);
			while (true) {
				System.out.println("Waiting...");
				try {
					sock = servsock.accept();
					System.out.println("Accepted connection : " + sock);
					os = sock.getOutputStream();
					DataOutputStream iods = new DataOutputStream(os);
					is = sock.getInputStream();
					is.read(filename, 0, FILE_NAME_SIZE);
					System.out.println(nparts);
					sendNumOfChunks(nparts, os);
					System.out.println("Done.");
					sock.close();
					os.close();
					sock = servsock.accept();
					DataInputStream idos = new DataInputStream(sock.getInputStream());
					System.out.println("Accepted connection : " + sock);
					os = sock.getOutputStream();
					reqpart = getChunkNumber(idos, sock);
					while(reqpart<=nparts){
						// send file
						System.out.println("send part "+reqpart);
						sendChunk (reqpart,mybytearray, FILE_PATH, os, sock);
						System.out.println("Done.");
						if (reqpart == nparts){
							break;
						}
						reqpart = idos.readInt();
					}
					myrecord.add(sock.getRemoteSocketAddress());
					System.out.println(sock.getRemoteSocketAddress());
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				finally {
					if (bis != null) bis.close();
					if (os != null) os.close();
					if (sock!=null) sock.close();
					if (fis!=null) fis.close();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally {
			if (servsock != null)
				try {
					servsock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}