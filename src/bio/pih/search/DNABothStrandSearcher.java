package bio.pih.search;

import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

public class DNABothStrandSearcher extends AbstractSearcher {

	private DNASearcher searcher;
	private DNASearcher complementInvertedSearcher;
	
	private static final Logger logger = Logger.getLogger(DNABothStrandSearcher.class.getName());
	private final IndexedDNASequenceDataBank databank;

	public DNABothStrandSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, 
			ExecutorService executor) {
		super(id, sp, databank, executor);
		this.databank = databank;			
	}

	@Override
	public SearchResults call() throws Exception {	
		long begin = System.currentTimeMillis();
		searcher = new DNASearcher(id, sp, databank, executor);
		complementInvertedSearcher = new DNAReverseComplementSearcher(id, sp, databank, executor);
				
		status.setActualStep(SearchStep.SEARCHING_INNER);
		CompletionService<SearchResults> completionService = 
			new ExecutorCompletionService<SearchResults>(executor);

		int total = 0;
		Future<SearchResults> searcherFuture = 
			completionService.submit(searcher); total++; 	
		Future<SearchResults> complInvertedSearcherFuture = 
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
	
		logger.info("Total Time of " + this.toString() + " " + (System.currentTimeMillis() - begin));						
		return sr;
	}
}
