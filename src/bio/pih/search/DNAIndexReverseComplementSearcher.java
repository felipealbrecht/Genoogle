package bio.pih.search;

import java.util.concurrent.CountDownLatch;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.seq.LightweightSymbolList;

public class DNAIndexReverseComplementSearcher extends DNAIndexReverseSearcher {

	public DNAIndexReverseComplementSearcher(long id, SearchParams sp,
			IndexedDNASequenceDataBank databank, CountDownLatch countDown,
			IndexRetrievedData[] retrievedDatas, int pos) throws BioException {
		super(id, sp, databank, countDown, retrievedDatas, pos);
	}

	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {
		String inverted = Utils.invert(sp.getQuery().seqString());
		String complement = Utils.sequenceComplement(inverted);
		return LightweightSymbolList.createDNA(complement);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (complement inverted) ");
		return sb.toString();
	}
}