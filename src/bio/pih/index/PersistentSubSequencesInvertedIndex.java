package bio.pih.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.SequenceDataBank;

/**
 * An inverted index witch the inverted data is stored into disk.
 * 
 * @author albrecht
 * @deprecated Should be converted to use Protocol Buffers
 */
@Deprecated
public class PersistentSubSequencesInvertedIndex extends
		AbstractSubSequencesInvertedIndex {

	private static final int TOTAL_SUB_SEQUENCES = (int) Math.pow(4, 8);
	private String indexDataFileName = null;;
	private String indexIndexFileName = null;
	private File indexDataFile = null;
	private File indexIndexFile = null;

	long[] indexOffsets = null;
	int[] indexQtd = null;

	RandomAccessFile indexDataRAF = null;

	private MemorySubSequencesInvertedIndexInteger temporaryIndex = null;

	/**
	 * @param databank
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public PersistentSubSequencesInvertedIndex(SequenceDataBank databank,
			int subSequenceLength) throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);
		indexDataFileName = databank.getFullPath() + ".index.dat";
		indexIndexFileName = databank.getFullPath() + ".index.idx";
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
	public void loadFromFile() throws IOException {
		this.indexOffsets = new long[TOTAL_SUB_SEQUENCES];
		this.indexQtd = new int[TOTAL_SUB_SEQUENCES];

		MappedByteBuffer mappedIndexIndexFile = new FileInputStream(
				getIndexIndexFile()).getChannel().map(MapMode.READ_ONLY, 0,
				getIndexIndexFile().length());

		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			int idxPos = mappedIndexIndexFile.getInt();
			assert idxPos == i;
			indexQtd[i] = mappedIndexIndexFile.getInt();
			indexOffsets[i] = mappedIndexIndexFile.getLong();
		}
		loaded = true;
	}

	@Override
	public void checkFile() throws IOException {
		RandomAccessFile indexRAF = new RandomAccessFile(getIndexIndexFile(),
				"r");
		RandomAccessFile dataRAF = new RandomAccessFile(getIndexDataFile(), "r");

		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			long[] bucket = temporaryIndex.getMatchingSubSequence((short) i);

			int readI = indexRAF.readInt();
			assert readI == i;
			int length = indexRAF.readInt();
			assert length == bucket.length;
			long filePointerReaded = indexRAF.readLong();
			long filePointer = dataRAF.getFilePointer();
			assert filePointerReaded == filePointer;

			readI = dataRAF.readInt();
			assert readI == i;
			length = dataRAF.readInt();
			assert length == bucket.length;
			for (int pos = 0; pos < bucket.length; pos++) {
				int bucketPos = dataRAF.readInt();
				assert bucketPos == bucket[pos];
			}
		}
	}

	@Override
	public void saveToFile() throws IOException {
		File indexFile = new File(indexIndexFileName);
		File dataFile = new File(indexDataFileName);

		FileChannel indexChannel = new FileOutputStream(indexFile).getChannel();
		FileChannel dataChannel = new FileOutputStream(dataFile).getChannel();

		for (int i = 0; i < TOTAL_SUB_SEQUENCES; i++) {
			long[] bucket = temporaryIndex.getMatchingSubSequence((short) i);

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
				buffer.putLong(bucket[j]);
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
	public boolean fileExists() {
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
		temporaryIndex = new MemorySubSequencesInvertedIndexInteger(this.databank,
				this.subSequenceLength);
		temporaryIndex.constructIndex();
	}

	@Override
	public void addSequence(int sequenceId, int[] encodedSequence, int subSequenceOffSet) {
		if (temporaryIndex != null) {
			temporaryIndex.addSequence(sequenceId, encodedSequence, subSequenceOffSet);
		}
	}

	@Override
	public void finishConstruction() throws IOException {
		this.saveToFile();
		this.temporaryIndex = null;
		this.loadFromFile();
	}

	@Override
	public long[] getMatchingSubSequence(SymbolList subSequence)
			throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException(
					"The length ("
							+ subSequence.length()
							+ ") of the given sequence is different from the sub-sequence ("
							+ subSequenceLength + ")");
		}
		int encodedSubSequence = encoder
				.encodeSubSequenceToInteger(subSequence);

		return getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public long[] getMatchingSubSequence(int encodedSubSequence)
			throws IOException, InvalidHeaderData {
		assert getDataFileChannel().size() > 0;

		int encodedSubSequenceInt = encodedSubSequence & 0xFFFF;
		int quantity = indexQtd[encodedSubSequenceInt];
		long offset = indexOffsets[encodedSubSequenceInt];

		int resultsInByte = quantity * 4;
		MappedByteBuffer map = getDataFileChannel().map(MapMode.READ_ONLY,
				offset, 4 + 4 + resultsInByte);

		LongBuffer buffer = map.asLongBuffer();
		if ( buffer.get() != encodedSubSequenceInt) {
			throw new InvalidHeaderData("encodedSubSequenceInt readen is wrong");
		}
		if (buffer.get() != quantity) {
			throw new InvalidHeaderData("quantity readen is wrong");
		}

		long bucket[] = new long[quantity];
		buffer.get(bucket);
		return bucket;
	}

	private FileChannel getDataFileChannel() throws FileNotFoundException {
		if (indexDataFileChannel == null) {
			indexDataFileChannel = new FileInputStream(getIndexDataFile())
					.getChannel();
		}
		return indexDataFileChannel;
	}

	@Override
	public String indexStatus() {
		return "nao sei >:-(";
	}

}
