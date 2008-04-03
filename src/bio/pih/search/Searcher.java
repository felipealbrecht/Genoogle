package bio.pih.search;

import javax.naming.directory.SearchResult;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.seq.LightweightSymbolList;

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
	 * @param input
	 * @param bank
	 * @param params
	 * @return an unique identification of this search process.
	 */
	SearchStatus doSearch(LightweightSymbolList input, SequenceDataBank bank);
	
	/**
	 * Verify the status of the given search.
	 * @param searchCode of the search
	 * @return actual status of this search 
	 */
	SearchStep verifySearch(long searchCode);
	
	/**
	 * Obtain the result of the given search. 
	 * @param searchCode of the search
	 * @return the result of the given search, 
	 * if the searchCode is invalid or the search did not finished,
	 *  will return <code>null</code>. 
	 */
	SearchResult getSearchResult(long searchCode);	
	
	/**
	 * Cancel the search and remove all data of this search. 
	 * If the searchCode do not exist or the search is canceled, 
	 * nothing is done.
	 * @param searchCode of the search
	 */
	void cancelSearch(long searchCode);
	
	/**
	 * Remove all data from a completed search.
	 * If the searchCode do not exist or the search is canceled, 
	 * nothing is done. 
	 * @param searchCode of the search
	 */
	void removeSearch(long searchCode);
}
