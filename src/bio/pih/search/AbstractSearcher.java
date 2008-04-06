package bio.pih.search;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;

/**
 * Abstract class to implement a Searcher.
 * This class provides methods to do a search and verify its status.
 * 
 * @author albrecht
 */
public abstract class AbstractSearcher implements Searcher {

	protected SearchStatus status = null;
	protected volatile long searchId;
	
	public SearchStatus doSearch(SearchParams sp, SequenceDataBank bank) {
		
		searchId = getNextSearchId();
		status= new SearchStatus(sp, bank, searchId);
		status.setActualStep(SearchStep.NOT_INITIALIZED);

		return status;
	}
	
	public SearchStatus getStatus() {
		return status;
	}
	
	protected synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}

}
