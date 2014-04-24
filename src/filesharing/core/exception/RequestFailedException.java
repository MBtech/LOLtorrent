package filesharing.core.exception;

/**
 * Represents a failure of fulfilling a request
 */
public class RequestFailedException extends FileSharingRuntimeException {

	public RequestFailedException(String reason) {
		super(reason);
	}

}
