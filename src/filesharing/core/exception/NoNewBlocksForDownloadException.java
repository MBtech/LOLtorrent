package filesharing.core.exception;

/**
 * Represents an exception in peer behaviour
 */
public class NoNewBlocksForDownloadException extends FileSharingRuntimeException {

	public NoNewBlocksForDownloadException(String reason) {
		super(reason);
	}
	
}
