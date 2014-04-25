package filesharing.core.exception;

/**
 * Represents an exception in peer behaviour
 */
public class DownloadCompleteException extends FileSharingRuntimeException {

	public DownloadCompleteException(String reason) {
		super(reason);
	}
	
}
