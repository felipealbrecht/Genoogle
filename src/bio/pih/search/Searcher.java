package bio.pih.search;

/**
 * @author albrecht
 * 
 *         This interface defines the methods that are presents in a similar
 *         sequence searcher.
 * 
 *         The doSearch method works asynchronous, returning an unique
 *         identifier for the solicited search. The verifySearch return the
 *         current status of the search. Others methods are for
 */
public interface Searcher {

	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus();

	/**
	 * Start an asynchronous search for similar sequences against the sequence data bank.
	 */
	void doSearch();

	/**
	 * Alert this search that its soon was finished.
	 * 
	 * @param searchStatus
	 * @return <code>true</code> if the search was found and marked as finished.
	 */
	boolean setFinished(SearchStatus searchStatus);
}
