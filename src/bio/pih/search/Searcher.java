package bio.pih.search;


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
	 * @return an unique identification of this search process.
	 */
	SearchStatus doSearch();
	
	/**
	 * Alert this search that its soon was finished.
	 *  
	 * @param searchStatus
	 * @return <code>true</code> if the search was found and marked as finished.
	 */
	boolean setFinished(SearchStatus searchStatus);
	
	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus();
}
