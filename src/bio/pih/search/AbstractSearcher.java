package bio.pih.search;

import java.util.concurrent.Callable;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.SearchResults;

/**
 * This interface defines the methods that are presents in a similar sequence
 * searcher.
 * 
 * The doSearch method works asynchronous, returning an unique identifier for
 * the solicited search. The verifySearch return the current status of the
 * search. Others methods are for
 * 
 * @author albrecht
 */
public abstract class AbstractSearcher implements Callable<SearchResults> {

	protected final SearchStatus status;
	protected final SearchParams sp;
	protected final SearchResults sr;

	/**
	 * @param id
	 * @param sp
	 *            Parameter of the search
	 * @param databank 
	 *            Sequence data bank where the search will be performed.
	 * @param sm
	 * @param parent
	 *            The parent of this search.
	 */
	public AbstractSearcher(long id, SearchParams sp, SequenceDataBank databank) {
		this.sp = sp;
		this.sr = new SearchResults(sp);
		status = new SearchStatus(id, sp, databank);
		status.setActualStep(SearchStep.NOT_INITIALIZED);
	}

	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus() {
		return status;
	}
		
	/**
	 * @return the results of the search
	 */
	public SearchResults getSearchResults() {
		return sr;
	}
}
