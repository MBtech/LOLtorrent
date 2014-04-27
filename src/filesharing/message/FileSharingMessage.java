package filesharing.message;

import java.io.Serializable;

/**
 * Superclass of all the messages exchanged between processes in our domain
 */
public abstract class FileSharingMessage implements Serializable {
	@Override
	public String toString() {
		return "[MSG] ";
	}
}
