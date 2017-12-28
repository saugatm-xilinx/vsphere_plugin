package com.solarflare.vcp.vim.connection;

/**
 * ConnectionException is the base exception thrown by connection classes,
 * making this a runtime exception means that catching it is optional preventing
 * clutter, basing all connection related exceptions on this class means that
 * you may decide to catch ConnectionException to deal with any issues
 * underneath the connection infrastructure. Basing all connection classes'
 * exceptions on ConnectionException means that all new exceptions originating
 * in the connection related utilities are decoupled from any other subsystem.
 */
public class ConnectionException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ConnectionException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable t) {
		super(t);
	}
}
