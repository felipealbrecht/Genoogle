package bio.pih.util;

import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class NotOverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	/**
	 * @param sequence
	 * @param windowSize
	 */
	public NotOverlappedSymbolListWindowIterator(SymbolList sequence, int windowSize) {
		super(sequence, windowSize);
	}

	public SymbolList next() {
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos+= windowSize;
		return subList;
	}

}
