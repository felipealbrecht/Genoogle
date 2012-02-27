/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.io.Serializable;

public interface SymbolList extends Serializable {

	Alphabet getAlphabet();
	
	int getLength();
	
	char symbolAt(int pos);

	String seqString();
	
	SymbolList subSymbolList(int start, int end);
	
	SymbolList reverse();
}
