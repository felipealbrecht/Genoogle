/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

/**
 * A symbol list hat consumes less memory and is faster to build.
 * 
 * @author albrecht
 */
public class LightweightSymbolList implements SymbolList {

	private Alphabet alphabet;
	private String seqString;	
			
	/**
	 * {@link LightweightSymbolList} constructor having a {@link Alphabet} and a String containing a sequence of the given {@link Alphabet}.
	 * @param alphabet
	 * @param seqString
	 */
	public LightweightSymbolList(Alphabet alphabet, String seqString) throws IllegalSymbolException {
		
		for(int i = 0; i < seqString.length(); i++) {
			if (!alphabet.isValid(seqString.charAt(i))) {
				throw new IllegalSymbolException(seqString.charAt(i));
			}
		}
											
		this.alphabet = alphabet;
		this.seqString = new String(seqString.toCharArray());
	}
	
	/**
	 * {@link LightweightSymbolList} constructor having a parent {@link SymbolList} and the range that will be used.
	 * @param parent
	 * @param start
	 * @param end
	 */
	public LightweightSymbolList(SymbolList parent, int start, int end) {
		String substring = this.seqString().substring(start - 1, end);
		this.alphabet = parent.getAlphabet();
		this.seqString = substring;
	}

	@Override
	public Alphabet getAlphabet() {
		return alphabet;
	}
		
	@Override
	public int getLength() {
		return seqString.length();
	}
	
	@Override
	public String seqString() {
		return seqString;
	}
	
	@Override
	public char symbolAt(int pos) {
		return seqString.charAt(pos);
	}

	@Override
	public int hashCode() {
		int value = 0;
		for (int i = 0; i < seqString.length();i++) {
			value += (seqString.charAt(i) * (i+1));
		}
		return value;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (");
		sb.append(this.seqString);
		sb.append(")");

		return sb.toString();
	}

	/**
	 * Create a DNA {@link SymbolList} from the given DNA {@link String}.
	 * @param dna
	 * @return SymbolList of the given DNA sequence string. 
	 */
	public static SymbolList createDNA(String dna) throws IllegalSymbolException {
		return new LightweightSymbolList(DNAAlphabet.SINGLETON, dna);
	}
}
