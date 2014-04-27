package filesharing.exception;

/**
 * Thrown when trying to seed/download but no metadata was found, or there was
 * a problem when trying to retrieve it
 */
public class NoMetadataException extends FileSharingRuntimeException {

	public NoMetadataException(String reason) {
		super(reason);
	}

}
