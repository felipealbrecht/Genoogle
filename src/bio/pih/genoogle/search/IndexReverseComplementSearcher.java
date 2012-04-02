/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

public class IndexReverseComplementSearcher extends IndexReverseSearcher {


	public IndexReverseComplementSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank, SequenceEncoder encoder, int subSequenceLength,
			String rcSliceQuery, int offset, SymbolList rcQuery, int[] rcEncodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, CountDownLatch countDown, List<Throwable> fails, int readFrame) {
		super(id, sp, databank, encoder, subSequenceLength, rcSliceQuery, offset, rcQuery, rcEncodedQuery, rcRetrievedAreas, statistics, countDown, fails, readFrame);
	}
	
	public IndexReverseComplementSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank, 
			String rcSliceQuery, int offset, SymbolList rcQuery, int[] rcEncodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, CountDownLatch countDown, List<Throwable> fails, int readFrame) {
		super(id, sp, databank, rcSliceQuery, offset, rcQuery, rcEncodedQuery, rcRetrievedAreas, statistics, countDown, fails, readFrame);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (complement inverted) ");
		return sb.toString();
	}
}