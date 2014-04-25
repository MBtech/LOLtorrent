package filesharing.exception;

/**
 * Superclass for all exceptions in our domain
 */
public abstract class FileSharingRuntimeException extends RuntimeException {

	public FileSharingRuntimeException(String reason) {
		super(reason);
	}

}
