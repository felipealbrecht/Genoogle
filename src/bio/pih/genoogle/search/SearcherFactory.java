package bio.pih.genoogle.search;

import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.DatabankCollection;

/**
 * Construct a Searcher appropriate to the kind of {@link AbstractSequenceDataBank}.
 * 
 * @author albrecht
 */
public class SearcherFactory {

	/**
	 * @param id
	 * @param sp
	 * @param databank
	 * @param sm
	 * @param parent
	 * @return {@link Searcher} related with the data bank given.
	 */
	@SuppressWarnings("unchecked")
	static public AbstractSearcher getSearcher(long id, SearchParams sp, AbstractSequenceDataBank databank) {

		if (databank instanceof DatabankCollection) {
			return new CollectionSearcher(id, sp, (DatabankCollection<AbstractSequenceDataBank>) databank);
		}

		throw new UnsupportedOperationException("Factory for " + databank.getClass().getName()
				+ " not implemented yet. :-( ");
	}
}