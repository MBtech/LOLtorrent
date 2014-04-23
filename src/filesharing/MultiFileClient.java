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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * FileClient for the peer. Starts the download and if seeding is selected at the end of file download it starts the FileSeeder (object)
 * Have to change the name
 * @author Muhammad
 *
 */
public class MultiFileClient implements Runnable {

	private int SOCKET_PORT = 0, DATA_SOCKET_PORT = 0;      // you may change this
	private String SERVER = "127.0.0.1";  // localhost
	private boolean seed = false;
	private int SEEDING_PORT = 0, TRACKER_PORT = 0;
	private String
	FILE_TO_RECEIVED = "";  // you may change this, I give a
	// different name because i don't want to
	// overwrite the one used by server...
	public String filename = "Amazon-DynamoDB.pptx";
	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
	// should bigger than the file to be downloaded
	public static byte currentpart=0;
	private static List <Integer> ChunksAcc = new ArrayList<Integer>();
	public Object lock = new Object();
	public static Object lock1 = new Object();
	public static Object lock2 = new Object();
	/**
	 * Constructor for MultiFileClient
	 * @param SocketPort Socket port for remote server
	 * @param server IP Address of the remote server
	 * @param filepath Path to save the downloaded file
	 * @param file Name of the downloaded file
	 * @param seed Whether to seed after downloading or not
	 * @param seedingport If yes, the port on which seeding server should start
	 * @param trackerport Port of the tracker to register with
	 */
	public MultiFileClient(int SocketPort, String server, String filepath, String file, boolean seed, int seedingport, int trackerport){
		this.SOCKET_PORT = SocketPort;
		this.SERVER = server;
		this.FILE_TO_RECEIVED = filepath;
		this.filename = file;
		this.seed = seed;
		this.SEEDING_PORT = seedingport;
		this.TRACKER_PORT = trackerport;
	}

	//Getting the next block to fetch
	public byte getpartnumber(){
		synchronized(lock){
			currentpart=currentpart++;
			return currentpart--; 
		}
	}
	
	public static void remove_element(int n){
		synchronized(lock1){
			System.out.println("Element to remove " + n);
			ChunksAcc.remove((Object)n);
			System.out.println(ChunksAcc);
		}
	}
	
	public static int getelement(int n){
		synchronized(lock1){
			if (n<=ChunksAcc.size()-1)
				return ChunksAcc.get(n);
			else
				return -1;
		}
	}

	@Override
	public void run() {
		int bytesRead = 0;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		Socket sock = null;
		byte nparts;
		byte[] bytefilename = filename.getBytes();
		byte[] mybytearray = new byte [1024];
		List <SocketAddress> sockadd = new ArrayList<SocketAddress>();
		//Getting number of chunks and the port to get data from the file server
		while(true){
			try{
				System.out.println("Connecting to ask for a file... at port " + SOCKET_PORT);
				sock = new Socket(SERVER, SOCKET_PORT);
				OutputStream os = sock.getOutputStream();
				os.write(bytefilename, 0, bytefilename.length);
				os.flush();
				InputStream is = sock.getInputStream();
				DataInputStream ids = new DataInputStream(is);
				System.out.println("Receiving number of chunks and next data socket");
				nparts = ids.readByte();
				sock.close();
				is.close();
			}
			catch(IOException e){
				e.printStackTrace();
				continue;
			}
			break;
		}
		for (int i =0; i<=nparts; i++){
			ChunksAcc.add(i);
		}
		System.out.println(ChunksAcc);
		//Module to connect to tracker and get seed info
		try
		{
			sock = new Socket(SERVER,TRACKER_PORT);
			OutputStream os = sock.getOutputStream();
			InputStream is = sock.getInputStream();
			os.write(0);
			os.flush();
			os.write(filename.getBytes(),0,filename.length());
			os.flush();
			//os.close(); // Closing the stream also closes the socket
			fos = new FileOutputStream("log/DownloadedRecordfile-"+filename);
			bos = new BufferedOutputStream(fos);
			System.out.println("reading the input stream to get seed record");
			do {
				try{
					bytesRead =	is.read(mybytearray, current, (mybytearray.length-current));
				}
				catch(SocketTimeoutException e){
					e.printStackTrace();
					continue;
				}
				System.out.println(bytesRead);
				if(bytesRead >= 0) current += bytesRead;
			} while(bytesRead > -1);
			System.out.println("Done reading the input stream. " + current + " Bytes read");
			bos.write(mybytearray, 0 , current);
			System.out.println(new String(mybytearray, 0, current));
			bos.flush();
			sock.close();
			//TODO: There is no need to write this seeder data to the file
			//Code to take input from the log file and change it into SocketAddresses 
			FileInputStream fis = new FileInputStream("log/DownloadedRecordfile-" + filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			File f= new File("log/DownloadedRecordfile-"+filename);
			mybytearray = new byte[(int) f.length()];
			int size = bis.read(mybytearray,0,mybytearray.length);
			String S = new String(mybytearray, 0, size);
			System.out.println(S);
			S = S.replaceAll("[^0-9\\.,:]" , "");
			System.out.println(S);
			String Sarray[] = S.split(",");
			int length = Sarray.length;
			for(int i = 0; i<length; i++){
				sockadd.add(new InetSocketAddress(Sarray[i].split(":")[0],Integer.parseInt(Sarray[i].split(":")[1])));
			}
			System.out.println(sockadd);
			bis.close();

		}
		catch(IOException e){
			e.printStackTrace();
		}
		List<Thread> tlist= new ArrayList<Thread>();
		List<DownloadThread> dtlist = new  ArrayList<DownloadThread>();
		//Spawn several threads to download the file from multiple clients simultaneously (based on the number of peers returned)
		int n = 0;
		if (sockadd.size()>5)
			n = 5;
		if (nparts <sockadd.size())
			n = nparts;
		else
			n = sockadd.size();
		System.out.println("Spawning "+ n + " threads to download the file");
		for(int i = 0; i<n; i++){
			SocketAddress seedaddress = sockadd.get(i);
			DownloadThread dt = new DownloadThread(seedaddress,i, filename, i);
			Thread t = new Thread(dt);
			tlist.add(t);
			dtlist.add(dt);
			t.start();
		}
		for (int i=0; i<n ; i++){
			try {
				tlist.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Thread Interrupted Exception");
				continue;
			}
		}
		FileSplitter joiner = new FileSplitter(FILE_TO_RECEIVED);
		try {
			joiner.join(FILE_TO_RECEIVED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
		if (seed){
			FileSeeder seeder = new FileSeeder(filename, FILE_TO_RECEIVED, SEEDING_PORT, "127.0.0.1", TRACKER_PORT);
			Thread seederthread = new Thread(seeder);
			seederthread.start();
		}
	}
}
