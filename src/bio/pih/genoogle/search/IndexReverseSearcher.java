/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bio.pih.genoogle.alignment.DividedStringGenoogleSmithWaterman;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

public class IndexReverseSearcher extends IndexSearcher {

	public IndexReverseSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank, 
			String sliceQuery, int offset, SymbolList query, int[] encodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, 
			CountDownLatch countDown, List<Throwable> fails) {
		super(id, sp, databank, sliceQuery, offset, query, encodedQuery, rcRetrievedAreas, statistics, countDown, fails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (inverted) ");
		return sb.toString();
	}

	@Override
	protected HSP createHSP(ExtendSequences extensionResult, DividedStringGenoogleSmithWaterman smithWaterman,
			double normalizedScore, double evalue, int queryLength, int targetLength) {
		return new HSP(smithWaterman, getQueryStart(extensionResult, smithWaterman), getQueryEnd(
				extensionResult, smithWaterman), getTargetStart(extensionResult, smithWaterman,
				targetLength), getTargetEnd(extensionResult, smithWaterman, targetLength),
				normalizedScore, evalue);
	}

	private int getQueryStart(ExtendSequences extensionResult, DividedStringGenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart();
	}

	private int getQueryEnd(ExtendSequences extensionResult, DividedStringGenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd();
	}

	private int getTargetStart(ExtendSequences extensionResult,
			DividedStringGenoogleSmithWaterman smithWaterman, int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}

	private int getTargetEnd(ExtendSequences extensionResult, DividedStringGenoogleSmithWaterman smithWaterman,
			int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}
}
