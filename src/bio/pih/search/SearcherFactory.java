package bio.pih.search;

import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;

/**
 * Construct a Searcher appropriate to the kind of {@link SequenceDataBank}.
 * 
 * @author albrecht
 */
public class SearcherFactory {

	/**
	 * @param sp
	 * @param databank
	 * @param parent
	 * @return {@link Searcher} related with the data bank given.
	 */
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
