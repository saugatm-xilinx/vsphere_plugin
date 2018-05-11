package com.solarflare.vcp.vim.connection;

import java.net.MalformedURLException;

/**
 *
 */
public class ConnectionMalformedUrlException extends ConnectionException {
	private static final long serialVersionUID = 1L;

	public ConnectionMalformedUrlException(String url, MalformedURLException e) {
		super(url, e);
	}
}
