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
import bio.pih.genoogle.io.RemoteSimilaritySequenceDataBank;
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
	private final RemoteSimilaritySequenceDataBank databank;
	private final List<RetrievedArea>[] retrievedAreas;
	private final List<RetrievedArea>[] rcRetrievedAreas;
	private final List<Throwable> fails;
	private final ExecutorService executor;
	private final SequenceEncoder encoder;

		
	public IndexSixFramesSearcher(long id, SearchParams sp, RemoteSimilaritySequenceDataBank databank,
			ExecutorService executor, List<Throwable> fails) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.fails = fails;
		int numberOfSequences = databank.getNumberOfSequences();
		this.encoder = databank.getReducedEncoder();
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

		SymbolList dnaToProtein1 = Converter.dnaToProtein(query);
		SymbolList dnaToProtein2 = Converter.dnaToProtein2(query);
		SymbolList dnaToProtein3 = Converter.dnaToProtein3(query);
		SymbolList dnaToProteinComplement1 = Converter.dnaToProteinComplement1(query);
		SymbolList dnaToProteinComplement2 = Converter.dnaToProteinComplement2(query);
		SymbolList dnaToProteinComplement3 = Converter.dnaToProteinComplement3(query);
		
		SymbolList read1 = Converter.proteinToReducedAA(dnaToProtein1);		
		SymbolList read2 = Converter.proteinToReducedAA(dnaToProtein2);		
		SymbolList read3 = Converter.proteinToReducedAA(dnaToProtein3);		
		SymbolList complement1 = Converter.proteinToReducedAA(dnaToProteinComplement1);		
		SymbolList complement2 = Converter.proteinToReducedAA(dnaToProteinComplement2);
		SymbolList complement3 = Converter.proteinToReducedAA(dnaToProteinComplement3);
		
		

		int[] encodedReducedRead1 = encoder.encodeSymbolListToIntegerArray(read1);
		int[] encodedReducedRead2 = encoder.encodeSymbolListToIntegerArray(read2);
		int[] encodedReducedRead3 = encoder.encodeSymbolListToIntegerArray(read3);
		int[] encodedReducedComplement1 = encoder.encodeSymbolListToIntegerArray(complement1);
		int[] encodedReducedComplement2 = encoder.encodeSymbolListToIntegerArray(complement2);
		int[] encodedReducedComplement3 = encoder.encodeSymbolListToIntegerArray(complement3);
		
		CountDownLatch indexSearchersCountDown = new CountDownLatch(6);
		
		submitSearch(read1.seqString(), 0, dnaToProtein1, encodedReducedRead1, statistics, indexSearchersCountDown, 1);
		submitSearch(read2.seqString(), 0, dnaToProtein2, encodedReducedRead2, statistics, indexSearchersCountDown, 2);
		submitSearch(read3.seqString(), 0, dnaToProtein3, encodedReducedRead3, statistics, indexSearchersCountDown, 3);
		
		submitRCSearch(complement1.seqString(), 0, dnaToProteinComplement1, encodedReducedComplement1, statistics, indexSearchersCountDown, 1);
		submitRCSearch(complement2.seqString(), 0, dnaToProteinComplement2, encodedReducedComplement3, statistics, indexSearchersCountDown, 2);
		submitRCSearch(complement3.seqString(), 0, dnaToProteinComplement3, encodedReducedComplement2, statistics, indexSearchersCountDown, 3);
		
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
			Statistics statistics, CountDownLatch countDown, int frame) {
		System.out.println(fullQuery);
		searcher = new IndexSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength() , sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails);
		executor.submit(searcher);
	}

	private void submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown, int frame) {
		System.out.println(fullQuery);
		crSearcher = new IndexReverseComplementSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength(), sliceQuery, offset, fullQuery, encodedQuery, rcRetrievedAreas, statistics, countDown, fails);
		executor.submit(crSearcher);
	}
}
