package bio.pih.search;

import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;

public class SearcherFactory {

	
	static public Searcher getSearcher(SearchParams sp, SequenceDataBank databank, Searcher parent) {
		if (databank instanceof IndexedDNASequenceDataBank) {
			return new DNASearcher(sp, databank, parent);
		}
		
		if (databank instanceof DatabankCollection) {
			return new CollectionSearcher(sp, databank, parent); 
		}
		
		throw new UnsupportedOperationException("Factory for " + databank.getClass().getName() + " not implemented yet. :-( ");		
	}
}
