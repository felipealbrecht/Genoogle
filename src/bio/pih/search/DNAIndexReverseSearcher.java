package bio.pih.search;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.DividedStringGenoogleSmithWaterman;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.IndexRetrievedData.RetrievedArea;
import bio.pih.search.results.HSP;
import bio.pih.statistics.Statistics;

public class DNAIndexReverseSearcher extends DNAIndexSearcher {

	public DNAIndexReverseSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, 
			String sliceQuery, int offset, SymbolList query, int[] encodedQuery, 
			List<RetrievedArea>[] rcRetrievedAreas, Statistics statistics, 
			CountDownLatch countDown, List<Exception> fails) {
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
