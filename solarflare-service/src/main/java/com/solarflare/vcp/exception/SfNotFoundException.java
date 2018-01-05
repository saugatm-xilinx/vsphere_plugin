package com.solarflare.vcp.exception;

public class SfNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public SfNotFoundException(String errMsg){
		super(errMsg);
	}
	
}
