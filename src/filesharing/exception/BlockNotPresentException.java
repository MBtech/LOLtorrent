package filesharing.exception;

/**
 * Thrown when trying to read a block that is not present
 */
public class BlockNotPresentException extends FileSharingRuntimeException {

	public BlockNotPresentException(String reason) {
		super(reason);
	}
	
}
