package bio.pih.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.compressor.DNASequenceCompressorToShort;
import bio.pih.compressor.SequenceCompressor;
import bio.pih.seq.LightweightSymbolList;

/**
 * @author albrecht
 */
public class SubSequencesArrayIndex {

	private IndexBucket index[];
	private HashMap<String, short[]> nameToSequenceMap;
	private final int subSequenceLength;
	private final FiniteAlphabet alphabet;

	// Okay, okay.. in some not so near future will be needed to create a
	// factory and do not use this class directly.
	private final DNASequenceCompressorToShort compressor;

	/**
	 * @param subSequenceLength
	 * @param alphabet
	 * @param symbolListWindowIteratorFactory
	 * @throws ValueOutOfBoundsException
	 */
	public SubSequencesArrayIndex(int subSequenceLength, FiniteAlphabet alphabet) throws ValueOutOfBoundsException {
		// assert (symbolListWindowIteratorFactory != null);
		assert (alphabet != null);
		assert (subSequenceLength > 0);

		this.subSequenceLength = subSequenceLength;
		this.alphabet = alphabet;

		this.compressor = new DNASequenceCompressorToShort(subSequenceLength);

		int indexSize = subSequenceLength * SequenceCompressor.bitsByAlphabetSize(alphabet.size());
		// this.integerType = getClassFromSize(indexSize);
		this.index = new IndexBucket[1 << indexSize];
		this.nameToSequenceMap = new HashMap<String, short[]>();

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

		short[] encodedSequence = compressor.encodeSymbolListToShortArray(sequence);
		nameToSequenceMap.put(sequence.getName(), encodedSequence);
		int length = encodedSequence[SequenceCompressor.getPositionLength()] / subSequenceLength;

		for (int pos = SequenceCompressor.getPositionBeginBitsVector(); pos < SequenceCompressor.getPositionBeginBitsVector() + length; pos++) {
			SubSequenceInfo subSequenceInfo = new SubSequenceInfo(sequence, encodedSequence[pos], (pos - SequenceCompressor.getPositionBeginBitsVector()) * subSequenceLength);
			addSubSequence(subSequenceInfo);
		}

	}

	/**
	 * @param subSymbolList
	 * @param subSequenceInfo
	 */
	private void addSubSequence(SubSequenceInfo subSequenceInfo) {
		int indexPos = subSequenceInfo.getSubSequence() & 0xFFFF;
		IndexBucket indexBucket = index[indexPos];
		if (indexBucket == null) {
			indexBucket = new IndexBucket(subSequenceInfo.getSubSequence());
			index[indexPos] = indexBucket;
		}
		indexBucket.addElement(subSequenceInfo);
	}

	/**
	 * @param subSequenceString
	 * @return
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	public List<SubSequenceInfo> getMatchingSubSequence(String subSequenceString) throws IllegalSymbolException, BioException, ValueOutOfBoundsException {
		LightweightSymbolList subSequence = LightweightSymbolList.constructLightweightSymbolList(alphabet, alphabet.getTokenization("token"), subSequenceString);
		return getMachingSubSequence(subSequence);
	}

	/**
	 * @param subSequence
	 * @return
	 * @throws ValueOutOfBoundsException
	 */
	public List<SubSequenceInfo> getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length() + ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
		}
		short encodedSubSequence = compressor.encodeSubSymbolListToShort(subSequence);
		return getMachingSubSequence(encodedSubSequence);
	}

	/**
	 * @param encodedSubSequence
	 * @return
	 */
	public List<SubSequenceInfo> getMachingSubSequence(short encodedSubSequence) {
		IndexBucket bucket = index[encodedSubSequence & 0xFFFF];
		if (bucket != null) {
			return bucket.getElements();
		}
		return null;
	}

	/**
	 * @return a string containing the status of the index.
	 */
	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (IndexBucket bucket : index) {
			if (bucket != null) {
				sb.append(bucket.getValue() & 0xFFFF);
				sb.append("(");
				sb.append(compressor.decodeShortToString(bucket.getValue()));
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
	 * Each bucket containing the positions of each sub-sequence indexed
	 * 
	 * TODO: remove it and substitute all by an {@link ArrayList}
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
