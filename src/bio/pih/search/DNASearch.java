package bio.pih.search;

import org.biojava.bio.seq.Sequence;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchInformation.SearchStep;

public class DNASearch implements Searcher{

	@Override
	public void cancelSearch(long searchCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long doSearch(Sequence input, SequenceDataBank bank, SearchParams params) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SearchResult getSearchResult(long searchCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeSearch(long searchCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SearchStep verifySearch(long searchCode) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
