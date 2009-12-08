/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.util;

import bio.pih.genoogle.seq.SymbolList;

/**
 * Factory for the {@link SymbolList} sliding windows
 * 
 * @author Albrecht
 */
public abstract class SymbolListWindowIteratorFactory {

	private static SymbolListWindowIteratorFactory OVERLAPPED_SYMBOL_LIST_WINDOW_ITERATOR_FACTORY = new SymbolListWindowIteratorFactory.OverlappedSymbolListWindowIteratorFactory();
	private static SymbolListWindowIteratorFactory NOT_OVERLAPPED_SYMBOL_LIST_WINDOW_ITERATOR_FACTORY = new SymbolListWindowIteratorFactory.NotOverlappedSymbolListWindowIteratorFactory();

	/**
	 * @param symbolList
	 * @param windowLength
	 * @return a new {@link SymbolListWindowIterator} related with this factory.
	 */
	public abstract SymbolListWindowIterator newSymbolListWindowIterator(SymbolList symbolList, int windowLength);

	/**
	 * @return a overlapped symbol list window iterator factory
	 */
	public static SymbolListWindowIteratorFactory getOverlappedFactory() {
		return OVERLAPPED_SYMBOL_LIST_WINDOW_ITERATOR_FACTORY;
	}

	/**
	 * @return a not overlapped symbol list window iterator factory
	 */
	public static SymbolListWindowIteratorFactory getNotOverlappedFactory() {
		return NOT_OVERLAPPED_SYMBOL_LIST_WINDOW_ITERATOR_FACTORY;
	}

	private static class OverlappedSymbolListWindowIteratorFactory extends SymbolListWindowIteratorFactory {

		@Override
		public SymbolListWindowIterator newSymbolListWindowIterator(SymbolList symbolList, int windowLength) {
			return new OverlappedSymbolListWindowIterator(symbolList, windowLength);
		}
	}

	private static class NotOverlappedSymbolListWindowIteratorFactory extends SymbolListWindowIteratorFactory {

		@Override
		public SymbolListWindowIterator newSymbolListWindowIterator(SymbolList symbolList, int windowLength) {
			return new NotOverlappedSymbolListWindowIterator(symbolList, windowLength);
		}
	}
}
