package com.diversityarrays.dal.server;

public class NoServiceException extends Exception {

	public NoServiceException(String message) {
		super(message);
	}

	public NoServiceException(Throwable cause) {
		super(cause);
	}

	public NoServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
