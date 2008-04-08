package bio.pih.search;

import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;

public class SearcherFactory {

	
	static public Searcher getSearcher(SequenceDataBank databank) {
		if (databank instanceof IndexedDNASequenceDataBank) {
			return new DNASearcher();
		}
		
		if (databank instanceof DatabankCollection) {
			return new CollectionSearcher(); 
		}
		
		throw new UnsupportedOperationException("Factory for " + databank.getClass().getName() + " not implemented yet. :-( ");		
	}
}
