/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

/**
 * This exception is throw when the configuration has some error.
 *
 * @author albrecht
 */
public class InvalidConfigurationException extends Exception {

	private static final long serialVersionUID = -7849602904181806080L;

	public InvalidConfigurationException(String message) {
		super(message);
	}
	
}
