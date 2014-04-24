package filesharing.core.message;

import java.io.Serializable;

public abstract class FileSharingMessage implements Serializable {
	public String toString() {
		return "[MSG] ";
	}
}
