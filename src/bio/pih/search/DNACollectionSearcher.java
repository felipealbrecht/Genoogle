package bio.pih.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import bio.pih.io.DatabankCollection;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Lists;

public class DNACollectionSearcher extends AbstractSearcher {

	// get the searchers individually
	// do the searchers
	// filter
	// merge
	// return

	protected List<SearchStatus> innerDataBanksStatus = null;

	@Override
	public SearchStatus doSearch(LightweightSymbolList input, SequenceDataBank bank) {
		SearchStatus status = super.doSearch(input, bank);
		
		SimilarSearcherDelegate ss = new SimilarSearcherDelegate(input, (DatabankCollection<SequenceDataBank>) bank);
		ss.start();
		
		return status;
	}

	private class SimilarSearcherDelegate extends Thread {

		private final LightweightSymbolList querySequence;
		private final DatabankCollection<SequenceDataBank> collection;

		public SimilarSearcherDelegate(LightweightSymbolList querySequence, DatabankCollection<SequenceDataBank> collection) {
			this.querySequence = querySequence;
			this.collection = collection;
		}

		@Override
		public void run() {
			status.setActualStep(SearchStep.SEARCHING_INNER);
			innerDataBanksStatus = Lists.newLinkedList();
			
			Iterator<SequenceDataBank> it = collection.databanksIterator();
			while (it.hasNext()) {
				SequenceDataBank innerBank = it.next();
				Searcher searcher = SearcherFactory.getSearcher(innerBank);
				innerDataBanksStatus.add(searcher.doSearch(querySequence, innerBank));
			}

			List<AlignmentResult> allResults = Lists.newLinkedList();
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

			Collections.sort(allResults, AlignmentResult.getScoreComparetor());

			status.setResults(allResults);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

}
