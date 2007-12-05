package bio.pih.scheduler;

import java.util.List;

import bio.pih.search.SearchInformation;
import bio.pih.search.SearchParams;
import bio.pih.search.SearchResult;

/**
 * A interface that define a worker, or who will do the hard job
 * @author albrecht
 *
 */
public interface Worker {
			
	/**
	 * @return the integer that identifier the worker.
	 */
	int getIdentifier();
	
	/**
	 * Get the running charge
	 * @return
	 */
	float getLoad();
	
	/**
	 * Get the actual running searches
	 * @return a <code>list</code> contening the running searches
	 */
	List<SearchInformation> getRunning();
	
	/**
	 * Do a search
	 * @param params
	 * @return
	 */
	SearchResult doSearch(SearchParams params);
	
}
