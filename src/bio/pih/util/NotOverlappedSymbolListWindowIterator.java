package bio.pih.util;

import org.biojava.bio.symbol.SymbolList;

public class NotOverlappedSymbolListWindowIterator extends AbstractSymbolListWindowIterator {

	public NotOverlappedSymbolListWindowIterator(SymbolList sequence, int windowSize) {
		super(sequence, windowSize);
	}

	public SymbolList next() {
		SymbolList subList = sequence.subList(actualPos+1, actualPos + windowSize);
		actualPos+= windowSize;
		return subList;
	}

}
