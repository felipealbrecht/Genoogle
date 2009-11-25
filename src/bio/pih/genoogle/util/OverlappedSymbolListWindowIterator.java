package bio.pih.genoogle.util;

import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class OverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	/**
	 * @param sequence
	 * @param windowSize
	 * @throws IndexOutOfBoundsException
	 */
	OverlappedSymbolListWindowIterator(SymbolList sequence, int windowSize) throws IndexOutOfBoundsException{
		super(sequence, windowSize);
	}

	public SymbolList next() {
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos++;
		return subList;
	}
}
