package bio.pih.genoogle.util;

import org.biojava.bio.symbol.SymbolList;

/**
 * Abstract class for the symbol list iterator.
 * 
 * @author albrecht
 */
public abstract class AbstractSymbolListWindowIterator implements SymbolListWindowIterator {

	protected int windowSize;
	protected int actualPos;
	protected SymbolList sequence;

	
	/**
	 * @param sequence
	 * @param windowLength
	 * @throws IndexOutOfBoundsException
	 */
	public AbstractSymbolListWindowIterator(SymbolList sequence, int windowLength) throws IndexOutOfBoundsException {
		if (windowLength < 1) {
			throw new IndexOutOfBoundsException("The windowSize must has the size at least one");
		}
		this.sequence = sequence;
		this.windowSize = windowLength;
		this.actualPos = 0;
	}

	@Override
	public SymbolList getSymbolList() {
		return sequence;
	}

	@Override
	public int getWindowSize() {
		return windowSize;
	}

	@Override
	public void setWindowSize(int size) {
		this.windowSize = size;

	}

	@Override
	public int getActualPos() {
		return actualPos;
	}

	@Override
	public boolean hasNext() {
		if (actualPos + windowSize <= sequence.length()) {
			return true;
		}
		return false;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}