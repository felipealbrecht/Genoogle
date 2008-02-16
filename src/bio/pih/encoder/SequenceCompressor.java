package bio.pih.compressor;

import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 *
 */
public class SequenceCompressor {
	private static int POSITION_LENGTH = 0;
	private static int POSITION_BEGIN_BITS_VECTOR = 1;

	private static int maximumAlphabetBitsSize = 8;

	protected final FiniteAlphabet alphabet;
	protected final int subSequenceLength;
	protected final int bitsByAlphabetSize;
	
	/**
	 * @param alphabet
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public SequenceCompressor(FiniteAlphabet alphabet, int subSequenceLength) throws ValueOutOfBoundsException {
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.bitsByAlphabetSize = bitsByAlphabetSize(alphabet.size());
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
	 * @return Position in integer vector that is the information of the {@link SymbolList} length
	 */
	public static int getPositionLength() {
		return POSITION_LENGTH;
	}
	
	/**
	 * @return Position that the bit vector itself begin 
	 */
	public static int getPositionBeginBitsVector() {
		return POSITION_BEGIN_BITS_VECTOR;
	}

	/**
	 * 
	 * @param size
	 * @return the integer Class that is need to store the value passed in size
	 *         parameter.
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

}
