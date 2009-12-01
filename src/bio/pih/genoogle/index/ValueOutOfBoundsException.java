/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

/**
 * Exception when the parameter value is lower or higher than a pre set limit.
 * <p>@author Albrecht
 */
public class ValueOutOfBoundsException extends RuntimeException {

	private static final long serialVersionUID = -526404286506434331L;
	
	/**
	 * @param message
	 */
	public ValueOutOfBoundsException(String message) {
		super(message);
	}
	

}
