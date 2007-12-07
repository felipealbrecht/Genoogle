package bio.pih.util;

import java.util.Iterator;

import org.biojava.bio.symbol.SymbolList;

/**
 * Iterate among the sub-sequences
 * @author albrecht
 * 
 */
public interface SymbolListWindowIterator extends Iterator<SymbolList>{
	
	/**
	 * @param size
	 */
	void setWindowSize(int size);	
	
	/**
	 * @return
	 */
	int getWindowSize();
	
	/**
	 * @return
	 */
	int getActualPos();
		
	/**
	 * @return
	 */
	SymbolList getSymbolList();
}
