/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import bio.pih.genoogle.io.AbstractDatabankCollection;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.search.results.Hit;
import bio.pih.genoogle.search.results.SearchResults;

import com.google.common.collect.Lists;

/**
 * A searcher that does search operation at each data bank of its collection.
 * 
 * @author albrecht
 * 
 */
public class CollectionSearcher extends AbstractSearcher {

	private static Logger logger = Logger.getLogger(CollectionSearcher.class.getName());

	private final AbstractDatabankCollection<AbstractSequenceDataBank> databankCollection;

	public CollectionSearcher(long code, SearchParams sp, AbstractDatabankCollection<AbstractSequenceDataBank> databank) {
		super(code, sp, databank);
		this.databankCollection = databank;
	}

	@Override
	public SearchResults call() {
		long begin = System.currentTimeMillis();

		int indexSearchers = databankCollection.size();

		ExecutorService subDatabanksExecutor = Executors.newFixedThreadPool(indexSearchers);
		CompletionService<IndexSearchResults> subDataBanksCS = new ExecutorCompletionService<IndexSearchResults>(subDatabanksExecutor);

		ExecutorService queryExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsIndexSearch());

		List<Throwable> fails = Lists.newLinkedList();
		fails = Collections.synchronizedList(fails);
		Iterator<AbstractSequenceDataBank> it = databankCollection.databanksIterator();
		while (it.hasNext()) {
			AbstractSequenceDataBank innerBank = it.next();
			final IndexBothStrandSearcher indexSearcher = new IndexBothStrandSearcher(id, sp, (IndexedSequenceDataBank) innerBank, queryExecutor, fails);
			subDataBanksCS.submit(indexSearcher);
		}

		IndexSearchResults indexSearchResults = null;
		try {
			;
			for (int i = 0; i < indexSearchers; i++) {
				IndexSearchResults subResults = subDataBanksCS.take().get();
				if (subResults == null) {
					logger.error("Results from searcher " + i + " was empty.");
				} else {
					if (indexSearchResults == null) {
						indexSearchResults = subResults;
					} else {
						indexSearchResults.merge(subResults);
					}
				}
			}
		} catch (InterruptedException e) {
			sr.addFail(e);
			return sr;
		} catch (ExecutionException e) {
			sr.addFail(e);
			return sr;
		}

		queryExecutor.shutdown();
		subDatabanksExecutor.shutdown();

		if (fails.size() > 0) {
			sr.addAllFails(fails);
			return sr;
		}

		logger.info("DNAIndexBothStrandSearcher total Time of " + this.toString() + " " + (System.currentTimeMillis() - begin));

		long alignmentBegin = System.currentTimeMillis();

		ExecutorService alignerExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsExtendAlign());

		int maxHits = sp.getMaxHitsResults() > 0 ? sp.getMaxHitsResults() : indexSearchResults.size();
		maxHits = Math.min(maxHits, indexSearchResults.size());

		CountDownLatch alignnmentsCountDown = new CountDownLatch(maxHits);

		try {
			for (int i = 0; i < maxHits; i++) {
				RetrievedSequenceAreas retrievedArea = indexSearchResults.get(i);
				SequenceAligner sequenceAligner = new SequenceAligner(alignnmentsCountDown, indexSearchResults.getIndexSearchers(), retrievedArea, sr, databankCollection);
				alignerExecutor.submit(sequenceAligner);
			}
		} catch (IOException e) {
			sr.addFail(e);
			return sr;
		}

		try {
			alignnmentsCountDown.await();
		} catch (InterruptedException e) {
			sr.addFail(e);
			return sr;
		}

		alignerExecutor.shutdown();

		ListIterator<Hit> hitsIterator = sr.getHits().listIterator();
		while (hitsIterator.hasNext()) {
			Hit hit = hitsIterator.next();
			filterHSPs(hit.getHSPs());
			if (hit.getHSPs().isEmpty()) {
				hitsIterator.remove();
			} else {
				Collections.sort(hit.getHSPs(), HSP.COMPARATOR);
			}
		}

		Collections.sort(sr.getHits(), Hit.COMPARATOR);
		logger.info("Alignments total Time of " + this.toString() + " " + (System.currentTimeMillis() - alignmentBegin));
		logger.info("Total Time of " + this.toString() + " " + (System.currentTimeMillis() - begin));

		return sr;
	}

	private void filterHSPs(List<HSP> HSPs) {
		ListIterator<HSP> iterator = HSPs.listIterator();
		while (iterator.hasNext()) {
			HSP hsp = iterator.next();
			if (hsp.getEValue() >= 0.1) {
				iterator.remove();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" CollectionSearcher ");
		return sb.toString();
	}
}