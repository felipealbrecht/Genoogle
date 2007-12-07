package bio.pih.indexer;

import java.util.List;

import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public interface SubSequecesIndex {

	/**
	 * @param subSymbolList
	 * @param info
	 */
	public void addSubSequence(SymbolList subSymbolList, SubSequenceInfo info);
	
	/**
	 * @param subSymbolList
	 * @return
	 */
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList);
}
