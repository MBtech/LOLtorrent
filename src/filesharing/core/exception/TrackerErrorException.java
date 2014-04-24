package filesharing.core.exception;

/**
 * Represents an exception in tracker behaviour
 */
public class TrackerErrorException extends FileSharingRuntimeException {

	public TrackerErrorException(String reason) {
		super(reason);
	}
	
}
