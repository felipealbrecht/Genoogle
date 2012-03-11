/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
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

import pih.bio.genoogle.seq.protein.Converter;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.seq.Reduced_AA_8_Alphabet;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

import com.google.common.collect.Lists;

public class IndexSixFramesSearcher implements Callable<List<BothStrandSequenceAreas>> {

	private IndexSearcher searcher;
	private IndexReverseComplementSearcher crSearcher;

	private static final Logger logger = Logger.getLogger(IndexSixFramesSearcher.class.getName());
	private final long id;
	private final SearchParams sp;
	private final IndexedSequenceDataBank databank;
	private final List<RetrievedArea>[] retrievedAreas;
	private final List<RetrievedArea>[] rcRetrievedAreas;
	private final List<Throwable> fails;
	private final ExecutorService executor;
	private final SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(Reduced_AA_8_Alphabet.SINGLETON, 3);

	@SuppressWarnings("unchecked")
	public IndexSixFramesSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank,
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

	// TODO: Fix statistics (correct alphabet and match and mismatch scores)	
	@Override
	public List<BothStrandSequenceAreas> call() throws InterruptedException {
		long searchBegin = System.currentTimeMillis();

		SymbolList query = sp.getQuery();

		Statistics statistics = new Statistics(databank.getAlphabet(), databank.getEncoder(), sp.getMatchScore(), sp.getMismatchScore(), query, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());

		SymbolList read1 = Converter.proteinToReducedAA(Converter.dnaToProtein(query));
		SymbolList read2 = Converter.proteinToReducedAA(Converter.dnaToProtein2(query));
		SymbolList read3 = Converter.proteinToReducedAA(Converter.dnaToProtein3(query));
		SymbolList complement1 = Converter.proteinToReducedAA(Converter.dnaToProteinComplement1(query));
		SymbolList complement2 = Converter.proteinToReducedAA(Converter.dnaToProteinComplement2(query));
		SymbolList complement3 = Converter.proteinToReducedAA(Converter.dnaToProteinComplement3(query));

		int[] encodedRead1 = encoder.encodeSymbolListToIntegerArray(read1);
		int[] encodedRead2 = encoder.encodeSymbolListToIntegerArray(read2);
		int[] encodedRead3 = encoder.encodeSymbolListToIntegerArray(read3);
		int[] encodedComplement1 = encoder.encodeSymbolListToIntegerArray(complement1);
		int[] encodedComplement2 = encoder.encodeSymbolListToIntegerArray(complement2);
		int[] encodedComplement3 = encoder.encodeSymbolListToIntegerArray(complement3);
		
		CountDownLatch indexSearchersCountDown = new CountDownLatch(6);
		
		submitSearch(read1.seqString(), 0, read1, encodedRead1, statistics, indexSearchersCountDown);
		submitSearch(read2.seqString(), 0, read2, encodedRead2, statistics, indexSearchersCountDown);
		submitSearch(read3.seqString(), 0, read3, encodedRead3, statistics, indexSearchersCountDown);
		
		submitRCSearch(complement1.seqString(), 0, complement1, encodedComplement1, statistics, indexSearchersCountDown);
		submitRCSearch(complement2.seqString(), 0, complement2, encodedComplement3, statistics, indexSearchersCountDown);
		submitRCSearch(complement3.seqString(), 0, complement3, encodedComplement2, statistics, indexSearchersCountDown);
		
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
		searcher = new IndexSearcher(id, sp, databank, encoder, 3, sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails);
		executor.submit(searcher);
	}

	private void submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown) {
		crSearcher = new IndexReverseComplementSearcher(id, sp, databank, encoder, 3, sliceQuery, offset, fullQuery, encodedQuery, rcRetrievedAreas, statistics, countDown, fails);
		executor.submit(crSearcher);
	}
}
