package bio.pih.search;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.seq.LightweightSymbolList;

public class DNAComplementInvertedSearcher extends DNAInvertedSearcher {

	public DNAComplementInvertedSearcher(long id, SearchParams sp,
			IndexedDNASequenceDataBank databank) {
		super(id, sp, databank);
	}
	
	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {
		String inverted = Utils.invert(sp.getQuery().seqString());
		String complement = Utils.sequenceComplement(inverted);				
		return LightweightSymbolList.createDNA(complement);
	}
	
	@Override
	public String toString() {
		if (thisToString == null) {
			StringBuilder sb = new StringBuilder(Integer.toString(this.hashCode()));
			sb.append("(complement inverted) -");
			sb.append(databank.toString());
			thisToString = sb.toString();
		}
		return thisToString;
	}
}