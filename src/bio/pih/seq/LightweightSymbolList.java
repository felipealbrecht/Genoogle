package bio.pih.seq;

import java.util.Hashtable;
import java.util.Iterator;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.io.SeqIOAdapter;
import org.biojava.bio.seq.io.StreamParser;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class LightweightSymbolList extends AbstractSymbolList {
	private static final long serialVersionUID = -3125317520644706924L;

	private static final int INCREMENT = 100;

	private static final Hashtable<Alphabet, Hashtable<String, LightweightSymbolList>> CACHE = new Hashtable<Alphabet, Hashtable<String, LightweightSymbolList>>(5);

	private Alphabet alphabet;
	private Symbol[] symbols;
	private String seqString;
	private int length;
	private SymbolTokenization parser;

	private LightweightSymbolList() {
	}

	// profile propose
	private static int cacheCount = 0;
	
	/**
	 * @param alphabet
	 * @param parser
	 * @param seqString
	 * @return
	 * @throws IllegalSymbolException
	 */
	public static LightweightSymbolList constructLightweightSymbolList(Alphabet alphabet, SymbolTokenization parser, String seqString) throws IllegalSymbolException {
		LightweightSymbolList lwsl = getFromCache(alphabet, seqString);
		if (lwsl != null) {
			cacheCount++;
			return lwsl;
		}

		lwsl = new LightweightSymbolList();

		if (parser.getTokenType() == SymbolTokenization.CHARACTER) {
			lwsl.symbols = new Symbol[seqString.length()];
		} else {
			lwsl.symbols = new Symbol[INCREMENT];
		}
		char[] charArray = new char[1024];
		int segLength = seqString.length();
		StreamParser stParser = parser.parseStream(new SSLIOListener(lwsl));
		int charCount = 0;
		int chunkLength;
		while (charCount < segLength) {
			chunkLength = Math.min(charArray.length, segLength - charCount);
			seqString.getChars(charCount, charCount + chunkLength, charArray, 0);
			stParser.characters(charArray, 0, chunkLength);
			charCount += chunkLength;
		}
		stParser.close();

		assert (alphabet.equals(parser.getAlphabet()));

		lwsl.alphabet = parser.getAlphabet();
		lwsl.seqString = seqString;
		lwsl.parser = parser;
		updateCache(lwsl);

		return lwsl;
	}
	
	/**
	 * @return
	 */
	static public int getCacheCount() {
		return cacheCount;
	}

	@Override
	public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
		String substring = this.getString().substring(start - 1, end);
		try {
			return constructLightweightSymbolList(this.getAlphabet(), this.getSymbolTokenization(), substring);
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
		return length;
	}

	/**
	 * @return
	 */
	public String getString() {
		return seqString;
	}

	private SymbolTokenization getSymbolTokenization() {
		return this.parser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;// just for optimality
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof SymbolList)) {
			return false;
		}
		return compare(this, (SymbolList) o);
	}

	private boolean compare(SymbolList sl1, SymbolList sl2) {
		if (sl1.length() != sl2.length()) {
			return false;
		}

		if (sl1.hashCode() != sl2.hashCode()) {
			return false;
		}

		Iterator si1 = sl1.iterator();
		Iterator si2 = sl2.iterator();
		while (si1.hasNext()) {
			if (!(si1.next() == si2.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Symbol symbolAt(int pos) throws IndexOutOfBoundsException {
		try {
			return symbols[pos - 1];
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Index must be within [1.." + length() + "] : " + pos);
		}
	}

	/**
	 * @return
	 */
	public Symbol[] getSymbols() {
		return symbols;
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
		Hashtable<String, LightweightSymbolList> c = CACHE.get(symbolList.getAlphabet());
		if (c == null) {
			c = new Hashtable<String, LightweightSymbolList>();
			CACHE.put(symbolList.getAlphabet(), c);
		}
		c.put(symbolList.getString(), symbolList);
	}

	private static LightweightSymbolList getFromCache(Alphabet alphabet, String seqString) {
		Hashtable<String, LightweightSymbolList> c = CACHE.get(alphabet);
		if (c == null) {
			return null;
		}
		return c.get(seqString);
	}

	/**
	 * @param dna
	 * @return
	 * @throws IllegalSymbolException
	 */
	public static SymbolList createDNA(String dna) throws IllegalSymbolException {
		SymbolTokenization p = null;
		Alphabet alphabet = AlphabetManager.alphabetForName("DNA");

		try {
			p = DNATools.getDNA().getTokenization("token");
		} catch (BioException e) {
			throw new BioError("Something has gone badly wrong with DNA", e);
		}
		return constructLightweightSymbolList(alphabet, p, dna);
	}

	// TODO: To create static methods for Protein and RNA sequencess

	/**
	 * Simple inner class for channeling sequence notifications from a StreamParser.
	 */
	private static class SSLIOListener extends SeqIOAdapter {
		LightweightSymbolList lwsl = null;

		/**
		 * @param lwsl
		 */
		public SSLIOListener(LightweightSymbolList lwsl) {
			this.lwsl = lwsl;
		}

		@Override
		public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) {
			if (this.lwsl.symbols.length < this.lwsl.length + length) {
				Symbol[] dest;
				dest = new Symbol[((int) (1.5 * this.lwsl.length)) + length];
				System.arraycopy(this.lwsl.symbols, 0, dest, 0, this.lwsl.length);
				System.arraycopy(syms, start, dest, this.lwsl.length, length);
				this.lwsl.symbols = dest;
			} else {
				System.arraycopy(syms, start, this.lwsl.symbols, this.lwsl.length, length);
			}

			this.lwsl.length += length;
		}
	}
}
