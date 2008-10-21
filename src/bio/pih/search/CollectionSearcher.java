package bio.pih.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
	private final DatabankCollection<SequenceDataBank> databankCollection;

	/**
	 * @param code
	 * @param sp
	 * @param databank
	 * @param sm
	 * @param parent
	 */
	public CollectionSearcher(long code, SearchParams sp,
			DatabankCollection<SequenceDataBank> databank) {
		super(code, sp, databank);
		this.databankCollection = databank;
	}

	@Override
	public SearchResults call() {
		status.setActualStep(SearchStep.SEARCHING_INNER);
		long begin = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(databankCollection.getMaxThreads());
		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				executor);

		Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
		while (it.hasNext()) {
			SequenceDataBank innerBank = it.next();
			final AbstractSearcher searcher = SearcherFactory.getSearcher(-1, sp, innerBank);
			completionService.submit(searcher);
		}

		for (int i = 0; i < databankCollection.size(); i++) {
			Future<SearchResults> future;
			SearchResults searchResults;
			try {
				future = completionService.take();
				searchResults = future.get();
			} catch (InterruptedException e) {
				sr.addFail(e);
				return sr;
			} catch (ExecutionException e) {
				sr.addFail(e);
				return sr;
			}

			if (searchResults.hasFail()) {
				sr.addAllFails(searchResults.getFails());
			} else {
				sr.addAllHits(searchResults.getHits());
			}
		}

		status.setActualStep(SearchStep.SELECTING);

		Collections.sort(sr.getHits(), Hit.COMPARATOR);

		status.setResults(sr);
		status.setActualStep(SearchStep.FINISHED);
		
		executor.shutdown();
		logger.info("Total Time of " + this.toString() + " " + (System.currentTimeMillis() - begin));

		return sr;
	}
}