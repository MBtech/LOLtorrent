package filesharing.exception;

/**
 * Thrown when asking for a block index to download but there are no blocks that
 * can be downloaded (peer has only a subset of the blocks we have)
 */
public class NoNewBlocksForDownloadException extends FileSharingRuntimeException {

	public NoNewBlocksForDownloadException(String reason) {
		super(reason);
	}
	
}
