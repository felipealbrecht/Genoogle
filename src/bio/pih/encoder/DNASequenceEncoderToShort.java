package bio.pih.encoder;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 *
 */
public class DNASequenceEncoderToShort extends DNASequenceEncoder {
	
	
	private static DNASequenceEncoderToShort defaultEncoder = null;
	
	/**
	 * @return singleton of the {@link DNASequenceEncoderToShort} 
	 */
	public static DNASequenceEncoderToShort getDefaultEncoder() {
		if (defaultEncoder == null) {
			try {
				defaultEncoder = new DNASequenceEncoderToShort(8);
			} catch (ValueOutOfBoundsException e) {
				System.out.println("Problem creating the default instance for DNASequenceEncoderToShort. Please check the stackstrace above.");
				e.printStackTrace();
			}
		}
		return defaultEncoder;
	}
	
	/**
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public DNASequenceEncoderToShort(int subSequenceLength) throws ValueOutOfBoundsException {
		super(subSequenceLength);
	}

	/**
	 * Encode a subsequence to its short representation
	 * 
	 * @param symbolList
	 * @return an short containing the representation of the subsequence
	 */
	public short encodeSubSymbolListToShort(SymbolList symbolList) {
		assert (symbolList.length() <= subSequenceLength);
		short encoded = 0;
		for (int i = 1; i <= symbolList.length(); i++) {
			encoded |= (getBitsFromSymbol(symbolList.symbolAt(i)) << ((subSequenceLength - i) * bitsByAlphabetSize));
		}

		return encoded;
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
	 * @return
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public SymbolList decodeShortToSymbolList(short encoded) throws IllegalSymbolException, BioException {
		String sequenceString = decodeShortToString(encoded);
		return LightweightSymbolList.constructLightweightSymbolList(alphabet, alphabet.getTokenization("token"), sequenceString);
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
		sequenceEncoded[getPositionLength()] = (short) sequence.length();

		if (sequence.length() < subSequenceLength) {
			sequenceEncoded[getPositionBeginBitsVector()] = encodeSubSymbolListToShort(sequence.subList(1, sequence.length()));
		} else {
			int pos = getPositionBeginBitsVector();			
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getNotOverlappedFactory().newSymbolListWindowIterator(sequence, this.subSequenceLength);
			while (symbolListWindowIterator.hasNext()) {
				SymbolList next = symbolListWindowIterator.next();
				sequenceEncoded[pos] = encodeSubSymbolListToShort(next);
				pos++;
			}
			if (pos < size) {
				int from = sequence.length() - extra +1;
				sequenceEncoded[pos] = encodeSubSymbolListToShort(sequence.subList(from, sequence.length()));
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
	 * @return the Sequence in String form encoded in encodedSequence
	 * TODO: Do this method accepting an ShortBuffer as input
	 */
	public String decodeShortArrayToString(short[] encodedSequence) {
		StringBuilder sequence = new StringBuilder(encodedSequence[getPositionLength()]);
		int extra = encodedSequence[getPositionLength()] % subSequenceLength;
		
		if (extra == 0) {					
			for(int i = getPositionBeginBitsVector(); i < encodedSequence.length; i++) {
				sequence.append(decodeShortToString(encodedSequence[i]));
			}
			return sequence.toString();

		} else {
			int i;
			for(i = getPositionBeginBitsVector(); i < encodedSequence.length-1; i++) {
				sequence.append(decodeShortToString(encodedSequence[i]));
			}				
			sequence.append(decodeShortToString(encodedSequence[i], extra));
			return sequence.toString();
		}			
	}

	@Override
	public int getLengthInBytes(int sequenceLength) {
		int total = sequenceLength / subSequenceLength;
		int extra = sequenceLength % subSequenceLength;
		if (extra != 0) { // extra space for the incomplete sub-sequence
			total++;
		}
		
		total++; // extra space for length information
		
		total *= 2; // convert short to byte
						
		return total;
	}	
}
