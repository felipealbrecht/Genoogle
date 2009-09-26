package bio.pih.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.proto.Io.InvertedIndexBuck;
import bio.pih.io.proto.Io.InvertedIndexBuckPosition;
import bio.pih.io.proto.Io.InvertedIndexFilePositions;
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
		this.loaded = true;
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
		int encodedSubSequence = encoder.encodeSubSequenceToInteger(subSequence);
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
	public void saveToFile() throws IOException {
		deleteOldFiles();
		FileChannel memoryInvertedIndexFileChannel = new FileOutputStream(getMemoryInvertedIndexFileName(), true).getChannel();
		
		InvertedIndexFilePositions.Builder invertedIndexFilePositionsBuilder = InvertedIndexFilePositions.newBuilder();
		invertedIndexFilePositionsBuilder.setSize(indexSize);
		
		
		for (int i = 0; i < indexSize; i++) {
			long offset = memoryInvertedIndexFileChannel.position();
			if (offset > Integer.MAX_VALUE) {
				throw new IOException("The offset position is too big.");
			}
			
			InvertedIndexBuck.Builder buckBuilder = InvertedIndexBuck.newBuilder();
			for (int j = 0; j < index[i].length; j++) {
				buckBuilder.addBuck(index[i][j]);
			}
			
			InvertedIndexBuck buck = buckBuilder.build();
			byte[] buckArray = buck.toByteArray();
			memoryInvertedIndexFileChannel.write(ByteBuffer.wrap(buckArray));
			
			InvertedIndexBuckPosition.Builder buckPositionBuilder = InvertedIndexBuckPosition.newBuilder();
			buckPositionBuilder.setBuck(i).setOffset((int) offset).setLength(buckArray.length);
			InvertedIndexBuckPosition buckPosition = buckPositionBuilder.build();
			invertedIndexFilePositionsBuilder.addPosition(buckPosition);			
		}
		memoryInvertedIndexFileChannel.close();
		InvertedIndexFilePositions indexFilePositions = invertedIndexFilePositionsBuilder.build();
		
		FileChannel indexFilePositionsFileChannel = new FileOutputStream(getMemoryInvertedOffsetIndexFileName(), true).getChannel();
		indexFilePositionsFileChannel.write(ByteBuffer.wrap(indexFilePositions.toByteArray()));
		indexFilePositionsFileChannel.close();
	}
	
	@Override
	public void loadFromFile() throws IOException { 
		this.index = new long[indexSize][];
		
		File memoryInvertedIndexFile = new File(getMemoryInvertedIndexFileName());
		FileChannel memoryInvertedIndexFileChannel = new FileInputStream(memoryInvertedIndexFile).getChannel();
		MappedByteBuffer map = memoryInvertedIndexFileChannel.map(MapMode.READ_ONLY, 0, memoryInvertedIndexFile.length());
				
		InvertedIndexFilePositions indexFilePositions =  InvertedIndexFilePositions.parseFrom(new FileInputStream(getMemoryInvertedOffsetIndexFileName()));
		for (int i = 0; i < indexFilePositions.getPositionCount(); i++) {
			InvertedIndexBuckPosition inveredIndexBuckPosition = indexFilePositions.getPosition(i);
			int buck = inveredIndexBuckPosition.getBuck();
			int offset = inveredIndexBuckPosition.getOffset();
			int length = inveredIndexBuckPosition.getLength();
			
			byte[] data = new byte[length];
			map.position(offset);
			map.get(data);
			
			InvertedIndexBuck invertedIndexBuck = InvertedIndexBuck.parseFrom(data);
			
			long[] entries = new long[invertedIndexBuck.getBuckCount()];
			for (int j = 0; j < invertedIndexBuck.getBuckCount(); j++) {
				entries[j] = invertedIndexBuck.getBuck(j);
			}
			index[buck] = entries;						
		}
		
		memoryInvertedIndexFileChannel.close();
		this.loaded = true;	
	}

	private void deleteOldFiles() {
		if (memoryInvertedIndexFileExisits()) {
			new File(getMemoryInvertedIndexFileName()).delete();
		}
		
		if (memoryInvertedOffsetIndexFileExisits()) {
			new File(getMemoryInvertedOffsetIndexFileName()).delete();
		}		
	}
	
	@Override
	public void checkFile() throws IOException { }
	
	
	private String getMemoryInvertedIndexFileName() {		
		return getName() + ".midx";
	}
	
	private String getMemoryInvertedOffsetIndexFileName() {		
		return getName() + ".oidx";
	}
	
	@Override
	public boolean fileExists() {
		return memoryInvertedIndexFileExisits() && memoryInvertedOffsetIndexFileExisits();
	}

	private boolean memoryInvertedIndexFileExisits() {
		return new File(getMemoryInvertedIndexFileName()).exists();
	}
	
	private boolean memoryInvertedOffsetIndexFileExisits() {
		
		return new File(getMemoryInvertedOffsetIndexFileName()).exists();
	}
}
