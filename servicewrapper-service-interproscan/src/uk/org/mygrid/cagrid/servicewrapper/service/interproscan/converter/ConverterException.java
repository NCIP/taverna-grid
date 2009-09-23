/**
 * 
 */
package uk.org.mygrid.cagrid.servicewrapper.service.interproscan.converter;

public class ConverterException extends RuntimeException {

	private static final long serialVersionUID = -2127788889952724963L;

	public ConverterException() {
		super();
	}

	public ConverterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConverterException(String message) {
		super(message);
	}

	public ConverterException(Throwable cause) {
		super(cause);
	}
}