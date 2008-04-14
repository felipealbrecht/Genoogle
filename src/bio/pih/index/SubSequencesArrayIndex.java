package bio.pih.index;

import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.IntArray;

/**
 * @author albrecht
 */
public class SubSequencesArrayIndex implements EncodedSubSequencesIndex {

	private IntArray index[];

	private final int subSequenceLength;
	private final int indexSize;
	private final FiniteAlphabet alphabet;
	private final int[] EMPTY_ARRAY = new int[0];
	
	// Okay, okay.. in some not so near future will be needed to create a
	// factory and do not use this class directly.
	private final DNASequenceEncoderToShort compressor;
	
	Logger logger = Logger.getLogger("bio.pih.index.SubSequencesArrayIndex");

	/**
	 * @param subSequenceLength
	 * @param alphabet
	 * @throws ValueOutOfBoundsException
	 */
	public SubSequencesArrayIndex(int subSequenceLength, FiniteAlphabet alphabet) throws ValueOutOfBoundsException {
		assert (alphabet != null);
		assert (subSequenceLength > 0);

		this.subSequenceLength = subSequenceLength;
		this.alphabet = alphabet;

		this.compressor = new DNASequenceEncoderToShort(subSequenceLength);

		int indexBitsSize = subSequenceLength * SequenceEncoder.bitsByAlphabetSize(alphabet.size());
		this.indexSize = 1 << indexBitsSize;
		// this.integerType = getClassFromSize(indexSize);
		this.index = new IntArray[indexSize];
	}

	public void addSequence(int sequenceId, SymbolList sequence) {
		if (sequence == null) {
			throw new NullPointerException("Sequence can not be null");
		}

		short[] encodedSequence = compressor.encodeSymbolListToShortArray(sequence);

		addSequence(sequenceId, encodedSequence);
	}

        // todo: passar length para ver se a ultima subsequencia nao é parcial
        // tentar executar com 300 megas
	public void addSequence(int sequenceId, short[] encodedSequence) {
		assert sequenceId <= 65535;
		int length = encodedSequence[SequenceEncoder.getPositionLength()] / subSequenceLength;

		for (int pos = SequenceEncoder.getPositionBeginBitsVector(); pos < SequenceEncoder.getPositionBeginBitsVector() + length; pos++) {			
			addSubSequenceInfoEncoded(encodedSequence[pos], SubSequenceIndexInfo.getSubSequenceInfoIntRepresention(sequenceId, (pos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceLength));
		}
	}

	public void optimize() {
		for (IntArray bucket : index) {
			if (bucket != null) {
				bucket.getArray();
			}
		}
	}

	/**
	 * @param subSymbolList
	 * @param subSequenceInfo
	 */
	private void addSubSequenceInfoEncoded(short subSequenceEncoded, int subSequenceInfoEncoded) {
		int indexPos = subSequenceEncoded & 0xFFFF;
		IntArray indexBucket = index[indexPos];
		if (indexBucket == null) {
			indexBucket = new IntArray(100);
			index[indexPos] = indexBucket;
		}
		indexBucket.add(subSequenceInfoEncoded);
	}

	public int[] getMatchingSubSequence(String subSequenceString) throws IllegalSymbolException, BioException, ValueOutOfBoundsException {
		LightweightSymbolList subSequence = LightweightSymbolList.constructLightweightSymbolList(alphabet, subSequenceString);
		return getMachingSubSequence(subSequence);
	}

	public int[] getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length() + ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
		}
		short encodedSubSequence = compressor.encodeSubSymbolListToShort(subSequence);
		return getMachingSubSequence(encodedSubSequence);
	}

	public int[] getMachingSubSequence(short encodedSubSequence) {
		IntArray bucket = index[encodedSubSequence & 0xFFFF];
		if (bucket != null) {
			return bucket.getArray();
		}
		return EMPTY_ARRAY;
	}

	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (IntArray bucket : index) {
			if (bucket != null) {
				for (long subSequenceInfoEncoded : bucket.getArray()) {
					sb.append("\t");
					sb.append(SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoEncoded));
					sb.append(": ");
					sb.append(SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(subSequenceInfoEncoded));
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
	List<SubSequenceIndexInfo> retrievePosition(SymbolList subSymbolList) {
		return null;
	}

}
