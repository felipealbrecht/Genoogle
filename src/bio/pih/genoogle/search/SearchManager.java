/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.SequencesProvider;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.search.SearchParams.Parameter;
import bio.pih.genoogle.search.results.SearchResults;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.SymbolList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Manage Searchers, check its status and stores and returns its results.
 * Each {@link Genoogle} instance has one {@link SearchManager}.
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
	
	public AbstractSequenceDataBank getDatabank(String name) {
		return databanks.get(name);
	}
	
	/**
	 * Process a batch of {@link SearchParams}
	 * 
	 * @return {@link List} of {@link SearchResults}, it does not give
	 *         guarantee that the first input SearchParam is the first in the
	 *         results list. 
	 */
	public List<SearchResults> doSyncSearch(BufferedReader in, String databankName, Map<Parameter, Object> parameters) throws UnknowDataBankException,
			InterruptedException, ExecutionException, NoSuchElementException, IOException, IllegalSymbolException, ParseException {

		long begin = System.currentTimeMillis();
		CompletionService<SearchResults> completionService = new ExecutorCompletionService<SearchResults>(
				requestsExecutor);

		AbstractSequenceDataBank databank = databanks.get(databankName);
		if (databank == null) {
			throw new UnknowDataBankException(databankName);
		}
		
		SequencesProvider provider = new SequencesProvider(in, databank.getAlphabet());
		
		int totalSubmited = 0;
		while(provider.hasNext()) {
			SymbolList nextSequence = provider.getNextSequence();
			if (nextSequence == null) {
				break;
			}
			
			SearchParams sp;
			if (parameters == null) {
				sp = new SearchParams(nextSequence, databankName);
			} else {
				sp = new SearchParams(nextSequence, databankName, parameters);
			}
			long id = getNextSearchId();
			final AbstractSearcher searcher = SearcherFactory.getSearcher(id, sp, databank);

			completionService.submit(searcher);
			totalSubmited ++;
		}

		List<SearchResults> results = Lists.newLinkedList();

		long prev = System.currentTimeMillis();		
		for (int i = 0; i < totalSubmited; i++) {
			Future<SearchResults> future = completionService.take();
			SearchResults results2 = future.get();
			results.add(results2);
			long c = System.currentTimeMillis();
			long total = c - prev;
			prev = c;
			profileLogger.info("  " + (i+1) +"/" +totalSubmited + " in " + (total) + " and total is " + (c - begin));
		}

		return results;
	}

	/**
	 * Do a search.
	 * 
	 * @param sp
	 * @return {@link SearchResults} of this search.
	 */
	public SearchResults doSyncSearch(SearchParams sp) throws UnknowDataBankException,
			InterruptedException, ExecutionException {
		logger.info("doSyncSearch on " + sp);
		AbstractSequenceDataBank databank = databanks.get(sp.getDatabank());
		if (databank == null) {
			throw new UnknowDataBankException(sp.getDatabank());
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
