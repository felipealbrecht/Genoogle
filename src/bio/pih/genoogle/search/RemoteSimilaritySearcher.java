/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009,2010,2011,2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import bio.pih.genoogle.alignment.SubstitutionMatrix;
import bio.pih.genoogle.io.RemoteSimilaritySequenceDataBank;
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
public class RemoteSimilaritySearcher extends AbstractSearcher {

	private static Logger logger = Logger.getLogger(RemoteSimilaritySearcher.class.getName());
	
	private final RemoteSimilaritySequenceDataBank databank;

	public RemoteSimilaritySearcher(long code, SearchParams sp, RemoteSimilaritySequenceDataBank databank) {
		super(code, sp, databank);
		this.databank = databank;
	}

	@Override
	public SearchResults call() {
		long begin = System.currentTimeMillis();

		ExecutorService queryExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsIndexSearch());
		
		List<Throwable> fails = Lists.newLinkedList();
		fails = Collections.synchronizedList(fails);
		final IndexSixFramesSearcher indexSearcher = new IndexSixFramesSearcher(id, sp, databank, queryExecutor, fails);		
		
		IndexSearchResults indexSearchResults = null;
		try {
			indexSearchResults  = indexSearcher.call();
		} catch (InterruptedException e) {
			sr.addFail(e);
			return sr;
		} 

		queryExecutor.shutdown();

		if (fails.size() > 0) {
			sr.addAllFails(fails);
			return sr;
		}

		logger.info("Index Searcher " + this.toString() + " " + (System.currentTimeMillis() - begin));

		long alignmentBegin = System.currentTimeMillis();

		ExecutorService alignerExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsExtendAlign());

		int maxHits = sp.getMaxHitsResults() > 0 ? sp.getMaxHitsResults() : indexSearchResults.size();
		maxHits = Math.min(maxHits, indexSearchResults.size());

		CountDownLatch alignnmentsCountDown = new CountDownLatch(maxHits);

		try {
			for (int i = 0; i < maxHits; i++) {
				RetrievedSequenceAreas retrievedArea = indexSearchResults.get(i);
				SequenceAligner sequenceAligner = new SequenceAligner(alignnmentsCountDown, indexSearchResults.getIndexSearchers(), retrievedArea, 
						sr, databank, databank.getEncoder(), databank.getAaEncoder(), databank.getReducedEncoder(), 
						// TODO: be possible to set the substitution matrix
						SubstitutionMatrix.BLOSUM62);
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
