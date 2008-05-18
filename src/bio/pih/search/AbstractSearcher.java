package bio.pih.search;

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
	protected Searcher parent;
	protected Thread ss;
	
	/**
	 * @param sp Parameter of the search
	 * @param bank Sequence data bank where the search will be performed.
	 * @param parent The parent of this search.
	 */
	public AbstractSearcher(SearchParams sp, SequenceDataBank bank, Searcher parent) {
		status = new SearchStatus(sp, bank, parent);
		status.setActualStep(SearchStep.NOT_INITIALIZED);
		
	}
	
	public SearchStatus doSearch() {
		ss.start();		
		return status;		
	}
	
	public SearchStatus getStatus() {
		return status;
	}
	
	@Override
	public boolean setFinished(SearchStatus searchStatus) {
		throw new UnsupportedOperationException("This Searcher do not support sons.");
	}

}
