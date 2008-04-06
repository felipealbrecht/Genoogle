package bio.pih.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;

import com.google.common.collect.Maps;

public class SearchManager {

	Map<String, SequenceDataBank> databanks;
	Map<Long, SearchStatus> searchs;

	public SearchManager() {
		databanks = Maps.newHashMap();
		searchs = Maps.newHashMap();
	}

	public void addDatabank(SequenceDataBank databank) {
		databanks.put(databank.getName(), databank);
	}

	public long doSearch(SearchParams sp) {
		SequenceDataBank databank = databanks.get(sp.getDatabank());
		if (databank == null) {
			return -1;
		}
		SearchStatus search = SearcherFactory.getSearcher(databank).doSearch(sp, databank);
		searchs.put(search.getCode(), search);
		
		return search.getCode();
	}

	public boolean checkSearch(long code) {
		SearchStatus searchStatus = searchs.get(code);
		if (searchStatus == null) {
			return false;
		}
		if (searchStatus.getActualStep() == SearchStep.FINISHED) {
			return true;
		}
		return false;
	}

	public List<HSP> getResult(long code) {
		SearchStatus searchStatus = searchs.get(code);
		if (searchStatus == null) {
			return null;
		}
		return searchStatus.getResults();

	}

	public Collection<SequenceDataBank> getDatabankNames() {
		return databanks.values();
	}

}
