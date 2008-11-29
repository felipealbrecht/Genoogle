package bio.pih.search;

import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

public class DNABothDirectionsSearcher extends AbstractSearcher {

	private DNASearcher searcher;
	private DNASearcher invertedSearcher;
	private DNASearcher complementSearcher;
	private DNASearcher complementInvertedSearcher;
	
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
		complementSearcher = new DNAComplementSearcher(id, sp, databank);
		complementInvertedSearcher = new DNAComplementInvertedSearcher(id, sp, databank);
		
		
		status.setActualStep(SearchStep.SEARCHING_INNER);
		long begin = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				executor);

		int total = 0;
		completionService.submit(searcher); total++; 
		completionService.submit(invertedSearcher); total++;
		completionService.submit(complementSearcher); total++;
		completionService.submit(complementInvertedSearcher); total++;
		
		for (int i = 0; i < total; i++) {
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

		status.setActualStep(SearchStep.SORTING);
		
		for (Hit hit: sr.getHits()) {
			Collections.sort(hit.getHSPs(), HSP.COMPARATOR);
		}
		Collections.sort(sr.getHits(), Hit.COMPARATOR);
		
		status.setResults(sr);
		status.setActualStep(SearchStep.FINISHED);
		
		executor.shutdown();
						
		return sr;
	}
}
