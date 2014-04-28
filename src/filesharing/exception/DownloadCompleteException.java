package filesharing.exception;

/**
 * Thrown when asking downloader for a block index for download but the download
 * is already complete
 */
public class DownloadCompleteException extends FileSharingRuntimeException {

	public DownloadCompleteException(String reason) {
		super(reason);
	}
	
}
