package bio.pih.scheduler;

import java.util.List;

import bio.pih.search.SearchParams;
import bio.pih.search.SearchResult;

public interface Scheduler {
	List<Worker> getWorkers();
	
	SearchResult runSearch(SearchParams params);
}
