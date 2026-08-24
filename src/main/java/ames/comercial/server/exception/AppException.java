package ames.comercial.server.exception;

@SuppressWarnings("serial")
public class AppException extends RuntimeException {
	
	private int httpCode;

	public AppException (Throwable error) {
		super(error);
		this.httpCode = 500;
	}

	public AppException (String text, Throwable error) {
		super(text, error);
		this.httpCode = 500;
	}
	
	public AppException (String text, int httpCode) {
		super(text);
		this.httpCode = httpCode;
	}
	
	public AppException () {
		this("", 500);
	}

	public AppException (int httpCode) {
		this("", httpCode);
	}
	
	public AppException (String text) {
		this(text, 500);
	}
	
	public int httpCode () {
		return httpCode;
	}
}
