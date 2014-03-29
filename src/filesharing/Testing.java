package filesharing;

/**
 * This is the testing class. Different Configurations are written in order to test the functionality of the system
 * Start up the SimpleServerDaemon and TrackerDaemon before running this test module
 * @author Muhammad Bilal
 *
 */
//TODO Write the code to start the server and the tracker from this testing class as well
public class Testing {
	
	public static void main (String [] args){
		MultiFileClient fileclient = new MultiFileClient(13264,"127.0.0.1","tmp/Amazon-DynamoDB-download.gif", "Amazon-DynamoDB.gif", true, 20000, 30000);   
		Thread thread = new Thread(fileclient);  
		thread.start(); 
		MultiFileClient fileclient1 = new MultiFileClient(20000,"127.0.0.1","tmp/Amazon-DynamoDB-download1.gif", "Amazon-DynamoDB.gif",false, 20001, 30000);   
		Thread thread1 = new Thread(fileclient1);  
		thread1.start(); 
		MultiFileClient fileclient2 = new MultiFileClient(13264,"127.0.0.1","tmp/Amazon-DynamoDB-download.pptx", "Amazon-DynamoDB.pptx",false, 20001, 30000);   
		Thread thread2 = new Thread(fileclient2);  
		thread2.start();
	}
}
