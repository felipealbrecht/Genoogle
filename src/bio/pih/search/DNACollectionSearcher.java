package bio.pih.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.DatabankCollection;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;

import com.google.common.collect.Lists;

public class DNACollectionSearcher extends AbstractSearcher {

	protected List<SearchStatus> innerDataBanksStatus = null;

	@Override
	public SearchStatus doSearch(SearchParams sp, SequenceDataBank bank) {
		SearchStatus status = super.doSearch(sp, bank);
		
		SimilarSearcherDelegate ss = new SimilarSearcherDelegate(sp, (DatabankCollection<SequenceDataBank>) bank);
		ss.start();
		
		return status;
	}

	private class SimilarSearcherDelegate extends Thread {

		private final SearchParams sp;
		private final DatabankCollection<SequenceDataBank> databankCollection;

		public SimilarSearcherDelegate(SearchParams sp, DatabankCollection<SequenceDataBank> databankCollection) {
			this.sp = sp;
			this.databankCollection = databankCollection;
		}

		@Override
		public void run() {
			status.setActualStep(SearchStep.SEARCHING_INNER);
			innerDataBanksStatus = Lists.newLinkedList();
			
			Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
			while (it.hasNext()) {
				SequenceDataBank innerBank = it.next();
				Searcher searcher = SearcherFactory.getSearcher(innerBank);
				innerDataBanksStatus.add(searcher.doSearch(sp, innerBank));
			}

			List<HSP> allResults = Lists.newLinkedList();
			while (innerDataBanksStatus.size() > 0) {
				ListIterator<SearchStatus> listIterator = innerDataBanksStatus.listIterator();
				while (listIterator.hasNext()) {
					SearchStatus searchStatus = listIterator.next();
					if (searchStatus.isDone()) {
						allResults.addAll(searchStatus.getResults());
						listIterator.remove();
					}
				}
				Thread.yield();
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(allResults, HSP.getScoreComparetor());

			status.setResults(allResults);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

}
