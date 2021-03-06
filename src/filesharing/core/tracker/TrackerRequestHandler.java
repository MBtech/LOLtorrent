package filesharing.core.tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import filesharing.core.connection.PeerConnection;
import filesharing.core.processor.TrackerRequestProcessor;
import filesharing.message.tracker.request.PeerListRequestMessage;
import filesharing.message.tracker.request.RegisterPeerRequestMessage;
import filesharing.message.tracker.request.TrackerRequestMessage;
import filesharing.message.tracker.response.PeerListResponseMessage;
import filesharing.message.tracker.response.SuccessResponseMessage;

/**
 * This is a tracker thread - processes tracker requests from a single client,
 * and sends back the response
 */
public class TrackerRequestHandler implements Runnable, TrackerRequestProcessor {
	
	/**
	 * The tracker attached to this handler
	 */
	TrackerDaemon tracker;
	
	/**
	 * Client socket to wait for requests
	 */
	Socket sock;
	ObjectInputStream is; // socket object input stream, for convenience
	ObjectOutputStream os; // socket object output stream, for convenience
	
	/**
	 * Create a new tracker request handler listening in the specified socket
	 * @param sock socket for listening
	 * @throws IOException 
	 */
	public TrackerRequestHandler(TrackerDaemon tracker, Socket sock) throws IOException {
		this.tracker = tracker;
		this.sock = sock;
		this.is = new ObjectInputStream(sock.getInputStream());
		this.os = new ObjectOutputStream(sock.getOutputStream());
	}
	
	/**
	 * Log message to console
	 * @param msg
	 */
	public void log(String msg) {
		tracker.log(msg);
	}

	/**
	 * Tracker request handler main thread - processes requests from a single client
	 */
	@Override
	public void run() {
		try {
			// read requests, process them and return the response
			while(true) {
					// read request
					TrackerRequestMessage msg = (TrackerRequestMessage) is.readObject();
					// process request
					log(sock.getRemoteSocketAddress() + ": " + msg);
					msg.accept(this);
			}
		}
		catch (IOException | ClassNotFoundException e) {
			log("Invalid request from client: " + e.toString());
			//e.printStackTrace();
			// just exit silently
		}
	}

	/**
	 * Process requests for handing out the list of peers for a given file
	 */
	@Override
	public void processPeerListRequestMessage(PeerListRequestMessage msg) throws IOException {
		
		// initialize
		String filename = msg.filename();

		// check if file is registered
		if(tracker.peerRecord().containsKey(filename)) {
			// if it is, return peer list
			Collection<PeerConnection> peerList = tracker.peerRecord().get(filename);
			os.writeObject(new PeerListResponseMessage(peerList));
		}
		else {
			// if it is not, return an empty list
			os.writeObject(new PeerListResponseMessage());
		}
		
	}

	/**
	 * Process requests for registering a peer with a given file
	 */
	@Override
	public void processRegisterPeerRequestMessage(RegisterPeerRequestMessage msg) throws IOException {
		
		// initialize
		String filename = msg.filename();
		int dataPort = msg.dataPort();
		
		// check if this file has not been registered yet
		if(!tracker.peerRecord().containsKey(filename)) {
			// if not, create a new set to store peers for that file
			tracker.peerRecord().put(filename, Collections.synchronizedSet(new TreeSet<PeerConnection>()));
		}
		// add the peer to the list of peers for the given filename
		tracker.addPeer(filename,  new PeerConnection(sock.getInetAddress().getHostAddress(), dataPort));
		
		// save tracker state
		tracker.saveState();
		
		// send response to client
		os.writeObject(new SuccessResponseMessage());
	}

}
