/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.util;

import bio.pih.genoogle.seq.SymbolList;

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
		if (actualPos + windowSize <= sequence.getLength()) {
			return true;
		}
		return false;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}