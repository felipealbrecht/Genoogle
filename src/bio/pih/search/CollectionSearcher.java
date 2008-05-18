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

/**
 * A searcher that does search operation at each data bank of its collection. 
 * @author albrecht
 *
 */
public class CollectionSearcher extends AbstractSearcher {

	private List<SearchStatus> innerDataBanksStatus = null;
	private SearchResults sr;
	private volatile boolean acceptingResults;

	/**
	 * @param sp
	 * @param bank
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	public CollectionSearcher(SearchParams sp, SequenceDataBank bank, Searcher parent) {
		super(sp, bank, parent);

		ss = new SimilarSearcherDelegate(sp, (DatabankCollection<SequenceDataBank>) bank);
		ss.setName("CollectionSearcher on " + bank.getName());
		sr = new SearchResults(sp);
	}

	@Override
	public synchronized boolean setFinished(SearchStatus searchStatus) {
		assert searchStatus.getActualStep() == SearchStep.FINISHED || searchStatus.getActualStep() == SearchStep.FATAL_ERROR;

		while (!acceptingResults) {
			Thread.yield();
		}
		
		if (searchStatus.getResults().getHits() != null) {
			sr.addAllHits(searchStatus.getResults().getHits());
		}
		
		boolean b = innerDataBanksStatus.remove(searchStatus);
		if (innerDataBanksStatus.size() == 0) {
			synchronized (ss) {
				ss.notify();
			}
		}
		return b;
	}

	private class SimilarSearcherDelegate extends Thread {

		private final SearchParams sp;
		private final DatabankCollection<SequenceDataBank> databankCollection;

		/**
		 * @param sp
		 * @param databankCollection
		 */
		public SimilarSearcherDelegate(SearchParams sp, DatabankCollection<SequenceDataBank> databankCollection) {
			this.sp = sp;
			this.databankCollection = databankCollection;
		}

		@Override
		public void run() {
			status.setActualStep(SearchStep.SEARCHING_INNER);
			innerDataBanksStatus = Lists.newLinkedList();
			innerDataBanksStatus = Collections.synchronizedList(innerDataBanksStatus);
			acceptingResults = false;

			Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
			while (it.hasNext()) {
				SequenceDataBank innerBank = it.next();
				Searcher searcher = SearcherFactory.getSearcher(sp, innerBank, CollectionSearcher.this);
				innerDataBanksStatus.add(searcher.doSearch());
			}		
			acceptingResults = true;

			try {
				synchronized (this) {
					while (innerDataBanksStatus.size() > 0) {
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
