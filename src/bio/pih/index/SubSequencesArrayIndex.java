package bio.pih.index;

import java.util.HashMap;
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
	
	/**
	 * Position in integer vector that is the information of the symbollist length
	 */
	public static int POSITION_LENGTH = 0;
	/**
	 * Position that the bit vector itself begin 
	 */
	public static int POSITION_BEGIN_BITS_VECTOR = 1;

	private static int maximumAlphabetBitsSize = 8;
	private IndexBucket index[];
	private HashMap<String, short[]> nameToSequenceMap;
	private final int subSequenceLength;
	private final int bitsByAlphabetSize;
	private final FiniteAlphabet alphabet;
	private final SymbolListWindowIteratorFactory symbolListWindowIteratorFactory;
	


	// private Class integerType;

	/**
	 * @param subSequenceLength
	 * @param alphabet
	 * @param symbolListWindowIteratorFactory
	 * @throws ValueOutOfBoundsException
	 */
	public SubSequencesArrayIndex(int subSequenceLength, FiniteAlphabet alphabet, SymbolListWindowIteratorFactory symbolListWindowIteratorFactory) throws ValueOutOfBoundsException {
		assert (symbolListWindowIteratorFactory != null);
		assert (alphabet != null);
		assert (subSequenceLength > 0);

		this.subSequenceLength = subSequenceLength;
		this.alphabet = alphabet;
		this.symbolListWindowIteratorFactory = symbolListWindowIteratorFactory;

		bitsByAlphabetSize = bitsByAlphabetSize(alphabet.size());
		int indexSize = subSequenceLength * bitsByAlphabetSize;
		// this.integerType = getClassFromSize(indexSize);
		this.index = new IndexBucket[1 << indexSize];
		this.nameToSequenceMap = new HashMap<String, short[]>();
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
	 * 
	 * @param sequence
	 */
	public void addSequence(Sequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("Sequence can not be null");
		}
		
		short[] encodedSequence = encodeSymbolListToShortArray(sequence);
		nameToSequenceMap.put(sequence.getName(), encodedSequence);
		int length = encodedSequence[POSITION_LENGTH] / subSequenceLength;
				
		for (int pos = POSITION_BEGIN_BITS_VECTOR; pos < POSITION_BEGIN_BITS_VECTOR + length; pos++) {
			SubSequenceInfo subSequenceInfo = new SubSequenceInfo(sequence, encodedSequence[pos], (pos - POSITION_BEGIN_BITS_VECTOR) * subSequenceLength);  
			addSubSequence(subSequenceInfo);	
		}
			
	}
	
	/**
	 * @param subSymbolList
	 * @param subSequenceInfo
	 */
	private void addSubSequence(SubSequenceInfo subSequenceInfo) {
		IndexBucket indexBucket = index[subSequenceInfo.getSubSequence() & 0xFFFF];
		if (indexBucket == null) {
			indexBucket = new IndexBucket(subSequenceInfo.getSubSequence());
			index[subSequenceInfo.getSubSequence() & 0xFFFF] = indexBucket;
		}
		indexBucket.addElement(subSequenceInfo);
	}
	
	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (IndexBucket bucket : index) {
			if (bucket != null) {
				sb.append(bucket.getValue() & 0xFFFF);
				sb.append("(");
				sb.append(decodeShortToString(bucket.getValue()));
				sb.append(")");
				sb.append(":\n");
				for (SubSequenceInfo subSequenceInfo : bucket.getElements()) {
					sb.append("\t");
					sb.append(subSequenceInfo.getSequence().getName());
					sb.append(": ");
					sb.append(subSequenceInfo.getStart());
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @param subSymbolList
	 * @return
	 */
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList) {
		return null;
	}

	/**
	 * @param sequence
	 * @return a vector of short as bit vector
	 */
	public short[] encodeSymbolListToShortArray(SymbolList sequence) {
		assert (sequence.getAlphabet().equals(alphabet));
		int size = sequence.length() / subSequenceLength;
		int extra = sequence.length() % subSequenceLength;
		if (extra != 0) { // extra space for the incomplete sub-sequence
			size++;
		}
		size++; // extra space for the length information.
		short sequenceEncoded[] = new short[size];
		sequenceEncoded[POSITION_LENGTH] = (short) sequence.length();

		if (sequence.length() < subSequenceLength) {
			sequenceEncoded[POSITION_BEGIN_BITS_VECTOR] = encodeSubsequenceToShort(sequence.subList(1, sequence.length()));
		} else {
			int pos = POSITION_BEGIN_BITS_VECTOR;
			SymbolListWindowIterator symbolListWindowIterator = symbolListWindowIteratorFactory.newSymbolListWindowIterator(sequence, this.subSequenceLength);
			while (symbolListWindowIterator.hasNext()) {
				SymbolList next = symbolListWindowIterator.next();
				sequenceEncoded[pos] = encodeSubsequenceToShort(next);
				pos++;
			}
			if (pos < size) {
				int from = sequence.length() - extra +1;
				sequenceEncoded[pos] = encodeSubsequenceToShort(sequence.subList(from, sequence.length()));
			}
		}

		return sequenceEncoded;
	}
	
	/**
	 * @param encodedSequence
	 * @return the {@link SymbolList} that is stored in encodedSequence 
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public SymbolList decodeShortArrayToSymbolList(short [] encodedSequence) throws IllegalSymbolException, BioException {
		String sequenceString = decodeShortArrayToString(encodedSequence);
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, alphabet.getTokenization("token"), sequenceString);		
	}
	
	/**
	 * @param encodedSequence
	 * @return
	 */
	public String decodeShortArrayToString(short[] encodedSequence) {
		StringBuilder sequence = new StringBuilder(encodedSequence[POSITION_LENGTH]);
		int extra = encodedSequence[POSITION_LENGTH] % subSequenceLength;
		
		if (extra == 0) {					
			for(int i = POSITION_BEGIN_BITS_VECTOR; i < encodedSequence.length; i++) {
				sequence.append(decodeShortToString(encodedSequence[i]));
			}
			return sequence.toString();

		} else {
			int i;
			for(i = POSITION_BEGIN_BITS_VECTOR; i < encodedSequence.length-1; i++) {
				sequence.append(decodeShortToString(encodedSequence[i]));
			}				
			sequence.append(decodeShortToString(encodedSequence[i], extra));
			return sequence.toString();
		}			
	}

	/**
	 * Encode a SubSequence into a integer of the type of integerClass TODO:
	 * Remove it from a inner class
	 * 
	 * @author Albrecht
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
	 * 
	 * @param symbolList
	 * @return an int containing the representation of the subsequence
	 */
	public short encodeSubsequenceToShort(SymbolList symbolList) {
		assert (symbolList.length() <= subSequenceLength);
		short encoded = 0;
		for (int i = 1; i <= symbolList.length(); i++) {
			encoded |= (getBitsFromSymbol(symbolList.symbolAt(i)) << ((subSequenceLength - i) * bitsByAlphabetSize));
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
	 * Decode a short vector to its sequence string representation
	 * @param encoded
	 * @return the sequence string
	 */
	public String decodeShortToString(short encoded) {
		return decodeShortToString(encoded, subSequenceLength);
	}
	
	/**
	 * @param encoded
	 * @param length 
	 * @return a array with the symbols that are represented in that encoded
	 *         value
	 */
	private String decodeShortToString(short encoded, int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = subSequenceLength - 1; i >= subSequenceLength - length; i--) {
			int shift = i * bitsByAlphabetSize;
			int mask = ((1 << bitsByAlphabetSize) - 1) << shift;
			int value = encoded & mask;
			byte symbolValue = (byte) (value >> shift);
			sb.append(getSymbolFromBits(symbolValue));
		}

		return sb.toString();
	}

	/**
	 * @param encoded
	 * @return
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public SymbolList decodeShortToSymbolList(short encoded) throws IllegalSymbolException, BioException {
		String sequenceString = decodeShortToString(encoded);
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, alphabet.getTokenization("token"), sequenceString);
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
	 * Each bucket containing the positions of each sub-sequence indexed
	 * 
	 * @author albrecht
	 */
	public class IndexBucket {
		short value;
		List<SubSequenceInfo> elements;

		/**
		 * @param subSequenceInfo
		 */
		public void addElement(SubSequenceInfo subSequenceInfo) {
			this.elements.add(subSequenceInfo);
		}
		
		/**
		 * @param value
		 */
		public IndexBucket(short value) {
			this.value = value;
			this.elements = new LinkedList<SubSequenceInfo>();
		}

		/**
		 * @return the value associate with this bucket
		 */
		public short getValue() {
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
