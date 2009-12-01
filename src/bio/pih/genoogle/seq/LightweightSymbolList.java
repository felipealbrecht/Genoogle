/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.io.Serializable;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

/**
 * A symbol list hat consumes less memory and is faster to build.
 * 
 * @author albrecht
 */
public class LightweightSymbolList extends AbstractSymbolList implements Serializable {
	private static final long serialVersionUID = -3125317520644706924L;

	private Alphabet alphabet;
	private Symbol[] symbols;
	private String seqString;	
			
	private LightweightSymbolList() {
	}

	/**
	 * Construct a new {@link LightweightSymbolList} from a seqString
	 * @param alphabet
	 * @param seqString
	 * @return {@link LightweightSymbolList} of the given seqString.
	 * @throws IllegalSymbolException
	 */
	public static LightweightSymbolList constructLightweightSymbolList(Alphabet alphabet, String seqString) throws IllegalSymbolException {
		return constructLightweightSymbolList(alphabet, seqString, true);
	}
	/**
	 * @param alphabet
	 * @param seqString
	 * @param cacheResult 
	 * @return {@link LightweightSymbolList} related with the given seqString.
	 * @throws IllegalSymbolException
	 */
	public static LightweightSymbolList constructLightweightSymbolList(Alphabet alphabet, String seqString, boolean cacheResult) throws IllegalSymbolException {
		Symbol[] symbols = new Symbol[seqString.length()];
		
		SymbolTokenization tokenization = null;
		try {
			tokenization = alphabet.getTokenization("token");
		} catch (BioException e) {
			e.printStackTrace();
			return null;
		}
		for(int i = 0; i < seqString.length(); i++) {
			symbols[i] = tokenization.parseTokenChar(seqString.charAt(i));
		}
						
		LightweightSymbolList lwsl = new LightweightSymbolList();					
		lwsl.symbols = symbols; 
		lwsl.alphabet = alphabet;
		lwsl.seqString = seqString;
		
		return lwsl;
	}
	
	@Override
	public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
		String substring = this.seqString().substring(start - 1, end);
		try {
			return constructLightweightSymbolList(this.getAlphabet(), substring);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Alphabet getAlphabet() {
		return alphabet;
	}
		
	@Override
	public int length() {
		return symbols.length;
	}
	
	@Override
	public String seqString() {
		return seqString;
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
	public Symbol symbolAt(int pos) throws IndexOutOfBoundsException {
		return symbols[pos - 1];
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
		Alphabet alphabet = AlphabetManager.alphabetForName("DNA");
		return constructLightweightSymbolList(alphabet, dna, false);
	}
}
