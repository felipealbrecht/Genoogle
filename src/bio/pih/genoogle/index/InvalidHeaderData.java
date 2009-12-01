/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

/**
 * @author albrecht
 *
 * Throw when some data as an invalid header. 
 */
public class InvalidHeaderData extends Exception {
	
	/**
	 * @param string the message 
	 */
	public InvalidHeaderData(String string) {
		super(string);
	}

	private static final long serialVersionUID = -1149058987449988029L;	
}
