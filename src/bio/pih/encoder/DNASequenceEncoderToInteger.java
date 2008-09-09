package bio.pih.encoder;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 */
public class DNASequenceEncoderToInteger extends DNASequenceEncoder {

	static Logger logger = Logger.getLogger("bio.pih.encoder.DNASequenceEncoderToInteger");

	private static DNASequenceEncoderToInteger defaultEncoder = null;

	/**
	 * @return singleton of the {@link DNASequenceEncoderToInteger}
	 */
	public static DNASequenceEncoderToInteger getDefaultEncoder() {
		if (defaultEncoder == null) {
			try {
				defaultEncoder = new DNASequenceEncoderToInteger(XMLConfigurationReader.getSubSequenceLength());
			} catch (ValueOutOfBoundsException e) {
				logger.fatal("Problem creating the default instance for DNASequenceEncoderToInteger. Please check the stackstrace above.");
				logger.fatal(e);
				return null;
			}
		}
		return defaultEncoder;
	}

	/**
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public DNASequenceEncoderToInteger(int subSequenceLength) throws ValueOutOfBoundsException {
		super(subSequenceLength);
	}

	/**
	 * Encode a subsequence of the encoder length to its int representation
	 * 
	 * @param subSymbolList
	 * @return an int containing the representation of the subsequence
	 */
	public int encodeSubSymbolListToInteger(SymbolList subSymbolList) {
		assert subSymbolList.length() <= subSequenceLength;

		int encoded = 0;

		for (int i = 1; i <= subSymbolList.length(); i++) {
			encoded |= (getBitsFromSymbol(subSymbolList.symbolAt(i)) << ((subSequenceLength - i) * bitsByAlphabetSize));
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
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public SymbolList decodeIntegerToSymbolList(int encoded) throws IllegalSymbolException,
			BioException {
		String sequenceString = decodeIntegerToString(encoded);
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, sequenceString);
	}

	/**
	 * Decode a int to a {@link SymbolList}
	 * 
	 * @param encoded
	 * @param length
	 * @return a array with the symbols that are represented in that encoded
	 *         value
	 */
	private String decodeIntegerToString(int encoded, int length) {
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
	 * Encode a {@link SymbolList} of length 1 to (2^16)-1 to an array of int.
	 * 
	 * @param sequence
	 * @return an array of int as bit vector
	 */
	public int[] encodeSymbolListToIntegerArray(SymbolList sequence) {
		assert (sequence.getAlphabet().equals(alphabet));
		int size = sequence.length() / subSequenceLength;
		int extra = sequence.length() % subSequenceLength;
		if (extra != 0) { // extra space for incomplete sub-sequence
			size++;
		}
		size++; // extra space for information on the length.
		int sequenceEncoded[] = new int[size];
		sequenceEncoded[getPositionLength()] = sequence.length();

		if (sequence.length() < subSequenceLength) {
			sequenceEncoded[getPositionBeginBitsVector()] = encodeSubSymbolListToInteger(sequence.subList(
					1, sequence.length()));
		} else {
			int pos = getPositionBeginBitsVector();
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getNotOverlappedFactory()
					.newSymbolListWindowIterator(sequence, this.subSequenceLength);
			while (symbolListWindowIterator.hasNext()) {
				SymbolList next = symbolListWindowIterator.next();
				sequenceEncoded[pos] = encodeSubSymbolListToInteger(next);
				pos++;
			}
			if (pos < size) {
				int from = sequence.length() - extra + 1;
				sequenceEncoded[pos] = encodeSubSymbolListToInteger(sequence.subList(from,
						sequence.length()));
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
	public SymbolList decodeIntegerArrayToSymbolList(int[] encodedSequence)
			throws IllegalSymbolException, BioException {
		String sequenceString = decodeIntegerArrayToString(encodedSequence);
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, sequenceString);
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
				sequence.append(decodeIntegerToString(encodedSequence[i]));
			}
			return sequence.toString();

		}
		int i;
		for (i = getPositionBeginBitsVector(); i < encodedSequence.length - 1; i++) {
			sequence.append(decodeIntegerToString(encodedSequence[i]));
		}
		sequence.append(decodeIntegerToString(encodedSequence[i], extra));
		return sequence.toString();
	}
}