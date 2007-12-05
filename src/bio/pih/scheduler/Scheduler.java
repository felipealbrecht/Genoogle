package bio.pih.scheduler;

import java.util.List;

import bio.pih.search.SearchParams;
import bio.pih.search.SearchResult;

/**
 * The Scheduler!
 * @author albrecht
  */
public interface Scheduler {
	/**
	 * @return the <code>list</code> of the workers
	 */
	List<Worker> getWorkers();
	
	/**
	 * Execute a search
	 * @param params
	 * @return
	 */
	SearchResult runSearch(SearchParams params);
}
