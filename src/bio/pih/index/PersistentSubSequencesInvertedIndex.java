package bio.pih.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.SequenceDataBank;

/**
 * An inverted index witch the inverted data is stored into disk.
 * 
 * @author albrecht
 */
public class PersistentSubSequencesInvertedIndex extends AbstractSubSequencesInvertedIndex {

	private static final int TOTAL_SUB_SEQUENCES = (int) Math.pow(4, 8);
	private String indexDataFileName = null;;
	private String indexIndexFileName = null;
	private File indexDataFile = null;
	private File indexIndexFile = null;
	
	long[] indexOffsets = null;
	int[] indexQtd = null;
	
	RandomAccessFile indexDataRAF = null;
	
	private MemorySubSequencesInvertedIndex temporaryIndex = null;
	
	/**
	 * @param databank 
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public PersistentSubSequencesInvertedIndex(SequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);		 		
		indexDataFileName = databank.getFullPath()+".index.dat";
		indexIndexFileName = databank.getFullPath()+".index.idx";
	}

	
	private File getIndexDataFile() {
		if (indexDataFile == null) {
			indexDataFile = new File(indexDataFileName); 
		}
		return indexDataFile;
	}
	
	private File getIndexIndexFile() {
		if (indexIndexFile == null) {
			indexIndexFile = new File(indexIndexFileName); 
		}
		return indexIndexFile;
	}

	@Override
	public void load() throws IOException {
		this.indexOffsets = new long[TOTAL_SUB_SEQUENCES];
		this.indexQtd = new int[TOTAL_SUB_SEQUENCES];
		
		MappedByteBuffer mappedIndexIndexFile = new FileInputStream(getIndexIndexFile()).getChannel().map(MapMode.READ_ONLY, 0, getIndexIndexFile().length());

		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			assert mappedIndexIndexFile.getInt() == i;
			indexQtd[i] = mappedIndexIndexFile.getInt();
			indexOffsets[i] = mappedIndexIndexFile.getLong();
		}
		loaded = true;
	}

	@Override
	public void check() throws IOException {
		RandomAccessFile indexRAF = new RandomAccessFile(getIndexIndexFile(), "r");
		RandomAccessFile dataRAF = new RandomAccessFile(getIndexDataFile(), "r");

		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			int[] bucket = temporaryIndex.getMatchingSubSequence((short)i);

			assert indexRAF.readInt() == i;
			assert indexRAF.readInt() == bucket.length;
			assert indexRAF.readLong() == dataRAF.getFilePointer();

			assert dataRAF.readInt() == i;
			assert dataRAF.readInt() == bucket.length;
			for (int pos = 0; pos < bucket.length; pos++) {
				assert bucket[pos] == dataRAF.readInt();
			}
		}
	}

	@Override
	public void write() throws IOException {
		File indexFile = new File(indexIndexFileName);
		File dataFile = new File(indexDataFileName);

		FileChannel indexChannel = new FileOutputStream(indexFile).getChannel();
		FileChannel dataChannel = new FileOutputStream(dataFile).getChannel();
		
		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			int[] bucket = temporaryIndex.getMatchingSubSequence((short)i);

			ByteBuffer buffer = ByteBuffer.allocate(16);
			buffer.putInt(i);
			buffer.putInt(bucket.length);
			buffer.putLong(dataChannel.position());
			buffer.flip();
			indexChannel.write(buffer);

			buffer = ByteBuffer.allocate(8 + (bucket.length * 4));
			buffer.putInt(i);
			buffer.putInt(bucket.length);
			for (int j = 0; j < bucket.length; j++) {
				buffer.putInt(bucket[j]);
			}
			buffer.flip();
			dataChannel.write(buffer);
		}
		indexChannel.close();
		dataChannel.close();
	}
	
	
	private boolean existsChecked = false;
	private boolean exists = false;
	private FileChannel indexDataFileChannel; 
	
	@Override
	public boolean exists() {
		if (existsChecked == false) {			
			if (getIndexDataFile().exists() && getIndexIndexFile().exists()) {
				exists = true;
			} else {
				exists = false;
			}
			existsChecked = true;
		}
		return exists;
	}
	
	@Override
	public void constructIndex() throws ValueOutOfBoundsException {
		temporaryIndex = new MemorySubSequencesInvertedIndex(this.databank, this.subSequenceLength);
		temporaryIndex.constructIndex();
	}

	@Override
	public void addSequence(int sequenceId, SymbolList sequence) {
		if (temporaryIndex != null) {
			temporaryIndex.addSequence(sequenceId, sequence);			
		}
		
	}

	@Override
	public void addSequence(int sequenceId, short[] encodedSequence) {
		if (temporaryIndex != null) {
			temporaryIndex.addSequence(sequenceId, encodedSequence);			
		}		
	}

	@Override
	public void finishConstruction() throws IOException {		
		this.write();
		this.temporaryIndex = null;
		this.load();
	}

	@Override
	public int[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException, IOException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length() + ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
		}
		short encodedSubSequence = encoder.encodeSubSymbolListToShort(subSequence);
		
		return getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public int[] getMatchingSubSequence(short encodedSubSequence) throws IOException {
		int encodedSubSequenceInt = encodedSubSequence & 0xFFFF;
		int quantity = indexQtd[encodedSubSequenceInt];
		long offset = indexOffsets[encodedSubSequenceInt];
		
		int resultsInByte = quantity * 4;
		MappedByteBuffer map = getDataFileChannel().map(MapMode.READ_ONLY, offset, 4 + 4 + resultsInByte);

		IntBuffer buffer = map.asIntBuffer();
		assert buffer.get() == encodedSubSequenceInt;
		assert buffer.get() == quantity;
		
		int bucket[] = new int[quantity];
		buffer.get(bucket);
		return bucket;	
	}

	private FileChannel getDataFileChannel() throws FileNotFoundException {
		if (indexDataFileChannel == null) {
			indexDataFileChannel = new FileInputStream(getIndexDataFile()).getChannel();
		}
		return indexDataFileChannel;
	}
	

	@Override
	public String indexStatus() {
		return "nao sei >:-(";
	}

}
