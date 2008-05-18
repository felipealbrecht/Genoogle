package bio.pih.seq;

import java.io.Serializable;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import com.google.common.collect.Maps;

/**
 * @author albrecht
 *
 */
public class LightweightSymbolList extends AbstractSymbolList implements Serializable {
	private static final long serialVersionUID = -3125317520644706924L;

	private static final Map<Alphabet, Map<String, LightweightSymbolList>> CACHE = Maps.newHashMap();

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
		
		LightweightSymbolList lwsl = getFromCache(alphabet, seqString);		
		if (lwsl != null) {
			return lwsl;
		}

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
						
		lwsl = new LightweightSymbolList();					
		lwsl.symbols = symbols; 
		lwsl.alphabet = alphabet;
		lwsl.seqString = seqString;
		
		assert lwsl.symbols.length == lwsl.seqString.length();
		
		if (cacheResult) {
			updateCache(lwsl);
		}

		return lwsl;
	}
	
	@Override
	public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
		String substring = this.getString().substring(start - 1, end);
		try {
			return constructLightweightSymbolList(this.getAlphabet(), substring);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Alphabet getAlphabet() {
		return alphabet;
	}
		
	public int length() {
		return symbols.length;
	}

	/**
	 * @return the internal string that represents this {@link SymbolList} 
	 * 
	 */
	public String getString() {
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
	

	public Symbol symbolAt(int pos) throws IndexOutOfBoundsException {
		try {
			return symbols[pos - 1];
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Index must be within [1.." + length() + "] : " + pos);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (");
		sb.append(this.seqString);
		sb.append(")");

		return sb.toString();
	}

	private static void updateCache(LightweightSymbolList symbolList) {
		Map<String, LightweightSymbolList> c = CACHE.get(symbolList.getAlphabet());
		if (c == null) {
			c = Maps.newHashMap();
			CACHE.put(symbolList.getAlphabet(), c);
		}
		c.put(symbolList.getString(), symbolList);
	}

	private static LightweightSymbolList getFromCache(Alphabet alphabet, String seqString) {
		Map<String, LightweightSymbolList> c = CACHE.get(alphabet);
		if (c == null) {
			return null;
		}
		return c.get(seqString);
	}

	/**
	 * @param dna
	 * @return SymbolList of the given dna sequence string. 
	 * @throws IllegalSymbolException
	 * 
	 */
	public static SymbolList createDNA(String dna) throws IllegalSymbolException {
		Alphabet alphabet = AlphabetManager.alphabetForName("DNA");
		return constructLightweightSymbolList(alphabet, dna, false);
	}
	//TODO: To create static methods for Protein and RNA sequencess
}
