/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import bio.pih.genoogle.io.IndexedDNASequenceDataBank;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

public class DNAIndexReverseComplementSearcher extends DNAIndexReverseSearcher {

	public DNAIndexReverseComplementSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, 
			String rcSliceQuery, int offset, SymbolList rcQuery, int[] rcEncodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, CountDownLatch countDown, List<Throwable> fails) {
		super(id, sp, databank, rcSliceQuery, offset, rcQuery, rcEncodedQuery, rcRetrievedAreas, statistics, countDown, fails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (complement inverted) ");
		return sb.toString();
	}
}