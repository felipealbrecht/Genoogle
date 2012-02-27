/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.util;

import bio.pih.genoogle.seq.SymbolList;

/**
 * Iterate over the overlapped sub-sequences from the symbol list
 *  
 * @author albrecht
 */
public class OverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	/**
	 * @param sequence
	 * @param windowSize
	 */
	OverlappedSymbolListWindowIterator(SymbolList sequence, int windowSize) throws IndexOutOfBoundsException{
		super(sequence, windowSize);
	}

	@Override
	public SymbolList next() {
		SymbolList subList = sequence.subSymbolList(actualPos+1, actualPos + windowSize);
		actualPos++;
		return subList;
	}
}
