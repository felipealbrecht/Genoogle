package bio.pih.index;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 * 
 */
public class SubSequencesArrayIndex {

	private static int maximumAlphabetBitsSize = 8;
	private IndexBucket index[];
	private final int subSequenceLength;
	private final int bitsByAlphabetSize;
	private final FiniteAlphabet alphabet;
	private final SymbolListWindowIteratorFactory symbolListWindowIteratorFactory;
//	private Class integerType;

	public SubSequencesArrayIndex(int subSequenceLength, FiniteAlphabet alphabet, SymbolListWindowIteratorFactory symbolListWindowIteratorFactory) throws ValueOutOfBoundsException {
		assert (symbolListWindowIteratorFactory != null);
		assert (alphabet != null);
		assert (subSequenceLength > 0);

		this.subSequenceLength = subSequenceLength;
		this.alphabet = alphabet;
		this.symbolListWindowIteratorFactory = symbolListWindowIteratorFactory;

		bitsByAlphabetSize = bitsByAlphabetSize(alphabet.size());
		int indexSize = subSequenceLength * bitsByAlphabetSize;
//		this.integerType = getClassFromSize(indexSize);
		this.index = new IndexBucket[indexSize];
	}

	/**
	 * @param alphabetSize
	 *            must be equal or higher than 1 and equals or lower than 256
	 * @return
	 * @throws ValueOutOfBoundsException
	 *             is size is between from 1 to 64
	 */
	public static int bitsByAlphabetSize(int alphabetSize) throws ValueOutOfBoundsException {
		if (alphabetSize <= 0) {
			throw new ValueOutOfBoundsException("alphabetSize lower than zero.");
		}
		if (alphabetSize > (1 << maximumAlphabetBitsSize)) {
			throw new ValueOutOfBoundsException("alphabetSize higher than " + (1 << maximumAlphabetBitsSize));
		}

		int maxValue = alphabetSize - 1;
		if (maxValue == 0) {
			return 1;
		}

		int bits = maximumAlphabetBitsSize;
		while ((maxValue & (1 << bits)) == 0) {
			bits--;
		}
		// the "one" that was used above must be added.
		return bits + 1;
	}

	/**
	 * Add a sequence into index
	 * @param sequence
	 */
	public void addSequence(Sequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("Sequence can not be null");
		}

		SymbolListWindowIterator symbolListWindowIterator = symbolListWindowIteratorFactory.newSymbolListWindowIterator(sequence, this.subSequenceLength);

		while (symbolListWindowIterator.hasNext()) {
			SymbolList next = symbolListWindowIterator.next();
			SubSequenceInfo subSequenceInfo = new SubSequenceInfo(sequence, next, symbolListWindowIterator.getActualPos(), this.subSequenceLength);
			addSubSequence(next, subSequenceInfo);
		}
	}

	/**
	 * 
	 * @param size
	 * @return the integer Class that is need to store the value passed in size
	 *         param.
	 * @throws ValueOutOfBoundsException
	 */
	@SuppressWarnings("unchecked")
	public static Class getClassFromSize(int size) throws ValueOutOfBoundsException {
		if (size <= 0) {
			throw new ValueOutOfBoundsException("size lower than zero.");
		}
		if (size > Long.MAX_VALUE) {
			throw new ValueOutOfBoundsException("size higher than " + Long.MAX_VALUE);
		}

		if (size <= 8) {
			return Byte.class;
		} else if (size <= 16) {
			return Short.class;
		} else if (size <= 32) {
			return Integer.class;
		} else if (size <= 64) {
			return Long.class;
		} else {
			throw new ValueOutOfBoundsException("size is higher than a " + Long.MAX_VALUE + "? May be it's a bug.");
		}
	}

	/**
	 * @param subSymbolList
	 * @param info
	 */
	private void addSubSequence(SymbolList subSymbolList, SubSequenceInfo info) {
	}

	/**
	 * @param subSymbolList
	 * @return
	 */
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList) {
		return null;
	}

	/**
	 * Encode a SubSequence into a integer of the type of integerClass TODO:
	 * Remove it from a inner class
	 * 
	 * @author Usuario
	 * 
	 */
	
	static Hashtable<Symbol, Byte> DNASymbolToBitsSubstitionTable;
	static {
		DNASymbolToBitsSubstitionTable = new Hashtable<Symbol, Byte>();
		DNASymbolToBitsSubstitionTable.put(DNATools.a(), (byte) 0x00);
		DNASymbolToBitsSubstitionTable.put(DNATools.c(), (byte) 0x01);
		DNASymbolToBitsSubstitionTable.put(DNATools.g(), (byte) 0x02);
		DNASymbolToBitsSubstitionTable.put(DNATools.t(), (byte) 0x03);
	}

	protected byte getBitsFromSymbol(Symbol symbol) {
		return DNASymbolToBitsSubstitionTable.get(symbol).byteValue();
	}

	/**
	 * Encode a subsequence to your int representation
	 * @param symbolList
	 * @return a int containing the representation of the subsequence
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public int encodeToInt(SymbolList symbolList) throws InstantiationException, IllegalAccessException {
		assert (symbolList.length() == subSequenceLength);
		int encoded = 0;
		for (int i = 1; i <= symbolList.length(); i++) {
			encoded |= (getBitsFromSymbol(symbolList.symbolAt(i)) << ((symbolList.length()-i) * bitsByAlphabetSize));
		}

		return encoded;
	}
	
	static Hashtable<Byte, Character> DNABitsToSymbolSubstitionTable;
	static {
		DNABitsToSymbolSubstitionTable = new Hashtable<Byte, Character>();
		DNABitsToSymbolSubstitionTable.put((byte) 0x00, 'A');
		DNABitsToSymbolSubstitionTable.put((byte) 0x01, 'C');
		DNABitsToSymbolSubstitionTable.put((byte) 0x02, 'G');
		DNABitsToSymbolSubstitionTable.put((byte) 0x03, 'T');
	}
	
	protected char getSymbolFromBits(byte bits) {
		return DNABitsToSymbolSubstitionTable.get(bits);
	}
	
	/**
	 * @param encoded
	 * @return a array with the symbols that are represented in that encoded value
	 */
	public String decodeToString(int encoded) {
		StringBuilder sb = new StringBuilder(subSequenceLength);
		for (int i = subSequenceLength-1; i >= 0; i--) {
			int shift = i * bitsByAlphabetSize;
			int mask = ((1 << bitsByAlphabetSize) -1) << shift;
			int value = encoded & mask;
			byte symbolValue = (byte) (value >> shift);
			sb.append(getSymbolFromBits(symbolValue));
		}
		
		return sb.toString();				
	}
	
	public SymbolList decodeToSymbolList(int encoded) throws IllegalSymbolException, BioException {
		String sequenceString = decodeToString(encoded);		
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, alphabet.getTokenization("token"), sequenceString);
	}

	/**
	 * Each bucket containing the positions of each sub-sequence indexed 
	 * @author albrecht
	 */
	public class IndexBucket {
		int value;
		List<SubSequenceInfo> elements;

		/**
		 * @param value
		 */
		public IndexBucket(int value) {
			this.value = value;
			this.elements = new LinkedList<SubSequenceInfo>();
		}

		/**
		 * @return the value associate with this bucket
		 */
		public int getValue() {
			return value;
		}

		/**
		 * @return the elements in this bucket
		 */
		public List<SubSequenceInfo> getElements() {
			return elements;
		}
	}
}
