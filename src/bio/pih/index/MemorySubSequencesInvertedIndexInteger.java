package bio.pih.index;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.SequenceDataBank;
import bio.pih.util.LongArray;

/**
 * An inverted sub-sequences index stored in the memory.
 * Faster than {@link PersistentSubSequencesInvertedIndex}, but requires much more memory.
 *  
 * @author albrecht
 */
public class MemorySubSequencesInvertedIndexInteger extends AbstractSubSequencesInvertedIndex {

	protected LongArray[] tempIndex = null;
	protected long[][] index = null;
	
	
	Logger logger = Logger.getLogger("bio.pih.index.SubSequencesArrayIndex");

	/**
	 * @param databank 
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public MemorySubSequencesInvertedIndexInteger(SequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);
	}
		
	@Override
	public void constructIndex() {		
		this.tempIndex = new LongArray[indexSize];
	}
	
	@Override
	public void addSequence(int sequenceId, int[] encodedSequence, int subSequenceOffSet) {
		// TODO: Buggy because it gets the incomplete final sub-sequence of the encodedSequence.
		int length = encodedSequence.length;
		for (int arrayPos = SequenceEncoder.getPositionBeginBitsVector(); arrayPos < length; arrayPos++) {
			int sequencePos = (arrayPos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceOffSet;
			long longRepresention = EncoderSubSequenceIndexInfo.getSubSequenceInfoLongRepresention(sequenceId, sequencePos);
			addSubSequenceInfoEncoded(encodedSequence[arrayPos], longRepresention);
		}
	}

	@Override
	public void finishConstruction() throws IOException {
		this.index = new long[indexSize][];
		for (int i = 0; i < indexSize; i++) {
			if (tempIndex[i] != null) {
				this.index[i] = tempIndex[i].getArray();
			} else {
				this.index[i] = EMPTY_ARRAY;
			}
		}
		// GC do your work!
		this.tempIndex = null;
		loaded = true;
	}

	/**
	 * @param subSymbolList
	 * @param subSequenceInfo
	 */
	private void addSubSequenceInfoEncoded(int subSequenceEncoded, long subSequenceInfoEncoded) {
		int indexPos = subSequenceEncoded;
		LongArray indexBucket = tempIndex[indexPos];
		if (indexBucket == null) {
			indexBucket = new LongArray();
			tempIndex[indexPos] = indexBucket;
		}
		indexBucket.add(subSequenceInfoEncoded);
	}

	@Override
	public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length() + ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
		}
		int encodedSubSequence = encoder.encodeSubSymbolListToInteger(subSequence);
		return getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public long[] getMatchingSubSequence(int encodedSubSequence) {
		return index[encodedSubSequence];
	}

	@Override
	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (long[] bucket : index) {
			if (bucket != null) {
				for (long subSequenceInfoEncoded : bucket) {
					sb.append("\t");
					sb.append(EncoderSubSequenceIndexInfo.getSequenceId(subSequenceInfoEncoded));
					sb.append(": ");
					sb.append(EncoderSubSequenceIndexInfo.getStart(subSequenceInfoEncoded));
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	@Override
	public void write() throws IOException { }
	
	@Override
	public void load() throws IOException { }
	
	@Override
	public void check() throws IOException { }
	
	@Override
	// Index in memory should always be reloaded.
	public boolean exists() {
		return false;
	}
}
