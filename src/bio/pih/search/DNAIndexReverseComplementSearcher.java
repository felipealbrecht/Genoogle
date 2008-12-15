package bio.pih.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.IndexRetrievedData.RetrievedArea;
import bio.pih.statistics.Statistics;

public class DNAIndexReverseComplementSearcher extends DNAIndexReverseSearcher {

	public DNAIndexReverseComplementSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, 
			String rcSliceQuery, int offset, SymbolList rcQuery, int[] rcEncodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, CountDownLatch countDown, List<Exception> fails) {
		super(id, sp, databank, rcSliceQuery, offset, rcQuery, rcEncodedQuery, rcRetrievedAreas, statistics, countDown, fails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (complement inverted) ");
		return sb.toString();
	}
}