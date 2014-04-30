package filesharing.core.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GlobalConnectionMonitor implements Runnable {
	
	/**
	 * Specifies how often the monitoring task should run
	 */
	public static final int MONITOR_INTERVAL = 1;
	
	/**
	 * Singleton instance of a global manager
	 */
	private static GlobalConnectionMonitor manager = new GlobalConnectionMonitor();
	
	/**
	 * Pool of scheduled/periodic tasks
	 */
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	/**
	 * A list of connections being monitored
	 */
	private List<ConnectionHandler> connections = Collections.synchronizedList(new ArrayList<ConnectionHandler>());
	
	/**
	 * Constructor - Don't allow external instantiation of this class
	 */
	private GlobalConnectionMonitor() {
		// schedule the monitor to run periodically
		scheduler.scheduleWithFixedDelay(this, 0, MONITOR_INTERVAL, TimeUnit.SECONDS);
	}
	
	/**
	 * Returns the singleton instance
	 * @return global connection manager
	 */
	public static GlobalConnectionMonitor getManager() {
		return manager;
	}
	
	/**
	 * Starts monitoring a connection handler
	 * @param connection a connection handler
	 */
	public synchronized void watch(ConnectionHandler connection) {
		connections.add(connection);
	}
	
	/**
	 * Stops monitoring a connection handler
	 * @param connection
	 */
	public synchronized void unwatch(ConnectionHandler connection) {
		connections.remove(connection);
	}

	@Override
	public synchronized void run() {

		// print something whenever ran
		if(connections.size() > 0) {
			log("monitoring " + connections.size() + " connections");
		}
		
		// perform monitoring tasks for every connection being watched
		Iterator<ConnectionHandler> it = connections.iterator();
		while(it.hasNext()) {
			ConnectionHandler connection = it.next();
			// stop monitoring if not connected
			if(!connection.isConnected()) {
				it.remove();
			}
			// close connection if idle for too long
			if(connection.getIdleTime() > connection.idleTimeout()) {
				connection.disconnect();
				it.remove();
			}
		}
	}
	
	@Override
	public String toString() {
		return "[CONNECTION MONITOR] " + connections;
	}
	
	/**
	 * Logs a message to console
	 * @param msg message to be written
	 */
	public void log(String msg) {
		System.out.println("[CONNECTION MONITOR] " + msg);
	}
}
