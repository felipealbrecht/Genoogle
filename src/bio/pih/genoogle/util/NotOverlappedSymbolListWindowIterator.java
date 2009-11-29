package bio.pih.genoogle.util;

import org.biojava.bio.symbol.SymbolList;

/**
 * Iterate over the non overlapped sub-sequences from the symbol list
 *  
 * @author albrecht
 */
public class NotOverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	/**
	 * @param sequence
	 * @param windowLength
	 */
	NotOverlappedSymbolListWindowIterator(SymbolList sequence, int windowLength) {
		super(sequence, windowLength);
	}

	@Override
	public SymbolList next() {
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos+= windowSize;
		return subList;
	}
}
