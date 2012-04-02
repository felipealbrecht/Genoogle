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
import bio.pih.genoogle.alignment.SubstitutionMatrix;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.RemoteSimilaritySequenceDataBank;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.SubstitutionMatrixStatistics;

import com.google.common.collect.Lists;

public class IndexSixFramesSearcher implements Callable<List<RetrievedSequenceAreas>> {

	private IndexSearcher searcher;
	private IndexReverseComplementSearcher crSearcher;

	private static final Logger logger = Logger.getLogger(IndexSixFramesSearcher.class.getName());
	private final long id;
	private final SearchParams sp;
	private final RemoteSimilaritySequenceDataBank databank;
	private final ArrayList<RetrievedArea>[] retrievedAreasFrame1;
	private final ArrayList<RetrievedArea>[] retrievedAreasFrame2;
	private final ArrayList<RetrievedArea>[] retrievedAreasFrame3;
	private final ArrayList<RetrievedArea>[] rcRetrievedAreasFrame1;
	private final ArrayList<RetrievedArea>[] rcRetrievedAreasFrame2;
	private final ArrayList<RetrievedArea>[] rcRetrievedAreasFrame3;
	private final List<Throwable> fails;
	private final ExecutorService executor;
	private final SequenceEncoder encoder;

		
	@SuppressWarnings("unchecked")
	public IndexSixFramesSearcher(long id, SearchParams sp, RemoteSimilaritySequenceDataBank databank,
			ExecutorService executor, List<Throwable> fails) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.fails = fails;
		int numberOfSequences = databank.getNumberOfSequences();
		this.encoder = databank.getReducedEncoder();
		this.retrievedAreasFrame1 = new ArrayList[numberOfSequences];
		this.retrievedAreasFrame2 = new ArrayList[numberOfSequences];
		this.retrievedAreasFrame3 = new ArrayList[numberOfSequences];
		this.rcRetrievedAreasFrame1 = new ArrayList[numberOfSequences];
		this.rcRetrievedAreasFrame2 = new ArrayList[numberOfSequences];
		this.rcRetrievedAreasFrame3 = new ArrayList[numberOfSequences];
		for (int i = 0; i < numberOfSequences; i++) {
			retrievedAreasFrame1[i] = new ArrayList<RetrievedArea>(0);
			retrievedAreasFrame2[i] = new ArrayList<RetrievedArea>(0);
			retrievedAreasFrame3[i] = new ArrayList<RetrievedArea>(0);
			rcRetrievedAreasFrame1[i] = new ArrayList<RetrievedArea>(0);
			rcRetrievedAreasFrame2[i] = new ArrayList<RetrievedArea>(0);
			rcRetrievedAreasFrame3[i] = new ArrayList<RetrievedArea>(0);
		}
	}

	// TODO: Fix statistics (correct alphabet and match and mismatch scores)	
	@Override
	public List<RetrievedSequenceAreas> call() throws InterruptedException {
		long searchBegin = System.currentTimeMillis();

		SymbolList query = sp.getQuery();

		SymbolList dnaToProtein1 = Converter.dnaToProtein(query);
		SymbolList dnaToProtein2 = Converter.dnaToProtein2(query);
		SymbolList dnaToProtein3 = Converter.dnaToProtein3(query);
		SymbolList dnaToProteinComplement1 = Converter.dnaToProteinComplement1(query);
		SymbolList dnaToProteinComplement2 = Converter.dnaToProteinComplement2(query);
		SymbolList dnaToProteinComplement3 = Converter.dnaToProteinComplement3(query);
		
		System.out.println(dnaToProtein1);
		System.out.println(dnaToProtein2);
		System.out.println(dnaToProtein3);
		System.out.println(dnaToProteinComplement1);
		System.out.println(dnaToProteinComplement2);
		System.out.println(dnaToProteinComplement3);
		
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
		
		submitSearch(read1.seqString(), 0, dnaToProtein1, encodedReducedRead1, indexSearchersCountDown, 1, retrievedAreasFrame1);
		submitSearch(read2.seqString(), 0, dnaToProtein2, encodedReducedRead2, indexSearchersCountDown, 2, retrievedAreasFrame2);
		submitSearch(read3.seqString(), 0, dnaToProtein3, encodedReducedRead3, indexSearchersCountDown, 3, retrievedAreasFrame3);
		
		submitRCSearch(complement1.seqString(), 0, dnaToProteinComplement1, encodedReducedComplement1, indexSearchersCountDown, 1, rcRetrievedAreasFrame1);
		submitRCSearch(complement2.seqString(), 0, dnaToProteinComplement2, encodedReducedComplement3, indexSearchersCountDown, 2, rcRetrievedAreasFrame2);
		submitRCSearch(complement3.seqString(), 0, dnaToProteinComplement3, encodedReducedComplement2, indexSearchersCountDown, 3, rcRetrievedAreasFrame3);
		
		indexSearchersCountDown.await();
		
		if (fails.size() > 0) {
			return null;
		}

		List<RetrievedSequenceAreas> results = Lists.newLinkedList();

		int numberOfSequences = databank.getNumberOfSequences();
		for (int i = 0; i < numberOfSequences; i++) {
			ArrayList<RetrievedArea> areas1 = retrievedAreasFrame1[i];
			ArrayList<RetrievedArea> areas2 = retrievedAreasFrame2[i];
			ArrayList<RetrievedArea> areas3 = retrievedAreasFrame3[i];
			ArrayList<RetrievedArea> areas4 = rcRetrievedAreasFrame1[i];
			ArrayList<RetrievedArea> areas5 = rcRetrievedAreasFrame2[i];
			ArrayList<RetrievedArea> areas6 = rcRetrievedAreasFrame3[i];
			
			if (areas1.size() > 0 || areas2.size() > 0 || areas3.size() > 0 || areas4.size() > 0 || areas5.size() > 0 || areas6.size() > 0) {
				RetrievedSequenceAreas retrievedAreas = new RetrievedSequenceAreas(i, searcher, crSearcher, 3, areas1, areas2, areas3, areas4, areas5, areas6);
				results.add(retrievedAreas);
			}
		}

		logger.info("(" + id + ") " + "Index search time: " + (System.currentTimeMillis() - searchBegin));

		return results;
	}

	private void submitSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			CountDownLatch countDown, int frame, List<RetrievedArea>[] retrievedAreas) {
		SubstitutionMatrixStatistics statistics = new SubstitutionMatrixStatistics(databank.getAaEncoder().getAlphabet(), SubstitutionMatrix.BLOSUM62,
				 fullQuery, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());
		
		searcher = new IndexSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength() , sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails, frame);
		executor.submit(searcher);
	}

	private void submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			CountDownLatch countDown, int frame, List<RetrievedArea>[] retrievedAreas) {
		SubstitutionMatrixStatistics statistics = new SubstitutionMatrixStatistics(databank.getAaEncoder().getAlphabet(), SubstitutionMatrix.BLOSUM62, 
				fullQuery, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());
		
		crSearcher = new IndexReverseComplementSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength(), sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails, frame);
		executor.submit(crSearcher);
	}
}
