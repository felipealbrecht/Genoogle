package bio.pih.indexer;

import java.util.List;

import org.biojava.bio.symbol.SymbolList;

public interface SubSequecesIndex {

	public void addSubSequence(SymbolList subSymbolList, SubSequenceInfo info);
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList);
}
