/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIterator;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

/**
 * Class with the main informations of the encoding sequences.
 * 
 * @author albrecht
 */
public abstract class SequenceEncoder {
	private static int POSITION_LENGTH = 0;
	private static int POSITION_BEGIN_BITS_VECTOR = 1;

	private static int maximumAlphabetBitsSize = 8;

	protected final Alphabet alphabet;
	protected final int subSequenceLength;
	protected final int bitsByAlphabetSize;
	protected final int bitsMask;

	/**
	 * @param alphabet
	 * @param subSequenceLength
	 */
	public SequenceEncoder(Alphabet alphabet, int subSequenceLength) throws ValueOutOfBoundsException {
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.bitsByAlphabetSize = bitsByAlphabetSize(alphabet.getSize());
		this.bitsMask = ((1 << bitsByAlphabetSize) - 1);
	}

	/**
	 * @param alphabetSize
	 *            must be equal or higher than 1 and equals or lower than 256
	 * @return how many bits is necessary to store each character of the given alphabet size.
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
		// the "one" that was used above should be added.
		return bits + 1;
	}

	/**
	 * @return Position in integer vector that is the information of the {@link SymbolList} length
	 */
	public final static int getPositionLength() {
		return POSITION_LENGTH;
	}

	/**
	 * @return Position that the bit vector itself begin
	 */
	public final static int getPositionBeginBitsVector() {
		return POSITION_BEGIN_BITS_VECTOR;
	}

	/**
	 * @param encodedSequence
	 * @return length in bases of the encoded sequence.
	 */
	public final static int getSequenceLength(int[] encodedSequence) {
		return encodedSequence[POSITION_LENGTH];
	}

	/**
	 * @return the length of the subsequences.
	 */
	public int getSubSequenceLength() {
		return subSequenceLength;
	}
	
	public Alphabet getAlphabet() {
		return alphabet;
	}
		
	public abstract int getBitsFromChar(char symbol);
	
	public abstract char getSymbolFromBits(int bits);
	
	/**
	 * Encode a subsequence of the encoder length to its int representation
	 * 
	 * @param subSymbolList
	 * @return an int containing the representation of the subsequence
	 */
	public int encodeSubSequenceToInteger(SymbolList subSymbolList) {
		if (subSymbolList.getLength() > subSequenceLength) {
			throw new ValueOutOfBoundsException(subSymbolList + " is bigger than subSequenceLength("
					+ subSequenceLength + ")");
		}

		int encoded = 0;

		for (int i = 1; i <= subSymbolList.getLength(); i++) {
			encoded |= (getBitsFromChar(subSymbolList.symbolAt(i)) << ((subSequenceLength - i) * bitsByAlphabetSize));
		}

		return encoded;
	}

	public int encodeSubSequenceToInteger(String subSequence) {
		if (subSequence.length() > subSequenceLength) {
			throw new ValueOutOfBoundsException(subSequence + " is bigger than subSequenceLength(" + subSequenceLength
					+ ")");
		}

		int encoded = 0;

		for (int i = 0; i < subSequence.length(); i++) {
			encoded |= (getBitsFromChar(subSequence.charAt(i)) << ((subSequenceLength - (i + 1)) * bitsByAlphabetSize));
		}

		return encoded;
	}

	/**
	 * Decode an int vector to its sequence string representation
	 * 
	 * @param encoded
	 * @return the sequence string
	 */
	public String decodeIntegerToString(int encoded) {
		return decodeIntegerToString(encoded, subSequenceLength);
	}

	/**
	 * @param encoded
	 * @return {@link LightweightSymbolList} of the given encoded sub-sequence.
	 */
	public SymbolList decodeIntegerToSymbolList(int encoded) throws IllegalSymbolException {
		String sequenceString = decodeIntegerToString(encoded, subSequenceLength);
		return new LightweightSymbolList(alphabet, sequenceString);
	}

	private String decodeIntegerToString(int encoded, int length) {
		return decodeIntegerToString(encoded, 0, length - 1);
	}

	/**
	 * TODO: Optimize this function using a constant masks table.
	 */
	private String decodeIntegerToString(int encoded, int begin, int end) {
		StringBuilder sb = new StringBuilder((end - begin) + 1);
		for (int pos = begin; pos <= end; pos++) {
			int posInInt = subSequenceLength - pos;
			int shift = posInInt * bitsByAlphabetSize;
			int value = encoded >> (shift - bitsByAlphabetSize);
			sb.append(getSymbolFromBits(value & bitsMask));
		}
		return sb.toString();
	}

