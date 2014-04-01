package filesharing;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

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

	@Override
	public void run() {
		int bytesRead;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		Socket sock = null;
		byte nparts, currentpart=0;
		byte[] bytefilename = filename.getBytes();
		byte[] mybytearray = new byte [1024];
		while(true){
			try{
				System.out.println("Connecting to ask for a file... at port " + SOCKET_PORT);
				sock = new Socket(SERVER, SOCKET_PORT);
				OutputStream os = sock.getOutputStream();
				os.write(bytefilename, 0, bytefilename.length);
				os.flush();
				InputStream is = sock.getInputStream();
				DataInputStream ids = new DataInputStream(is);
				System.out.println("Receive number of chunks and next data socket");
				nparts = ids.readByte();
//				bytesRead = ids.read(mybytearray, 0, mybytearray.length);
//				@SuppressWarnings("deprecation")
//				String record = new String(mybytearray,bytesRead);
//				System.out.println(record);
				DATA_SOCKET_PORT=ids.readInt();
				//DATA_SOCKET_PORT = SOCKET_PORT + 1;
				//System.out.println(nparts);
				System.out.println(DATA_SOCKET_PORT);
				sock.close();
				is.close();
			}
			catch(IOException e){
				e.printStackTrace();
				continue;
			}
			break;
		}
		while(true){
			try {
				System.out.println("Connecting to get file now... at port " + DATA_SOCKET_PORT);
				sock = new Socket(SERVER, DATA_SOCKET_PORT);
				// receive file
				mybytearray  = new byte [FILE_SIZE];
				InputStream is = sock.getInputStream();
				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
				dos.writeInt(currentpart);
				dos.flush();
				while(currentpart<=nparts){
					fos = new FileOutputStream(FILE_TO_RECEIVED+"."+currentpart);
					bos = new BufferedOutputStream(fos);
					System.out.println("reading the input stream");
					sock.setSoTimeout(5000);
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
					System.out.println("Done reading the input stream");

					bos.write(mybytearray, 0 , current);
					bos.flush();
					System.out.println("File " + FILE_TO_RECEIVED
							+ " downloaded (" + current + " bytes read)");
					currentpart++;
					current = 0;
					if(currentpart > nparts){
						break;
					}
					dos.writeInt(currentpart);
					dos.flush();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			finally {
				try{
					if (fos != null)
						fos.close();
					if (bos != null)
						bos.close();

					if (sock != null)
						sock.close();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
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
