package bio.pih.genoogle.util;

import org.biojava.bio.symbol.SymbolList;

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
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos++;
		return subList;
	}
}
