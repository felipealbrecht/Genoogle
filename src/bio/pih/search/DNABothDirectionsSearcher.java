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

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

public class DNABothDirectionsSearcher extends AbstractSearcher {

	private DNASearcher searcher;
	private DNASearcher invertedSearcher;
	
	private static final Logger logger = Logger.getLogger(DNABothDirectionsSearcher.class.getName());
	private final IndexedDNASequenceDataBank databank;

	public DNABothDirectionsSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank) {
		super(id, sp, databank);
		this.databank = databank;			
	}

	@Override
	public SearchResults call() throws Exception {
		searcher = new DNASearcher(id, sp, databank);		
		invertedSearcher = new DNAInvertedSearcher(id, sp, databank);
		
		
		status.setActualStep(SearchStep.SEARCHING_INNER);
		long begin = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				executor);

		completionService.submit(searcher);
		completionService.submit(invertedSearcher);

		for (int i = 0; i < 2; i++) {
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
