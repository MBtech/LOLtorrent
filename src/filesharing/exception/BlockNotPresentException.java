package filesharing.exception;

/**
 * Represents an exception in peer behaviour
 */
public class BlockNotPresentException extends FileSharingRuntimeException {

	public BlockNotPresentException(String reason) {
		super(reason);
	}
	
}
