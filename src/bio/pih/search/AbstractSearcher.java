package bio.pih.search;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import bio.pih.io.SequenceDataBank;
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

	protected final long id;
	protected final SearchParams sp;
	protected final SearchResults sr;
	protected final ExecutorService executor;
	
	/**
	 * @param id
	 * @param sp
	 *            Parameter of the search
	 * @param databank 
	 *            Sequence data bank where the search will be performed.
	 */
	public AbstractSearcher(long id, SearchParams sp, SequenceDataBank databank, 
			ExecutorService executor) {
		this.id = id;
		this.sp = sp;
		this.executor = executor;
		this.sr = new SearchResults(sp);
	}
		
	/**
	 * @return the results of the search
	 */
	public SearchResults getSearchResults() {
		return sr;
	}
}
