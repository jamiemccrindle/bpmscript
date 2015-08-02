package org.bpmscript.exec.js;

import org.mozilla.javascript.EcmaError;

/**
 * An EcmaError that can be serialized
 */
public class SerializeableEcmaError extends Exception {

	private static final long serialVersionUID = -8686171042432510276L;

	private String errorMessage;
	private String name;
	private int columnNumber;
	private String details;
	private int lineNumber;
	private String lineSource;
	private String sourceName;

	/**
	 * Copy constructor
	 * 
	 * @param error the EcmaError to copy
	 */
	public SerializeableEcmaError(EcmaError error) {
		super(error.getMessage());
		errorMessage = error.getErrorMessage();
		name = error.getName();
		columnNumber = error.columnNumber();
		details = error.details();
		lineNumber = error.lineNumber();
		lineSource = error.lineSource();
		sourceName = error.sourceName();
		this.setStackTrace(error.getStackTrace());
	}

	/**
	 * @return the columnNumber the error was on
	 */
	public int columnNumber() {
		return columnNumber;
	}

	/**
	 * @return any message associated with this error
	 */
	public String details() {
		return details;
	}

	/**
	 * @return the error message
	 */
	public String errorMessage() {
		return errorMessage;
	}

	/**
	 * @return the lineNumber the error happened on
	 */
	public int lineNumber() {
		return lineNumber;
	}

	/**
	 * @return the source code for the line
	 */
	public String lineSource() {
		return lineSource;
	}

	/**
	 * @return no idea...
	 */
	public String name() {
		return name;
	}

	/**
	 * @return the name of the source file
	 */
	public String sourceName() {
		return sourceName;
	}
	
	/**
	 * @return the columnNumber
	 */
	public int getColumnNumber() {
		return columnNumber;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return the line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * @return the line source
	 */
	public String getLineSource() {
		return lineSource;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the source name
	 */
	public String getSourceName() {
		return sourceName;
	}
	
}
