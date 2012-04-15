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

import bio.pih.genoogle.alignment.SubstitutionMatrix;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.RemoteSimilaritySequenceDataBank;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.seq.protein.Converter;
import bio.pih.genoogle.statistics.SubstitutionMatrixStatistics;

public class IndexSixFramesSearcher implements Callable<IndexSearchResults> {
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
	}

	// TODO: Fix statistics (correct alphabet and match and mismatch scores)	
	@Override
	public IndexSearchResults call() throws InterruptedException {
		long searchBegin = System.currentTimeMillis();

		SymbolList query = sp.getQuery();

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
		
		IndexSearcher searcher1 = submitSearch(read1.seqString(), 0, dnaToProtein1, encodedReducedRead1, indexSearchersCountDown, 1, retrievedAreasFrame1);
		IndexSearcher searcher2 = submitSearch(read2.seqString(), 0, dnaToProtein2, encodedReducedRead2, indexSearchersCountDown, 2, retrievedAreasFrame2);
		IndexSearcher searcher3 = submitSearch(read3.seqString(), 0, dnaToProtein3, encodedReducedRead3, indexSearchersCountDown, 3, retrievedAreasFrame3);
		
		IndexSearcher rcSearcher1 = submitRCSearch(complement1.seqString(), 0, dnaToProteinComplement1, encodedReducedComplement1, indexSearchersCountDown, 1, rcRetrievedAreasFrame1);
		IndexSearcher rcSearcher2 = submitRCSearch(complement2.seqString(), 0, dnaToProteinComplement2, encodedReducedComplement2, indexSearchersCountDown, 2, rcRetrievedAreasFrame2);
		IndexSearcher rcSearcher3 = submitRCSearch(complement3.seqString(), 0, dnaToProteinComplement3, encodedReducedComplement3, indexSearchersCountDown, 3, rcRetrievedAreasFrame3);		
		
		indexSearchersCountDown.await();
		
		if (fails.size() > 0) {
			return null;
		}

		IndexSearchResults results = new IndexSearchResults(searcher1, searcher2, searcher3, rcSearcher1, rcSearcher2, rcSearcher3);
		
		int numberOfSequences = databank.getNumberOfSequences();
		for (int i = 0; i < numberOfSequences; i++) {
			ArrayList<RetrievedArea> areas1 = retrievedAreasFrame1[i];
			ArrayList<RetrievedArea> areas2 = retrievedAreasFrame2[i];
			ArrayList<RetrievedArea> areas3 = retrievedAreasFrame3[i];
			ArrayList<RetrievedArea> areas4 = rcRetrievedAreasFrame1[i];
			ArrayList<RetrievedArea> areas5 = rcRetrievedAreasFrame2[i];
			ArrayList<RetrievedArea> areas6 = rcRetrievedAreasFrame3[i];
			
			if (areas1 != null || areas2 != null || areas3 != null || areas4 != null || areas5 != null || areas6 != null) {
				@SuppressWarnings("unchecked")
				RetrievedSequenceAreas retrievedAreas = new RetrievedSequenceAreas(i, searcher1.getDatabank(), areas1, areas2, areas3, areas4, areas5, areas6);
				results.add(retrievedAreas);
			}
		}

		logger.info("(" + id + ") " + "Index search time: " + (System.currentTimeMillis() - searchBegin));

		return results;
	}


	private IndexSearcher submitSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			CountDownLatch countDown, int frame, List<RetrievedArea>[] retrievedAreas) {
		SubstitutionMatrixStatistics statistics = new SubstitutionMatrixStatistics(databank.getAaEncoder().getAlphabet(), SubstitutionMatrix.BLOSUM62,
				 fullQuery, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());
		
		IndexSearcher searcher = new IndexSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength() , sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails, frame);
		executor.submit(searcher);
		return searcher;
	}

	private IndexSearcher submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			CountDownLatch countDown, int frame, List<RetrievedArea>[] retrievedAreas) {
		SubstitutionMatrixStatistics statistics = new SubstitutionMatrixStatistics(databank.getAaEncoder().getAlphabet(), SubstitutionMatrix.BLOSUM62, 
				fullQuery, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences());
		
		IndexSearcher crSearcher = new IndexReverseComplementSearcher(id, sp, databank, encoder, encoder.getSubSequenceLength(), sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails, frame);
		executor.submit(crSearcher);
		return crSearcher;
	}
}
