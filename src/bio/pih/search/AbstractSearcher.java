package bio.pih.search;

import java.util.Map;

import javax.naming.directory.SearchResult;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Maps;

/**
 * Abstract class to implement a Searcher.
 * This class provides methods to do a search and verify its status.
 * 
 * @author albrecht
 */
public class AbstractSearcher implements Searcher {

	protected SearchStatus status = null;
	protected Map<Long, SearchStatus> idToSearch = Maps.newHashMap();
	protected volatile long searchId;
	
	public SearchStatus doSearch(LightweightSymbolList input, SequenceDataBank bank) {
		
		searchId = getNextSearchId();
		status= new SearchStatus(input, bank, searchId);
		idToSearch.put(searchId, status);
		status.setActualStep(SearchStep.NOT_INITIALIZED);

		return status;
	}
	
	public SearchStatus getStatus() {
		return status;
	}

	public void cancelSearch(long searchCode) {
		throw new UnsupportedOperationException();
	}
	
	public SearchResult getSearchResult(long searchCode) {
		throw new UnsupportedOperationException();
	}

	public void removeSearch(long searchCode) {
		throw new UnsupportedOperationException();
	}

	public SearchStep verifySearch(long searchCode) {
		throw new UnsupportedOperationException();
	}

	protected synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}

}
