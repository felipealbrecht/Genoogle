package bio.pih.genoogle.util;

import java.util.Iterator;

import org.biojava.bio.symbol.SymbolList;

/**
 * Iterate among the sub-sequences
 * 
 * @author albrecht
 */
public interface SymbolListWindowIterator extends Iterator<SymbolList>{
	
	/**
	 * @param size
	 */
	void setWindowSize(int size);	
	
	/**
	 * @return window size.
	 */
	int getWindowSize();
	
	/**
	 * @return actual position in the symbol list.
	 */
	int getActualPos();
		
	/**
	 * @return symbol list.
	 */
	SymbolList getSymbolList();
}
