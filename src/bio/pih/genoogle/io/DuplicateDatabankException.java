/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

/**
 * This exception is throw when it has two data banks with the same name.
 *
 * @author albrecht
 */
public class DuplicateDatabankException extends Exception {

	private static final long serialVersionUID = 1237811559244598848L;
	private final String databankName;
	private final String databankCollection;

	/**
	 * @param databankName
	 * @param databankCollection
	 */
	public DuplicateDatabankException(String databankName, String databankCollection) {
		this.databankName = databankName;
		this.databankCollection = databankCollection;
	}
	
	/**
	 * @return data bank collection where have two data banks with the same name. 
	 */
	public String getDatabankCollection() {
		return databankCollection;
	}
	
	
	/**
	 * @return name of the duplicate data bank.
	 */
	public String getDatabankName() {
		return databankName;
	}
	

}
