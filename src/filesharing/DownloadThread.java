package filesharing;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * FileClient for the peer. Starts the download and if seeding is selected at the end of file download it starts the FileSeeder (object)
 * Have to change the name
 * @author Muhammad
 *
 */
public class DownloadThread implements Runnable {

	private int CHUNK_NUMBER = 0;
	private SocketAddress SEED_ADDRESS;
	private int INDEX;
	private String
	FILE_TO_RECEIVED = "";  // you may change this, I give a
	// different name because i don't want to
	// overwrite the one used by server...
	public String FILE_NAME = "";
	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
	// should bigger than the file to be downloaded
	public Object lock = new Object();

	public DownloadThread(SocketAddress seedaddress, int chunknumber, String filename, int index){
		SEED_ADDRESS = seedaddress;
		CHUNK_NUMBER= chunknumber;
		FILE_NAME = filename;
		INDEX = index;
	}
	

	@Override
	public void run() {
		int bytesRead = 0;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		Socket sock = null;
		byte[] mybytearray = null;
		
		//For actual download
		while(true){
			try {
				sock = new Socket();
				sock.connect(SEED_ADDRESS);
				// receive file
				mybytearray  = new byte [FILE_SIZE];
				InputStream is = sock.getInputStream();
				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
				while(CHUNK_NUMBER!=-1){
					dos.writeInt(CHUNK_NUMBER);
					dos.flush();
					fos = new FileOutputStream(FILE_TO_RECEIVED+"."+CHUNK_NUMBER);
					bos = new BufferedOutputStream(fos);
					System.out.println("reading the input stream for chunk number " + CHUNK_NUMBER);
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
					System.out.println(INDEX + " :Done reading the input stream");
					bos.write(mybytearray, 0 , current);
					bos.flush();
					System.out.println(INDEX + " :File " + FILE_TO_RECEIVED
							+ " downloaded (" + current + " bytes read)");
					MultiFileClient.remove_element(CHUNK_NUMBER);
					CHUNK_NUMBER = MultiFileClient.getelement(INDEX);
					System.out.println(INDEX+ ": Next Chunk is " + CHUNK_NUMBER);
					current = 0;
					
				}
				dos.writeInt(-1);
				dos.flush();
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
	}
}
