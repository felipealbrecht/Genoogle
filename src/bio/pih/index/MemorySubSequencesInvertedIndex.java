package bio.pih.index;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.SequenceDataBank;
import bio.pih.util.IntArray;

/**
 * An inverted sub-sequences index stored in the memory.
 * Faster than {@link PersistentSubSequencesInvertedIndex}, but requires much more memory.
 *  
 * @author albrecht
 */
public class MemorySubSequencesInvertedIndex extends AbstractSubSequencesInvertedIndex {

	protected IntArray index[] = null;	
	
	Logger logger = Logger.getLogger("bio.pih.index.SubSequencesArrayIndex");

	/**
	 * @param databank 
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public MemorySubSequencesInvertedIndex(SequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);
	}
		
	@Override
	public void constructIndex() {		
		this.index = new IntArray[indexSize];
	}
	
	@Override
	public void addSequence(int sequenceId, SymbolList sequence) {
		if (sequence == null) {
			throw new NullPointerException("Sequence can not be null");
		}

		short[] encodedSequence = encoder.encodeSymbolListToShortArray(sequence);
		addSequence(sequenceId, encodedSequence);
	}

	@Override
	public void addSequence(int sequenceId, short[] encodedSequence) {
		//assert sequenceId <= 65535;
		if (sequenceId > 65535) {
			logger.warn("Sequence id " + sequenceId);
			return;
		}
		int length = encodedSequence[SequenceEncoder.getPositionLength()] / subSequenceLength;

		for (int pos = SequenceEncoder.getPositionBeginBitsVector(); pos < SequenceEncoder.getPositionBeginBitsVector() + length; pos++) {			
			addSubSequenceInfoEncoded(encodedSequence[pos], EncoderSubSequenceIndexInfo.getSubSequenceInfoIntRepresention(sequenceId, (pos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceLength));
		}
	}

	@Override
	public void finishConstruction() throws IOException {
		for (IntArray bucket : index) {
			if (bucket != null) {
				bucket.getArray();
			}
		}
		loaded = true;
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

	@Override
	public int[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length() + ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
		}
		short encodedSubSequence = encoder.encodeSubSymbolListToShort(subSequence);
		return getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public int[] getMatchingSubSequence(short encodedSubSequence) {
		IntArray bucket = index[encodedSubSequence & 0xFFFF];
		if (bucket != null) {
			return bucket.getArray();
		}
		return EMPTY_ARRAY;
	}

	@Override
	public String indexStatus() {
		StringBuilder sb = new StringBuilder();
		for (IntArray bucket : index) {
			if (bucket != null) {
				for (long subSequenceInfoEncoded : bucket.getArray()) {
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
