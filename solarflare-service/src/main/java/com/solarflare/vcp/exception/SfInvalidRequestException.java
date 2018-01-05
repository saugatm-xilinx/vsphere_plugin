package com.solarflare.vcp.exception;

public class SfInvalidRequestException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public SfInvalidRequestException(String errMsg){
		super(errMsg);
	}

}
