package bio.pih.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import bio.pih.io.DatabankCollection;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Lists;

public class CollectionSearcher extends AbstractSearcher {

	private List<SearchStatus> innerDataBanksStatus = null;
	private SearchResults sr;
	private volatile boolean isWaitingChildren;

	public CollectionSearcher(SearchParams sp, SequenceDataBank bank, Searcher parent) {
		super(sp, bank, parent);

		ss = new SimilarSearcherDelegate(sp, (DatabankCollection<SequenceDataBank>) bank);
		ss.setName("CollectionSearcher on " + bank.getName());
		sr = new SearchResults(sp);
	}

	@Override
	public synchronized boolean setFinished(SearchStatus searchStatus) {
		assert searchStatus.getActualStep() == SearchStep.FINISHED;

		sr.addAllHits(searchStatus.getResults().getHits());
		boolean b = innerDataBanksStatus.remove(searchStatus);
		if (innerDataBanksStatus.size() == 0) {
			isWaitingChildren = false;
			synchronized (ss) {
				ss.notify();
			}
		}
		return b;
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
				Searcher searcher = SearcherFactory.getSearcher(sp, innerBank, CollectionSearcher.this);
				innerDataBanksStatus.add(searcher.doSearch());
			}

			if (innerDataBanksStatus.size() > 0) {
				isWaitingChildren = true;
			}

			try {
				synchronized (this) {
					while (isWaitingChildren) {
						this.wait();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(sr.getHits(), Hit.COMPARATOR);

			status.setResults(sr);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

}
