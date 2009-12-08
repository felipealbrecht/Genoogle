package bio.pih.genoogle.seq;

import java.io.Serializable;

public interface SymbolList extends Serializable {

	Alphabet getAlphabet();
	
	int getLength();
	
	char symbolAt(int pos);

	String seqString();
}