	/**
	 * Encode a {@link SymbolList} of length 1 to (2^16)-1 to an array of int.
	 * 
	 * @param sequence
	 * @return an array of int as bit vector
	 */
	public int[] encodeSymbolListToIntegerArray(SymbolList sequence) {
		assert (sequence.getAlphabet().equals(alphabet));
		int size = sequence.getLength() / subSequenceLength;
		int extra = sequence.getLength() % subSequenceLength;
		if (extra != 0) { // extra space for incomplete sub-sequence
			size++;
		}
		size++; // extra space for information on the length.
		int sequenceEncoded[] = new int[size];
		sequenceEncoded[getPositionLength()] = sequence.getLength();

		if (sequence.getLength() < subSequenceLength) {
			sequenceEncoded[getPositionBeginBitsVector()] = encodeSubSequenceToInteger(sequence);
		} else {
			int pos = getPositionBeginBitsVector();
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getNotOverlappedFactory().newSymbolListWindowIterator(
					sequence, this.subSequenceLength);
			while (symbolListWindowIterator.hasNext()) {
				SymbolList next = symbolListWindowIterator.next();
				sequenceEncoded[pos] = encodeSubSequenceToInteger(next);
				pos++;
			}
			if (pos < size) {
				int from = sequence.getLength() - extra + 1;
				sequenceEncoded[pos] = encodeSubSequenceToInteger(new LightweightSymbolList(sequence, from, sequence.getLength()));
			}
		}

		return sequenceEncoded;
	}

	/**
	 * @param encodedSequence
	 * @return the {@link SymbolList} that is stored in encodedSequence
	 */
	public SymbolList decodeIntegerArrayToSymbolList(int[] encodedSequence) throws IllegalSymbolException {
		String sequenceString = decodeIntegerArrayToString(encodedSequence);
		return new LightweightSymbolList(alphabet, sequenceString);
	}

	/**
	 * @param encodedSequence
	 * @param begin
	 * @param end
	 * @return the sequence in {@link String} form that is stored in encodedSequence
	 */
	public String decodeIntegerArrayToString(int[] encodedSequence, int begin, int end) {

		if ((end - begin) + 1 < subSequenceLength) {
			return decoteIntegerArrayToStringShortenOneSubSequence(encodedSequence, begin, end);
		}
		StringBuilder sequence = new StringBuilder();

		int arrayPos = (begin / subSequenceLength) + 1;
		int posInInt = begin % subSequenceLength;

		if (posInInt != 0) {
			sequence.append(decodeIntegerToString(encodedSequence[arrayPos], posInInt, subSequenceLength - 1));
			arrayPos++;
		}

		int arrayPosLast = end / subSequenceLength;
		for (; arrayPos <= arrayPosLast; arrayPos++) {
			sequence.append(decodeIntegerToString(encodedSequence[arrayPos], subSequenceLength));
		}

		int posInIntLast = end % subSequenceLength;
		if (posInIntLast > 0) {
			sequence.append(decodeIntegerToString(encodedSequence[arrayPos], 0, posInIntLast));
		}

		return sequence.toString();
	}

	private String decoteIntegerArrayToStringShortenOneSubSequence(int[] encodedSequence, int begin, int end) {

		int arrayPosBegin = (begin / subSequenceLength) + 1;
		int arrayPosEnd = (end / subSequenceLength) + 1;
		int firstInt = encodedSequence[arrayPosBegin];

		if (arrayPosBegin == arrayPosEnd) {
			return decodeIntegerToString(firstInt, begin, end);
		}

		StringBuilder sequence = new StringBuilder();
		int beginPos = begin % subSequenceLength;
		sequence.append(decodeIntegerToString(firstInt, beginPos, subSequenceLength - 1));
		int endPos = end % subSequenceLength;
		sequence.append(decodeIntegerToString(encodedSequence[arrayPosEnd], 0, endPos));
		return sequence.toString();
	}

	/**
	 * @param encodedSequence
	 * @return the Sequence in String form encoded in encodedSequence.
	 */
	public String decodeIntegerArrayToString(int[] encodedSequence) {
		StringBuilder sequence = new StringBuilder(encodedSequence[getPositionLength()]);
		int extra = encodedSequence[getPositionLength()] % subSequenceLength;

		if (extra == 0) {
			for (int i = getPositionBeginBitsVector(); i < encodedSequence.length; i++) {
				sequence.append(decodeIntegerToString(encodedSequence[i], subSequenceLength));
			}
			return sequence.toString();

		}
		int i;
		for (i = getPositionBeginBitsVector(); i < encodedSequence.length - 1; i++) {
			sequence.append(decodeIntegerToString(encodedSequence[i], subSequenceLength));
		}
		sequence.append(decodeIntegerToString(encodedSequence[i], extra));
		return sequence.toString();
	}

	// TODO: 1o. aplico a mask e depois faco o shift right, nao seria melhor fazer inverso?
	public static int getValueAtPos(int[] encodedSequence, int pos, int subSequenceLength) {
		int posInArray = (pos / subSequenceLength) + 1;
		int posInInt = (subSequenceLength) - (pos % subSequenceLength);
		int vectorValue = encodedSequence[posInArray];
		int shift = posInInt * 2;
		int value = vectorValue >> (shift - 2);
		return value & 3;
	}
}
