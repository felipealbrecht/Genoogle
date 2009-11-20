package bio.pih.index;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.AbstractSequenceDataBank;
import bio.pih.io.proto.Io.InvertedIndexBuck;

/**
 * An inverted sub-sequences index stored in the memory. Faster than {@link DiskInvertedIndex}, but
 * requires much more memory.
 * 
 * @author albrecht
 */
public class MemoryInvertedIndex extends AbstractInvertedIndex {

	protected long[][] index = null;

	private static Logger logger = Logger.getLogger(MemoryInvertedIndex.class.getCanonicalName());

	/**
	 * @param databank
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public MemoryInvertedIndex(AbstractSequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);
	}

	@Override
	public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.length() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.length()
					+ ") of the given sequence is different from the sub-sequence (" + subSequenceLength + ")");
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
					sb.append(SubSequenceIndexInfo.getSequenceId(subSequenceInfoEncoded));
					sb.append(": ");
					sb.append(SubSequenceIndexInfo.getStart(subSequenceInfoEncoded));
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}

	@Override
	public void loadFromFile() throws IOException {
		long b = System.currentTimeMillis();
		logger.info("Loading inverted index.");
		this.index = new long[indexSize][];

		File memoryInvertedIndexFile = getMemoryInvertedIndexFile();
		FileChannel memoryInvertedIndexFileChannel = new FileInputStream(memoryInvertedIndexFile).getChannel();
		FileInputStream in = new FileInputStream(getMemoryInvertedOffsetIndexFile());
		DataInputStream fileInputStream = new DataInputStream(new BufferedInputStream(in));

		MappedByteBuffer map = memoryInvertedIndexFileChannel.map(MapMode.READ_ONLY, 0,
				memoryInvertedIndexFile.length());

		while (fileInputStream.available() > 0) {
			IndexFileOffset indexFilePosition = IndexFileOffset.newFrom(fileInputStream);
			
			int subSequence = indexFilePosition.subSequence;
			int offset = indexFilePosition.offset;
			int length = indexFilePosition.length;

			byte[] data = new byte[length];
			map.position(offset);
			map.get(data);

			InvertedIndexBuck invertedIndexBuck = InvertedIndexBuck.parseFrom(data);

			long[] entries = new long[invertedIndexBuck.getBuckCount()];
			for (int j = 0; j < invertedIndexBuck.getBuckCount(); j++) {
				entries[j] = invertedIndexBuck.getBuck(j);
			}

			index[subSequence] = entries;
		}

		for (int i = 0; i < indexSize; i++) {
			if (index[i] == null) {
				index[i] = EMPTY_ARRAY;
			}
		}

		memoryInvertedIndexFileChannel.close();
		this.loaded = true;
		logger.info("Inverted index loaded in " + (System.currentTimeMillis() - b));
	}

	@Override
	public boolean fileExists() {
		return memoryInvertedIndexFileExisits() && memoryInvertedOffsetIndexFileExisits();
	}

	private boolean memoryInvertedIndexFileExisits() {
		return getMemoryInvertedIndexFile().exists();
	}

	private boolean memoryInvertedOffsetIndexFileExisits() {

		return getMemoryInvertedOffsetIndexFile().exists();
	}

	public File getMemoryInvertedIndexFile() {
		return new File(databank.getFullPath() + ".midx");

	}

	public File getMemoryInvertedOffsetIndexFile() {
		return new File(databank.getFullPath() + ".oidx");
	}

	public boolean check() {
		if (getMemoryInvertedIndexFile().exists() && getMemoryInvertedOffsetIndexFile().exists()) {
			return true;
		}
		return false;
	}

	public void delete() {
		if (getMemoryInvertedIndexFile().exists()) {
			boolean delete = getMemoryInvertedIndexFile().delete();
			if (!delete) {
				logger.error(getMemoryInvertedOffsetIndexFile() + " can not be deleted.");
			}
		}
		
		if (getMemoryInvertedOffsetIndexFile().exists()) {
			boolean delete = getMemoryInvertedOffsetIndexFile().delete();
			if (!delete) {
				logger.error(getMemoryInvertedOffsetIndexFile() + " can not be deleted.");
			}
		}		
	}
}
