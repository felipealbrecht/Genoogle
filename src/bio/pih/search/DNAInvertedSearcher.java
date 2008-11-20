package bio.pih.search;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.seq.LightweightSymbolList;

public class DNAInvertedSearcher extends DNASearcher {

	private static final Logger logger = Logger.getLogger(DNAInvertedSearcher.class.getName());

	public DNAInvertedSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank) {
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
	
	protected void addHit(int hspNum, Hit hit, ExtendSequences extensionResult,
			GenoogleSmithWaterman smithWaterman, double normalizedScore, double evalue, 
			int queryLength) {

		hit.addHSP(new HSP(hspNum++, 
				smithWaterman,
				getQueryStart(extensionResult, smithWaterman, queryLength),
				getQueryEnd(extensionResult, smithWaterman, queryLength),
				getTargetStart(extensionResult, smithWaterman),
				getTargetEnd(extensionResult, smithWaterman),
				normalizedScore, evalue));
	}

	private int getQueryStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman, int queryLength) {
		return queryLength - (extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart()) + 1;
	}
	
	private int getQueryEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman, int queryLength) {
		return queryLength - (extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd()) + 1;
	}
	
	private int getTargetStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}
	
	private int getTargetEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}

	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {
		SymbolList invertedQuery;
		invertedQuery = LightweightSymbolList.createDNA(Utils.invert(sp.getQuery().seqString()));
		return invertedQuery;
	}

}
