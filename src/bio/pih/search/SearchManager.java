package bio.pih.search;

import java.util.Collection;
import java.util.Map;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Maps;

/**
 * Manage Searchers, check its status and stores and returns its results.
 * 
 * @author albrecht
 */
public class SearchManager {

	Map<String, SequenceDataBank> databanks;
	Map<Long, SearchStatus> searchs;

	/**
	 * 
	 */
	public SearchManager() {
		databanks = Maps.newHashMap();
		searchs = Maps.newHashMap();
	}

	/**
	 * @param databank
	 */
	public void addDatabank(SequenceDataBank databank) {
		databanks.put(databank.getName(), databank);
	}

	/**
	 * @param sp
	 * @return unique identifier of the solicited search.
	 * @throws UnknowDataBankException
	 */
	public long doSearch(SearchParams sp) throws UnknowDataBankException {
		SequenceDataBank databank = databanks.get(sp.getDatabank());
		if (databank == null) {
			throw new UnknowDataBankException(this, sp.getDatabank()); 
		}
		SearchStatus search = SearcherFactory.getSearcher(sp, databank, null).doSearch();
		long id = getNextSearchId();
		searchs.put(id, search);
		
		return id;
	}

	/**
	 * @param code
	 * 
	 * @return <code>true</code> if the search is completed.
	 */
	public boolean checkSearch(long code) {
		SearchStatus searchStatus = searchs.get(code);
		if (searchStatus == null) {
			return false;
		}
		if (searchStatus.getActualStep() == SearchStep.FATAL_ERROR) {
			return true;
		}
		if (searchStatus.getActualStep() == SearchStep.FINISHED) {
			return true;
		}
		return false;
	}

	/**
	 * @param code
	 * @return {@link SearchResults} of the related search.
	 */
	public SearchResults  getResult(long code) {
		SearchStatus searchStatus = searchs.get(code);
		if (searchStatus == null) {
			return null;
		}
		return searchStatus.getResults();

	}

	/**
	 * @return {@link Collection} of all {@link SequenceDataBank} that this {@link SearchResults} is managing. 
	 */
	public Collection<SequenceDataBank> getDatabanks() {
		return databanks.values();
	}
	
	private static long searchId = 0; 
	private  synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}

}
