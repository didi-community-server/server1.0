
package com.didi.community.socket;

public class DiDiNetException extends RuntimeException {
	public DiDiNetException () {
		super();
	}

	public DiDiNetException (String message, Throwable cause) {
		super(message, cause);
	}

	public DiDiNetException (String message) {
		super(message);
	}

	public DiDiNetException (Throwable cause) {
		super(cause);
	}
}
