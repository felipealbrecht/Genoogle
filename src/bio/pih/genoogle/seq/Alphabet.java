/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.io.Serializable;

public interface Alphabet extends Serializable {
	
	public String getName();
	
	public int getSize();

	boolean isValid(char c);
	
	char[] getLetters();
}
