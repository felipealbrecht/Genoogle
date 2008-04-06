package bio.pih.search;

import bio.pih.io.SequenceDataBank;

/**
 * @author albrecht
 * 
 * This interface defines the methods that are presents in a similar sequence searcher.
 * 
 * The doSearch method works asynchronous, returning an unique identifier for the solicited search.
 * The verifySearch return the current status of the search.  Others methods are for 
 */
public interface Searcher {
	
	/**
	 * Search similar sequences against the bank.
	 * @param sp 
	 * @param query 
	 * @param databank 
	 * @return an unique identification of this search process.
	 */
	SearchStatus doSearch(SearchParams sp, SequenceDataBank databank);
	
	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus();
}
