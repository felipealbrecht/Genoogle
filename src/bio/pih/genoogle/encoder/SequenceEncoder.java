/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.genoogle.index.ValueOutOfBoundsException;

/**
 * Class with the main informations of the encoding sequences.
 * 
 * @author albrecht
 */
public abstract class SequenceEncoder {
	private static int POSITION_LENGTH = 0;
	private static int POSITION_BEGIN_BITS_VECTOR = 1;

	private static int maximumAlphabetBitsSize = 8;

	protected final FiniteAlphabet alphabet;
	protected final int subSequenceLength;
	protected final int bitsByAlphabetSize;
	protected final int bitsMask;

	/**
	 * @param alphabet
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public SequenceEncoder(FiniteAlphabet alphabet, int subSequenceLength) throws ValueOutOfBoundsException {
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.bitsByAlphabetSize = bitsByAlphabetSize(alphabet.size());
		this.bitsMask = ((1 << bitsByAlphabetSize) - 1);
	}

	/**
	 * @param alphabetSize
	 *            must be equal or higher than 1 and equals or lower than 256
	 * @return how many bits is necessary to store each character of the given alphabet size.
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

}
