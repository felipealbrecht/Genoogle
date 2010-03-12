/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import bio.pih.genoogle.io.AbstractDatabankCollection;
import bio.pih.genoogle.io.AbstractSequenceDataBank;

/**
 * Construct a Searcher appropriate to the kind of {@link AbstractSequenceDataBank}.
 * 
 * @author albrecht
 */
public class SearcherFactory {

	@SuppressWarnings("unchecked")
	/**
	 * Get the searcher for the appropriate data bank.
	 */
	static public AbstractSearcher getSearcher(long id, SearchParams sp, AbstractSequenceDataBank databank) {

		if (databank instanceof AbstractDatabankCollection) {
			return new CollectionSearcher(id, sp, (AbstractDatabankCollection<AbstractSequenceDataBank>) databank);
		}

		throw new UnsupportedOperationException("Factory for " + databank.getClass().getName()
				+ " do not exist. ");
	}
}
