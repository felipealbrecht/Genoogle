/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.Utils;
import bio.pih.genoogle.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

import com.google.common.collect.Lists;

public class IndexBothStrandSearcher implements Callable<List<BothStrandSequenceAreas>> {

	private IndexSearcher searcher;
	private IndexReverseComplementSearcher crSearcher;

	private static final Logger logger = Logger.getLogger(IndexBothStrandSearcher.class.getName());
	private final long id;
	private final SearchParams sp;
	private final IndexedSequenceDataBank databank;
	private final List<RetrievedArea>[] retrievedAreas;
	private final List<RetrievedArea>[] rcRetrievedAreas;
	private final List<Throwable> fails;
	private final ExecutorService executor;

	@SuppressWarnings("unchecked")
	public IndexBothStrandSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank,
			ExecutorService executor, List<Throwable> fails) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.fails = fails;
		int numberOfSequences = databank.getNumberOfSequences();
		this.retrievedAreas = new ArrayList[numberOfSequences];
		this.rcRetrievedAreas = new ArrayList[numberOfSequences];
		for (int i = 0; i < numberOfSequences; i++) {
			retrievedAreas[i] = new ArrayList<RetrievedArea>(0);
			rcRetrievedAreas[i] = new ArrayList<RetrievedArea>(0);
		}
	}

	@Override
	public List<BothStrandSequenceAreas> call() throws InterruptedException {
		long searchBegin = System.currentTimeMillis();

		SymbolList query = sp.getQuery();

		Statistics statistics = new Statistics(databank.getAlphabet(), databank.getEncoder(), sp.getMatchScore(), sp.getMismatchScore(), query, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());

		String seqString = query.seqString();

		int subSequenceLength = databank.getSubSequenceLength();
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(databank.getAlphabet(), subSequenceLength);

		int[] encodedQuery = encoder.encodeSymbolListToIntegerArray(query);
		String inverted = Utils.invert(query.seqString());
		String rcString = Utils.sequenceComplement(inverted);

		SymbolList rcQuery = null;

		// this try/catch should never happens, because the rc string is create by a verified sequence. 
		try {
			rcQuery = LightweightSymbolList.createDNA(rcString);
		} catch (IllegalSymbolException e) {			
			logger.fatal(e);
			return null;
		}

		int[] rcEncodedQuery = encoder.encodeSymbolListToIntegerArray(rcQuery);

		int length = query.getLength();

		int querySplitQuantity = sp.getQuerySplitQuantity();
		int minLength = sp.getMinQuerySliceLength();
		int sliceSize = length / querySplitQuantity;

		while (sliceSize < minLength && querySplitQuantity != 1) {
			querySplitQuantity--;
			sliceSize = length / querySplitQuantity;
		}

		CountDownLatch indexSearchersCountDown = new CountDownLatch(querySplitQuantity * 2);

		logger.info("(" + id + ") " + querySplitQuantity + " threads with slice query with " + length + " bases.");
		for (int i = 0; i < querySplitQuantity; i++) {
			int begin = (sliceSize * i);
			int end = (sliceSize * i) + sliceSize + (sp.getMinHspLength() - subSequenceLength);
			if (end > length) {
				end = length;
			}
			logger.info("(" + id + ") " + i + " [" + begin + " - " + end + "].");
			String sliceQuery = seqString.substring(begin, end);
			String rcSliceQuery = rcString.substring(begin, end);
			submitSearch(sliceQuery, begin, query, encodedQuery, statistics, indexSearchersCountDown);
			submitRCSearch(rcSliceQuery, begin, rcQuery, rcEncodedQuery, statistics, indexSearchersCountDown);
		}

		indexSearchersCountDown.await();

		if (fails.size() > 0) {
			return null;
		}

		List<BothStrandSequenceAreas> results = Lists.newLinkedList();

		int numberOfSequences = databank.getNumberOfSequences();
		for (int i = 0; i < numberOfSequences; i++) {
			List<RetrievedArea> areas1 = retrievedAreas[i];
			List<RetrievedArea> areas2 = rcRetrievedAreas[i];

			if (areas1.size() > 0 || areas2.size() > 0) {
				BothStrandSequenceAreas retrievedAreas = new BothStrandSequenceAreas(i, searcher, crSearcher, areas1, areas2);
				results.add(retrievedAreas);
			}
		}

		logger.info("(" + id + ") " + "Index search time: " + (System.currentTimeMillis() - searchBegin));

		return results;
	}

	private void submitSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown) {
		searcher = new IndexSearcher(id, sp, databank, sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails);
		executor.submit(searcher);
	}

	private void submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown) {
		crSearcher = new IndexReverseComplementSearcher(id, sp, databank, sliceQuery, offset, fullQuery, encodedQuery, rcRetrievedAreas, statistics, countDown, fails);
		executor.submit(crSearcher);
	}
}
