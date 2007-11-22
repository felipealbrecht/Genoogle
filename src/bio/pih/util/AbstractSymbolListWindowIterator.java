package bio.pih.util;

import org.biojava.bio.symbol.SymbolList;

public abstract class AbstractSymbolListWindowIterator implements SymbolListWindowIterator {

	protected int windowSize;
	protected int actualPos;
	protected SymbolList sequence;

	public AbstractSymbolListWindowIterator(SymbolList sequence, int windowSize) throws IndexOutOfBoundsException {
		if (windowSize < 1) {
			throw new IndexOutOfBoundsException("The windowSize must has the size at least one");
		}
		this.sequence = sequence;
		this.windowSize = windowSize;
		this.actualPos = 0;
	}

	public SymbolList getSymbolList() {
		return sequence;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int size) {
		this.windowSize = size;

	}

	public int getActualPos() {
		return actualPos;
	}

	public boolean hasNext() {
		if (actualPos + windowSize <= sequence.length()) {
			return true;
		}
		return false;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}