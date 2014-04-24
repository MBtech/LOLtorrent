package filesharing.core.exception;

/**
 * Represents an exception in peer behaviour
 */
public class PeerErrorException extends FileSharingRuntimeException {

	public PeerErrorException(String reason) {
		super(reason);
	}
	
}
