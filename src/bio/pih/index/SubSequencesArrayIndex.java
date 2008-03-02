package bio.pih.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.seq.LightweightSymbolList;

/**
 * @author albrecht
 */
public class SubSequencesArrayIndex implements EncodedSubSequencesIndex {

	private IndexBucket index[];
	
	private final int subSequenceLength;
	private final int indexSize;
	private final FiniteAlphabet alphabet;

	// Okay, okay.. in some not so near future will be needed to create a
	// factory and do not use this class directly.
	private final DNASequenceEncoderToShort compressor;

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
		this.index = new IndexBucket[indexSize];
	}

	public void addSequence(int sequenceId, SymbolList sequence) {
		if (sequence == null) {
			throw new NullPointerException("Sequence can not be null");
		}

		short[] encodedSequence = compressor.encodeSymbolListToShortArray(sequence);
		
		addSequence(sequenceId, encodedSequence);
	}
	
	public void addSequence(int sequenceId, short[] encodedSequence) {		
		int length = encodedSequence[SequenceEncoder.getPositionLength()] / subSequenceLength;
		
		for (int pos = SequenceEncoder.getPositionBeginBitsVector(); pos < SequenceEncoder.getPositionBeginBitsVector() + length; pos++) {
			int subSequenceInfoIntRepresention = SubSequenceIndexInfo.getSubSequenceInfoIntRepresention(sequenceId, (pos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceLength);
			addSubSequenceInfoEncoded(encodedSequence[pos], subSequenceInfoIntRepresention);
		}
	}
	
	public void optime() {
		for (IndexBucket bucket: index) {
			if (bucket != null) {
				bucket.compressData();
			}
		}
		// Ok... it's ugly, but the unused index data is *huge* and should be free soon. 
		System.gc();
	}
	
	/**
	 * @param subSymbolList
	 * @param subSequenceInfo
	 */
	private void addSubSequenceInfoEncoded(short subSequenceEncoded, int subSequenceInfoEncoded) {
		int indexPos = subSequenceEncoded & 0xFFFF;
		IndexBucket indexBucket = index[indexPos];
		if (indexBucket == null) {
			indexBucket = new IndexBucket(subSequenceEncoded);
			index[indexPos] = indexBucket;
		}
		indexBucket.addElement(subSequenceInfoEncoded);
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
		IndexBucket bucket = index[encodedSubSequence & 0xFFFF];
		if (bucket != null) {
			return bucket.getElements();
		}
		return null;
	}

	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (IndexBucket bucket : index) {
			if (bucket != null) {
				sb.append(bucket.getValue() & 0xFFFF);
				sb.append("(");
				sb.append(compressor.decodeShortToString(bucket.getValue()));
				sb.append(")");
				sb.append(":\n");
				for (int subSequenceInfoEncoded : bucket.getElements()) {
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

	/**
	 * Each bucket containing the positions of each sub-sequence indexed
	 * 
	 * TODO: remove it and substitute all by an {@link ArrayList}
	 * @author albrecht
	 */
	public class IndexBucket {
		short value;
		List<Integer> tempElements;
		int[] elements;
		
		/**
		 * A bucket in sub sequence array index that stores the data and takes care on compressing 
		 * @param value
		 */
		public IndexBucket(short value) {
			this.elements = null;
			this.value = value;
			this.tempElements = new LinkedList<Integer>();
		}
			
		// TODO: update with new data.
		protected void compressData() {
			elements = new int[tempElements.size()];
			for (int i = 0; i < elements.length; i++) {
				elements[i] = tempElements.get(i);
			}
			tempElements = null; // Yes, it can occours a NullPointer exception at addElement(), but best it than something change the data and I do not know. 
		}

		/**
		 * @param subSequenceInfoEncoded 
		 * @param subSequenceInfo
		 */
		public void addElement(int subSequenceInfoEncoded) {
			this.tempElements.add(subSequenceInfoEncoded);
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
		public int[] getElements() {
			if (tempElements != null) {
				compressData();
			}
			assert tempElements == null;
			assert elements != null;
			
			return elements;
		}
				
	}
		
}
