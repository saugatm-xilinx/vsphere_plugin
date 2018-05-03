package com.solarflare.vcp.exception;

public class SfInvalidRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SfInvalidRequestException(String errMsg) {
		super(errMsg);
	}

	public SfInvalidRequestException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SfInvalidRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public SfInvalidRequestException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public SfInvalidRequestException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
