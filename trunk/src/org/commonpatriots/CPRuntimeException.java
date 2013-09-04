package org.commonpatriots;

public class CPRuntimeException extends RuntimeException {

	public CPRuntimeException() {
	}

	public CPRuntimeException(String message) {
		super(message);
	}

	public CPRuntimeException(Throwable cause) {
		super(cause);
	}

	public CPRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CPRuntimeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
