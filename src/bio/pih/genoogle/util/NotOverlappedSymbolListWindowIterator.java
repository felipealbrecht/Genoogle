package bio.pih.genoogle.util;

import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class NotOverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	/**
	 * @param sequence
	 * @param windowLength
	 */
	NotOverlappedSymbolListWindowIterator(SymbolList sequence, int windowLength) {
		super(sequence, windowLength);
	}

	public SymbolList next() {
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos+= windowSize;
		return subList;
	}
}
