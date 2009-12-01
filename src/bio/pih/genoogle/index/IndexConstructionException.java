/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

public class IndexConstructionException extends Exception {

	private static final long serialVersionUID = 3385806705266172005L;
	

	public IndexConstructionException(String message) {
		super(message);
	}
	
	public IndexConstructionException(Throwable cause) {
		super(cause);
	}
	
	public IndexConstructionException(String message, Throwable cause) {
		super(message, cause);
	}	
}
