package bio.pih.util;

import java.util.Iterator;

import org.biojava.bio.symbol.SymbolList;

public interface SymbolListWindowIterator extends Iterator<SymbolList>{
	
	void setWindowSize(int size);	
	int getWindowSize();
	int getActualPos();
		
	SymbolList getSymbolList();
}
