package bio.pih.search;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.seq.LightweightSymbolList;

public class DNAComplementSearcher extends DNASearcher {
	
	public DNAComplementSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank) {
		super(id, sp, databank);
	}

	@Override
	protected SymbolList getQuery() throws IllegalSymbolException {		
		String seqString = sp.getQuery().seqString();
		String complement = Utils.sequenceComplement(seqString);				
		return LightweightSymbolList.createDNA(complement);
	}
	
	@Override
	public String toString() {
		if (thisToString == null) {
			StringBuilder sb = new StringBuilder(Integer.toString(this.hashCode()));
			sb.append("(complement) -");
			sb.append(databank.toString());
			thisToString = sb.toString();
		}
		return thisToString;
	}
	
}
