package bio.pih.genoogle.seq;

public interface SymbolList {

	Alphabet getAlphabet();
	
	int getLength();
	
	char symbolAt(int pos);

	String seqString();
}
