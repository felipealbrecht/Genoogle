/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import org.apache.log4j.Logger;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.proto.Io.InvertedIndexBuck;
import bio.pih.genoogle.seq.SymbolList;

/**
 * An inverted sub-sequences index stored in the memory.
 * 
 * @author albrecht
 */
public class MemoryInvertedIndex extends AbstractInvertedIndex {

	protected long[][] index = null;

	private static Logger logger = Logger.getLogger(MemoryInvertedIndex.class.getCanonicalName());

	/**
	 * @param databank
	 * @param subSequenceLength
	 */
	public MemoryInvertedIndex(AbstractSequenceDataBank databank, SequenceEncoder indexedSequenceEncoder) {
		super(databank, indexedSequenceEncoder);
	}

	@Override
	public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException {
		if (subSequence.getLength() != subSequenceLength) {
			throw new ValueOutOfBoundsException("The length (" + subSequence.getLength()
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
		FileInputStream memoryInvertedIndexIS = new FileInputStream(memoryInvertedIndexFile);
				
		FileInputStream in = new FileInputStream(getMemoryInvertedOffsetIndexFile());
		DataInputStream fileInputStream = new DataInputStream(new BufferedInputStream(in));
		
		long mmOffset = 0;
		final long TWO_GB = Integer.MAX_VALUE;
		final long invertedIndexFileLength = memoryInvertedIndexFile.length();
		
                MappedByteBuffer map = null;        
		if (invertedIndexFileLength > TWO_GB) {			
			map = memoryInvertedIndexIS.getChannel().map(MapMode.READ_ONLY, mmOffset, TWO_GB);
		} else {
			map = memoryInvertedIndexIS.getChannel().map(MapMode.READ_ONLY, mmOffset, invertedIndexFileLength);
		}
		
		long totalSubSequences = 0;
		
		while (fileInputStream.available() > 0) {
			IndexFileOffset indexFilePosition = IndexFileOffset.newFrom(fileInputStream);						

			int subSequence = indexFilePosition.subSequence;
			int length = indexFilePosition.length;
			long offset = indexFilePosition.offset;
			//System.out.println(subSequence);
			if (offset + length > mmOffset + TWO_GB) {
				mmOffset += (TWO_GB - length);
                                if (mmOffset + TWO_GB > invertedIndexFileLength) {
                                    long l = invertedIndexFileLength - mmOffset;
                                    System.out.println(l);
				    map = memoryInvertedIndexIS.getChannel().map(MapMode.READ_ONLY, mmOffset, l);
                                } else {
                                    map = memoryInvertedIndexIS.getChannel().map(MapMode.READ_ONLY, mmOffset, TWO_GB);
                                }
			}
										
			long reOffset = offset - mmOffset;
			if (reOffset > Integer.MAX_VALUE) {
				logger.fatal(reOffset + " is too big for " + reOffset);
				System.exit(-2);
			}
			byte[] data = new byte[length];
			map.position((int) reOffset);
			map.get(data);

			InvertedIndexBuck invertedIndexBuck = InvertedIndexBuck.parseFrom(data);

			long[] entries = new long[invertedIndexBuck.getBuckCount()];
			for (int j = 0; j < invertedIndexBuck.getBuckCount(); j++) {
				entries[j] = invertedIndexBuck.getBuck(j);
			}
			totalSubSequences += invertedIndexBuck.getBuckCount();

			index[subSequence] = entries;
		}

		for (int i = 0; i < indexSize; i++) {
			if (index[i] == null) {
				index[i] = EMPTY_ARRAY;
			}
		}
	
		this.loaded = true;
		logger.info(totalSubSequences + " sub sequences was loaded into the inverted index.");
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
