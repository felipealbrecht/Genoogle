package bio.pih.search;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.seq.LightweightSymbolList;

public class DNAReverseSearcher extends DNASearcher {

	private static final Logger logger = Logger.getLogger(DNAReverseSearcher.class.getName());

	public DNAReverseSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank) {
		super(id, sp, databank);
	}

	@Override
	public String toString() {
		if (thisToString == null) {
			StringBuilder sb = new StringBuilder(Integer.toString(this.hashCode()));
			sb.append("(inverted) -");
			sb.append(databank.toString());
			thisToString = sb.toString();
		}
		return thisToString;
	}
	
	@Override
	protected void addHit(Hit hit, ExtendSequences extensionResult,
			GenoogleSmithWaterman smithWaterman, double normalizedScore, double evalue, 
			int queryLength, int targetLength) {

		hit.addHSP(new HSP(smithWaterman,
				getQueryStart(extensionResult, smithWaterman),
				getQueryEnd(extensionResult, smithWaterman),
				getTargetStart(extensionResult, smithWaterman, targetLength),
				getTargetEnd(extensionResult, smithWaterman, targetLength),
				normalizedScore, evalue));
	}

	private int getQueryStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart();
	}
	
	private int getQueryEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd();
	}
		
	private int getTargetStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman, int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}
	
	private int getTargetEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman, int targetLength) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}

	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {
		return LightweightSymbolList.createDNA(Utils.invert(sp.getQuery().seqString()));
	}
}
