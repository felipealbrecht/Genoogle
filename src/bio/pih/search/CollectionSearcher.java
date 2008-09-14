package bio.pih.search;

import java.util.Collections;
import java.util.Iterator;

import org.apache.log4j.Logger;

import bio.pih.io.DatabankCollection;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

/**
 * A searcher that does search operation at each data bank of its collection.
 * 
 * @author albrecht
 * 
 */
public class CollectionSearcher extends AbstractSearcher {

	static Logger logger = Logger.getLogger(CollectionSearcher.class.getName());

	private volatile int waitingSearchs = 0;
	private SearchResults sr;
	private boolean acceptingResults;

	/**
	 * @param code
	 * @param sp
	 * @param bank
	 * @param sm
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	public CollectionSearcher(long code, SearchParams sp, SequenceDataBank bank, SearchManager sm,
			AbstractSearcher parent) {
		super(code, sp, bank, sm, parent);

		ss = new SimilarSearcherDelegate(sp, (DatabankCollection<SequenceDataBank>) bank);
		ss.setName("CollectionSearcher on " + bank.getName());
		sr = new SearchResults(sp);
	}

	@Override
	public synchronized boolean setFinished(SearchStatus searchStatus) {
		while (!acceptingResults) {
			Thread.yield();
		}

		SearchResults results = searchStatus.getResults();
		if (results.hasFail()) {
			sr.addFails(results.getFails());
		} else {
			sr.addAllHits(results.getHits());
		}

		synchronized (ss) {
			waitingSearchs--;
			if (waitingSearchs == 0) {
				ss.notify();
			}
		}
		return true;
	}

	private class SimilarSearcherDelegate extends Thread {

		private final SearchParams sp;
		private final DatabankCollection<SequenceDataBank> databankCollection;

		/**
		 * @param sp
		 * @param databankCollection
		 */
		public SimilarSearcherDelegate(SearchParams sp,
				DatabankCollection<SequenceDataBank> databankCollection) {
			this.sp = sp;
			this.databankCollection = databankCollection;
		}

		@Override
		public void run() {
			status.setActualStep(SearchStep.SEARCHING_INNER);
			
			acceptingResults = false;
			Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
			while (it.hasNext()) {
				SequenceDataBank innerBank = it.next();
				AbstractSearcher searcher = SearcherFactory.getSearcher(-1, sp, innerBank, null,
						CollectionSearcher.this);
				waitingSearchs++;
				searcher.doSearch();
			}
			acceptingResults = true;

			try {
				synchronized (this) {
					while (waitingSearchs > 0) {
						this.wait();
					}
				}
			} catch (InterruptedException e) {
				logger.fatal(e.getMessage(), e);
				status.setActualStep(SearchStep.FATAL_ERROR);
				return;
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(sr.getHits(), Hit.COMPARATOR);

			status.setResults(sr);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

}
