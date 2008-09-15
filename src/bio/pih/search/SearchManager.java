package bio.pih.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Manage Searchers, check its status and stores and returns its results.
 * 
 * @author albrecht
 */
public class SearchManager {

	static Logger logger = Logger.getLogger(SearchManager.class.getName());

	Map<String, SequenceDataBank> databanks;
	ExecutorService executor = null;

	/**
	 * @param maxSimulaneousSearchs
	 * 
	 */
	public SearchManager(int maxSimulaneousSearchs) {
		databanks = Maps.newHashMap();
		executor = Executors.newFixedThreadPool(maxSimulaneousSearchs);
	}
	
	/**
	 * Shutdown the search manager.
	 * @throws InterruptedException 
	 */
	public void shutdown() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.MILLISECONDS);			
	}

	/**
	 * @param databank
	 */
	public void addDatabank(SequenceDataBank databank) {
		databanks.put(databank.getName(), databank);
	}

	/**
	 * Process a batch of {@link SearchParams}
	 * 
	 * @param sps
	 * @return a {@link List} of {@link SearchResults}, it does not give
	 *         guarantee that the first input SearchParam is the first in the
	 *         results list.
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<SearchResults> doSyncSearch(List<SearchParams> sps) throws UnknowDataBankException,
			InterruptedException, ExecutionException {

		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				executor);

		for (SearchParams sp : sps) {
			logger.info("doSearch on " + sp);

			SequenceDataBank databank = databanks.get(sp.getDatabank());
			if (databank == null) {
				throw new UnknowDataBankException(this, sp.getDatabank());
			}
			long id = getNextSearchId();
			final AbstractSearcher searcher = SearcherFactory.getSearcher(id, sp, databank);

			completionService.submit(searcher);
		}

		List<SearchResults> results = Lists.newLinkedList();

		for (int i = 0; i < sps.size(); i++) {
			Future<SearchResults> future = completionService.take();
			results.add(future.get());
		}

		return results;
	}

	/**
	 * Do a search.
	 * 
	 * @param sp
	 * @return {@link SearchResults} of this search.
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public SearchResults doSyncSearch(SearchParams sp) throws UnknowDataBankException,
			InterruptedException, ExecutionException {
		logger.info("doSearch on " + sp);
		SequenceDataBank databank = databanks.get(sp.getDatabank());
		if (databank == null) {
			throw new UnknowDataBankException(this, sp.getDatabank());
		}
		long id = getNextSearchId();
		final AbstractSearcher searcher = SearcherFactory.getSearcher(id, sp, databank);

		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				executor);

		completionService.submit(searcher);
		return completionService.take().get();
	}

	/**
	 * @return {@link Collection} of all {@link SequenceDataBank} that this
	 *         {@link SearchResults} is managing.
	 */
	public Collection<SequenceDataBank> getDatabanks() {
		return databanks.values();
	}

	private long searchId = 0;

	private synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}
}
