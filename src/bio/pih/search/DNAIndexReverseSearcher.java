package bio.pih.search;

import java.util.concurrent.CountDownLatch;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.search.results.HSP;
import bio.pih.seq.LightweightSymbolList;

public class DNAIndexReverseSearcher extends DNAIndexSearcher {

	public DNAIndexReverseSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, 
			CountDownLatch countDown, IndexRetrievedData[] retrievedDatas, int resultPos) throws BioException {
		super(id, sp, databank, countDown, retrievedDatas, resultPos);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (inverted) ");
		return sb.toString();
	}

	@Override
	protected HSP createHSP(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman,
			double normalizedScore, double evalue, int queryLength, int targetLength) {
		return new HSP(smithWaterman, getQueryStart(extensionResult, smithWaterman), getQueryEnd(
				extensionResult, smithWaterman), getTargetStart(extensionResult, smithWaterman,
				targetLength), getTargetEnd(extensionResult, smithWaterman, targetLength),
				normalizedScore, evalue);
	}

	private int getQueryStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart();
	}

	private int getQueryEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd();
	}

	private int getTargetStart(ExtendSequences extensionResult,
			GenoogleSmithWaterman smithWaterman, int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}

	private int getTargetEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman,
			int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}

	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {
		return LightweightSymbolList.createDNA(Utils.invert(sp.getQuery().seqString()));
	}
}
