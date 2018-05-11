package com.solarflare.vcp.exception;

public class SfInvalidLoginException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SfInvalidLoginException(String errMsg){
		super(errMsg);
	}
}
