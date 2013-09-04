package org.commonpatriots;

public class CPException extends Exception {

	public CPException() {
	}

	public CPException(String message) {
		super(message);
	}

	public CPException(Throwable cause) {
		super(cause);
	}

	public CPException(String message, Throwable cause) {
		super(message, cause);
	}

	public CPException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}