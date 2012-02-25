/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.io.Serializable;

/**
 * A symbol list hat consumes less memory and is faster to build.
 * 
 * @author albrecht
 */
public class LightweightSymbolList implements SymbolList, Serializable {

	private static final long serialVersionUID = 839990518618066909L;
	
	private Alphabet alphabet;
	private String seqString;	
			
	/**
	 * {@link LightweightSymbolList} constructor having a {@link Alphabet} and a String containing a sequence of the given {@link Alphabet}.
	 * @param alphabet
	 * @param seqString
	 */
	public LightweightSymbolList(Alphabet alphabet, String seqString) throws IllegalSymbolException {
		
		for(int i = 0; i < seqString.length(); i++) {
			char c = seqString.charAt(i);
			if (!alphabet.isValid(c)) {
				throw new IllegalSymbolException(c, i, seqString);
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
		String substring = parent.seqString().substring(start - 1, end);
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
		return seqString.charAt(pos-1);
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
	
	/**
	 * Create a RNA {@link SymbolList} from the given RNA {@link String}.
	 * @param rna
	 * @return SymbolList of the given RNA sequence string. 
	 */
	public static SymbolList createRNA(String rna) throws IllegalSymbolException {
		return new LightweightSymbolList(RNAAlphabet.SINGLETON, rna);
	}
	
	public static SymbolList createProtein(String aas) throws IllegalSymbolException {
		return new LightweightSymbolList(AminoAcidAlphabet.SINGLETON, aas);
	}
	
	public static SymbolList createReducedAA(String aas) throws IllegalSymbolException {
		return new LightweightSymbolList(Reduced_AA_8_Alphabet.SINGLETON, aas);
	}
}
