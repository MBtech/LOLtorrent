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

public class MultiFileClient implements Runnable {

	public int SOCKET_PORT = 0, DATA_SOCKET_PORT = 0;      // you may change this
	public String SERVER = "127.0.0.1";  // localhost
	public boolean seed = false;
	public String
	FILE_TO_RECEIVED = "";  // you may change this, I give a
	// different name because i don't want to
	// overwrite the one used by server...
	public String filename = "Amazon-DynamoDB.pptx";
	public final static int FILE_SIZE = 6022386; // file size temporary hard coded
	// should bigger than the file to be downloaded

	public static void main (String [] args ) throws IOException { 
		MultiFileClient fileclient = new MultiFileClient(13264,"127.0.0.1","C:/Users/Muhammad/Desktop/Amazon-DynamoDB-download.gif", "Amazon-DynamoDB.gif", true);   
		Thread thread = new Thread(fileclient);  
		thread.start();  
	}
	public MultiFileClient(int SocketPort, String server, String filepath, String file, boolean seed){
		this.SOCKET_PORT = SocketPort;
		this.SERVER = server;
		this.FILE_TO_RECEIVED = filepath;
		this.filename = file;
		this.seed = seed;
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
		while(true){
			try{
				sock = new Socket(SERVER, SOCKET_PORT);
				System.out.println("Connecting to ask for a file...");
				OutputStream os = sock.getOutputStream();
				os.write(bytefilename, 0, bytefilename.length);
				os.flush();
				InputStream is = sock.getInputStream();
				DataInputStream ids = new DataInputStream(is);
				System.out.println("Receive number of chunks and next data socket");
				nparts = ids.readByte();
				DATA_SOCKET_PORT=ids.readInt();
				System.out.println(nparts);
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
				sock = new Socket(SERVER, DATA_SOCKET_PORT);
				System.out.println("Connecting to get file now... at port " + DATA_SOCKET_PORT);

				// receive file
				byte [] mybytearray  = new byte [FILE_SIZE];
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
			FileSeeder seeder = new FileSeeder(filename, FILE_TO_RECEIVED, 20000);
			Thread seederthread = new Thread(seeder);
			seederthread.start();
			MultiFileClient fileclient2 = new MultiFileClient(20000,"127.0.0.1","C:/Users/Muhammad/Desktop/Amazon-DynamoDB-download1.gif", "Amazon-DynamoDB.gif",false);   
			Thread thread2 = new Thread(fileclient2);  
			thread2.start();  
		}
	}
}
