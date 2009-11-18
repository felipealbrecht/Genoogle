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

import bio.pih.io.AbstractSequenceDataBank;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Manage Searchers, check its status and stores and returns its results.
 * 
 * @author albrecht
 */
public class SearchManager {

	private static Logger logger = Logger.getLogger(SearchManager.class.getName());
	private static Logger profileLogger = Logger.getLogger("profile");

	private Map<String, AbstractSequenceDataBank> databanks;
	private ExecutorService requestsExecutor = null;

	/**
	 * @param maxSimulaneousSearchs
	 * 
	 */
	public SearchManager(int maxSimulaneousSearchs) {
		databanks = Maps.newHashMap();
		requestsExecutor = Executors.newFixedThreadPool(maxSimulaneousSearchs);		
	}
	
	/**
	 * Shutdown the search manager.Service
	 * @throws InterruptedException 
	 */
	public void shutdown() throws InterruptedException {
		requestsExecutor.shutdown();
		requestsExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);			
	}
	
	/**
	 * @param databank
	 */
	public void addDatabank(AbstractSequenceDataBank databank) {
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

		long begin = System.currentTimeMillis();
		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				requestsExecutor);

		for (SearchParams sp : sps) {
			AbstractSequenceDataBank databank = databanks.get(sp.getDatabank());
			if (databank == null) {
				throw new UnknowDataBankException(this, sp.getDatabank());
			}
			long id = getNextSearchId();
			final AbstractSearcher searcher = SearcherFactory.getSearcher(id, sp, databank);

			completionService.submit(searcher);
		}

		List<SearchResults> results = Lists.newLinkedList();

		long prev = System.currentTimeMillis();		
		for (int i = 0; i < sps.size(); i++) {
			Future<SearchResults> future = completionService.take();
			SearchResults results2 = future.get();
			results.add(results2);
			long c = System.currentTimeMillis();
			long total = c - prev;
			prev = c;
			profileLogger.info("  " + (i+1) +"/" +sps.size() + " in " + (total) + " and total is " + (c - begin));
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
		AbstractSequenceDataBank databank = databanks.get(sp.getDatabank());
		if (databank == null) {
			throw new UnknowDataBankException(this, sp.getDatabank());
		}
		long id = getNextSearchId();
		final AbstractSearcher searcher = SearcherFactory.getSearcher(id, sp, databank);

		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				requestsExecutor);

		completionService.submit(searcher);
		return completionService.take().get();
	}

	/**
	 * @return {@link Collection} of all {@link AbstractSequenceDataBank} that this
	 *         {@link SearchResults} is managing.
	 */
	public Collection<AbstractSequenceDataBank> getDatabanks() {
		return databanks.values();
	}
	
	/**
	 * The name of the default data bank name. 
	 * For while will be the first data bank. 
	 * @return name of the default data bank.
	 */
	public String getDefaultDataBankName() {
		return this.databanks.keySet().iterator().next();
	}

	private long searchId = 0;

	private synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}
}
