/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index.builder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.IndexFileOffset;
import bio.pih.genoogle.index.LowComplexitySubSequences;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.proto.Io.InvertedIndexBuck;

import com.google.common.collect.Lists;

/**
 * Class which uses SortBased method to create the inverted index. Possibly will be merged with the
 * {@link MemoryInvertedIndex}.
 *
 * @author albrecht
 */
public class InvertedIndexBuilder {

	private static final int MEMORY_CHUCK = 256 * 1024 * 1024; // 512 MEGABYTES.

	private static Logger logger = Logger.getLogger("bio.pih.index.builder.InvertedIndexBuilder");

	private static final int MINIMUM_ENTRY_SET = 10;

	private long memoryChuck = MEMORY_CHUCK;
	private long totalMemoryUsedToStoreSubSequences;

	private File entriesTempFilePhase1;
	private DataOutputStream entriesOutputPhase1;
	private DataInputStream entriesInputPhase1;

	private File entriesTempFilePhase2;
	private DataOutputStream entriesOutputStreamPhase2;
	private DataInputStream entriesInputStreamPhase2;

	private long totalEntries = -1;
	private final MemoryInvertedIndex memoryInvertedIndex;
	private final AbstractSequenceDataBank databank;
	private int indexSize;

	private List<SequenceInfo> sequencesToAdd = Lists.newArrayList();
	private long totalMemorytoAdd;
	private long totalSubSequencesToAdd;

	private final int subSequenceOffSet;
	private BitSet lowComplexitySubSequences;

	private long totalFiltered = 0;

	public InvertedIndexBuilder(IndexedSequenceDataBank indexedSequenceDataBank) {
		this.memoryInvertedIndex = indexedSequenceDataBank.getIndex();
		this.subSequenceOffSet = indexedSequenceDataBank.getSubSequencesOffset();
		this.databank = memoryInvertedIndex.getDatabank();
		this.indexSize = memoryInvertedIndex.getIndexSize();
		this.totalMemoryUsedToStoreSubSequences = memoryChuck / 4;

		int lowComplexityFilter = databank.getLowComplexityFilter();
		if (lowComplexityFilter < 0) {
			this.lowComplexitySubSequences = new BitSet();
			logger.info("Low complexity sub sequences filter disabled.");
		} else {
			int[] lowComplexitySubSequencesArray = new LowComplexitySubSequences(databank.getSubSequenceLength(), lowComplexityFilter).getSubSequences();
			logger.info("Low complexity sub sequences filter for " + lowComplexitySubSequencesArray.length
					+ " sub sequences.");

			this.lowComplexitySubSequences = new BitSet(databank.getAlphabet().getSize()
					^ databank.getSubSequenceLength());
			for (int lowComplexSubSequence : lowComplexitySubSequencesArray) {
				this.lowComplexitySubSequences.set(lowComplexSubSequence);
			}
		}
	}

	public InvertedIndexBuilder(IndexedSequenceDataBank indexedSequenceDaaBank, int totalSortMemory)
			throws IndexConstructionException {
		this(indexedSequenceDaaBank);
		this.setTotalSortMemory(totalSortMemory);
	}

	public void constructIndex() throws IndexConstructionException {
		if (entriesOutputPhase1 != null) {
			throw new IndexConstructionException("The index is already being build [1].");
		}

		if (entriesInputPhase1 != null) {
			throw new IndexConstructionException("The index is already being build [2].");
		}

		if (entriesOutputStreamPhase2 != null) {
			throw new IndexConstructionException("The index is already being build [4].");
		}

		if (entriesInputStreamPhase2 != null) {
			throw new IndexConstructionException("The index is already being build [5].");
		}

		if (totalEntries != -1) {
			throw new IndexConstructionException("The index is already being build [7].");
		}

		try {
			resetFilePhase1();
			resetFilePhase2();
		} catch (IOException e) {
			throw new IndexConstructionException(e);
		}

		totalEntries = 0;
	}

	private void resetFilePhase1() throws IOException, IndexConstructionException {
		if (entriesTempFilePhase1 != null && entriesTempFilePhase1.exists()) {
			boolean delete = entriesTempFilePhase1.delete();
			if (!delete) {
				logger.error(entriesTempFilePhase1 + " can not be deleted.");
			}
		}
		entriesTempFilePhase1 = File.createTempFile(databank.getFullPath().getName(), ".sbms_1.tmp");
		entriesTempFilePhase1.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(getEntriesFilePhase1());
		entriesOutputPhase1 = new DataOutputStream(new BufferedOutputStream(fileOutputStream));

		FileInputStream fileInputStream = new FileInputStream(getEntriesFilePhase1());
		entriesInputPhase1 = new DataInputStream(new BufferedInputStream(fileInputStream));
	}

	private void resetFilePhase2() throws IOException, IndexConstructionException {
		if (entriesTempFilePhase2 != null && entriesTempFilePhase2.exists()) {
			boolean delete = entriesTempFilePhase2.delete();
			if (!delete) {
				logger.error(entriesTempFilePhase2 + " can not be deleted.");
			}
		}
		entriesTempFilePhase2 = File.createTempFile(databank.getFullPath().getName(), ".sbms_2.tmp");
		entriesTempFilePhase2.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(getEntriesFilePhase2());
		entriesOutputStreamPhase2 = new DataOutputStream(new BufferedOutputStream(fileOutputStream));
		FileInputStream fileInputStream = new FileInputStream(getEntriesFilePhase2());
		entriesInputStreamPhase2 = new DataInputStream(new BufferedInputStream(fileInputStream));
	}

	private DataOutputStream getEntriesOutpuPhase1() throws IndexConstructionException {
		if (entriesOutputPhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesOutputPhase1;
	}

	private DataInputStream getEntriesInputPhase1() throws IndexConstructionException {
		if (entriesInputPhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesInputPhase1;
	}

	private File getEntriesFilePhase1() throws IndexConstructionException {
		if (entriesTempFilePhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized");
		}
		return entriesTempFilePhase1;
	}

	private DataOutputStream getEntriesOutpuPhase2() throws IndexConstructionException {
		if (entriesOutputStreamPhase2 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesOutputStreamPhase2;
	}

	private File getEntriesFilePhase2() throws IndexConstructionException {
		if (entriesTempFilePhase2 == null) {
			throw new IndexConstructionException("The index structure was not initialized");
		}
		return entriesTempFilePhase2;
	}

	public void setTotalSortMemory(int size) throws IndexConstructionException {
		if (size < MINIMUM_ENTRY_SET * Entry.INSTANCE_SIZE) {
			throw new IndexConstructionException("The sort memory size is too small.");
		}
		this.memoryChuck = size;
	}

	public void addSequence(int sequenceId, int[] encodedSequence) throws IndexConstructionException {

		SequenceInfo sequenceInfo = new SequenceInfo(sequenceId, encodedSequence);
		totalMemorytoAdd += sequenceInfo.getMemoryConsumption();
		totalSubSequencesToAdd += encodedSequence.length - 1;
		sequencesToAdd.add(sequenceInfo);

		if (totalMemorytoAdd > totalMemoryUsedToStoreSubSequences) {
			try {
				addSequences(sequencesToAdd);
			} catch (IOException e) {
				throw new IndexConstructionException(e);
			}
			sequencesToAdd = Lists.newLinkedList();
			totalMemorytoAdd = 0;
			totalSubSequencesToAdd = 0;
		}

	}

	@SuppressWarnings("unchecked")
	private void addSequences(List<SequenceInfo> toAddSequences) throws IndexConstructionException, IOException {
		logger.info("Adding " + toAddSequences.size() + " sequences with " + totalSubSequencesToAdd + " sub sequences.");
		long indexDiv = getIndexDiv();
		long e = indexSize / indexDiv;
		if (e > Integer.MAX_VALUE) {
			throw new IOException("Fudeu1");
		}
		int entriesArraySize = (int) e;
		List<Entry>[] entries = new ArrayList[entriesArraySize];

		for (SequenceInfo sequenceInfo : toAddSequences) {
			int sequenceId = sequenceInfo.sequenceId;
			int[] encodedSequence = sequenceInfo.encodedSequence;

			int length = encodedSequence.length;
			for (int arrayPos = SequenceEncoder.getPositionBeginBitsVector(); arrayPos < length; arrayPos++) {
				int sequencePos = (arrayPos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceOffSet;
				int subSequence = encodedSequence[arrayPos];

				if (!this.lowComplexitySubSequences.get(subSequence)) {
					long mLong = subSequence % entriesArraySize;
					if (mLong > Integer.MAX_VALUE) {
						throw new IOException("Fudeu2");
					}
					int m = (int) mLong;
					if (entries[m] == null) {
						entries[m] = new ArrayList<Entry>(5);
					}
					entries[m].add(new Entry(subSequence, sequenceId, sequencePos));
				} else {
					totalFiltered++;
				}
			}
		}

		DataOutputStream stream = getEntriesOutpuPhase1();
		for (int i = 0; i < entriesArraySize; i++) {
			List<Entry> subSequenceEntries = entries[i];
			if (subSequenceEntries != null) {
				for (Entry entry : subSequenceEntries) {
					int subSequence = entry.subSequence;
					int sequenceId = entry.sequenceId;
					long position = entry.position;
					Entry.writeTo(subSequence, sequenceId, position, stream);
					totalEntries++;
				}
			}
		}
	}

	private long getIndexDiv() {
		// the memory used if each possible subSequence has 5 entries.
		long estimedSize = indexSize * (8 /* ArrayList instance */
		+ 8 /* Internal Array */
		+ (5 * Entry.INSTANCE_SIZE)) /* entries by index */
				+ 4; /* ArrayList.size member */

		long div = 1;

		while (estimedSize / div > totalMemoryUsedToStoreSubSequences) {
			div++;
		}

		return div;
	}

	private static Comparator<Entry> ENTRY_COMPARATOR = new Comparator<Entry>() {
		@Override
		public int compare(Entry o1, Entry o2) {
			if (o1.subSequence != o2.subSequence) {
				return o1.subSequence - o2.subSequence;
			}

			if (o1.sequenceId != o2.sequenceId) {
				return o1.sequenceId - o2.sequenceId;
			}

			return (int) (o1.position - o2.position);
		}
	};

	public void finishConstruction() throws IndexConstructionException {
		List<SortedEntriesInfo> sortedEntriesInfos = Lists.newLinkedList();
		if (!sequencesToAdd.isEmpty()) {

			try {
				addSequences(sequencesToAdd);
				sequencesToAdd = Lists.newLinkedList();
				totalMemorytoAdd = 0;
				totalSubSequencesToAdd = 0;
				getEntriesOutpuPhase1().flush();
			} catch (IOException e) {
				throw new IndexConstructionException(e);
			}
		}

		try {
			logger.info("Index Construction phase 2.");
			indexConstructionPhase2(sortedEntriesInfos);
			logger.info("Index Construction phase 3.");
			indexConstructionPhase3(sortedEntriesInfos);
			logger.info("Index Construction phase 4.");
			indexConstructionPhase4();
			logger.info("Index Construction finished.");
		} catch (IOException e) {
			throw new IndexConstructionException(e);
		}
	}

	private void indexConstructionPhase2(List<SortedEntriesInfo> sortedEntriesInfos) throws IOException,
			IndexConstructionException {
		long totalUsedMemory = 0;
		long offset = 0;
		long beginOffset = 0;
		long processedEntries = 0;

		logger.info("Filtered " + totalFiltered + " low complexity subsequences.");

		while (processedEntries < totalEntries) {

			List<Entry> allEntries = Lists.newArrayList();
			beginOffset = offset;
			totalUsedMemory = 0;

			while (totalUsedMemory < memoryChuck && processedEntries < totalEntries) {
				allEntries.add(Entry.newFrom(getEntriesInputPhase1()));
				processedEntries++;
				totalUsedMemory += Entry.INSTANCE_SIZE;
				offset += Entry.DISK_SPACE;
			}

			Collections.sort(allEntries, ENTRY_COMPARATOR);
			sortedEntriesInfos.add(new SortedEntriesInfo(beginOffset, allEntries.size() * Entry.DISK_SPACE));

			for (Entry entry : allEntries) {
				entry.write(getEntriesOutpuPhase2());
			}
		}

		getEntriesOutpuPhase2().flush();
	}

	private void indexConstructionPhase3(List<SortedEntriesInfo> sortedEntriesInfos) throws IOException,
			IndexConstructionException {

		List<SortedEntriesBufferManager> entryBufferManagers = Lists.newArrayList();

		FileChannel channel = new FileInputStream(getEntriesFilePhase2()).getChannel();
		for (SortedEntriesInfo info : sortedEntriesInfos) {
			MappedByteBuffer map = channel.map(MapMode.READ_ONLY, info.offset, info.size);
			entryBufferManagers.add(new SortedEntriesBufferManager(map.asIntBuffer()));
		}

		resetFilePhase1();

		int totalWrote = 0;
		// merging
		// TODO: Some printout to show de status.
		// TODO: Rathen than the "while", use a MinHeap to get the next element.
		for (int i = 0; i < totalEntries; i++) {
			SortedEntriesBufferManager smallerOwner = null;
			for (SortedEntriesBufferManager bm : entryBufferManagers) {
				if (smallerOwner == null) {
					smallerOwner = bm;
				} else if (bm.isSmaller(smallerOwner)) {
					smallerOwner = bm;
				}
			}

			totalWrote++;
			smallerOwner.consumeEntry().write(getEntriesOutpuPhase1());

			if (!smallerOwner.hasRemaining()) {
				entryBufferManagers.remove(smallerOwner);
			}
		}

		getEntriesOutpuPhase1().flush();
	}

	private void indexConstructionPhase4() throws IOException, IndexConstructionException {
		FileChannel memoryInvertedIndexFileChannel = new FileOutputStream(memoryInvertedIndex.getMemoryInvertedIndexFile(), true).getChannel();

		ReadSortedEntriesFromFile entriesFromFile = new ReadSortedEntriesFromFile(getEntriesInputPhase1(), totalEntries);

		FileOutputStream fileOutputStream = new FileOutputStream(memoryInvertedIndex.getMemoryInvertedOffsetIndexFile(), true);
		DataOutputStream offsetIndexStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream));

		while (entriesFromFile.hasMore()) {
			List<Entry> subSequenceEntries = entriesFromFile.readEntries();
			int subSequence = entriesFromFile.subSequence;

			long offset = memoryInvertedIndexFileChannel.position();
			if (offset > Long.MAX_VALUE) {
				throw new IOException("NEW: The offset position is too big: " + offset);
			}

			InvertedIndexBuck.Builder buckBuilder = InvertedIndexBuck.newBuilder();
			for (Entry e : subSequenceEntries) {
				buckBuilder.addBuck(SubSequenceIndexInfo.newIndexInfo(e.sequenceId, e.position));
			}

			InvertedIndexBuck buck = buckBuilder.build();
			byte[] buckArray = buck.toByteArray();
			memoryInvertedIndexFileChannel.write(ByteBuffer.wrap(buckArray));

			IndexFileOffset.writeTo(subSequence, offset, buckArray.length, offsetIndexStream);
		}

		memoryInvertedIndexFileChannel.close();
		offsetIndexStream.flush();
		offsetIndexStream.close();

	}

	private static class SequenceInfo {
		int sequenceId;
		int[] encodedSequence;

		public SequenceInfo(int sequenceId, int[] encodedSequence) {
			this.sequenceId = sequenceId;
			this.encodedSequence = encodedSequence;
		}

		public int getMemoryConsumption() {
			return 8 /* Instance */
					+ 4 /* sequenceId */
					+ 8 /* encodedSequence array instance */
					+ 4 * encodedSequence.length; /* encodedSequence elements */
		}
	}

	private static class Entry {
		public static final int INSTANCE_SIZE = 8 /* Instance */
		+ 4 /* subSequence */
		+ 4 /* sequenceId */
		+ 8; /* position */

		private static final int DISK_SPACE = 4 /* subSequence */
		+ 4 /* sequenceId */
		+ 8; /* position */

		final int subSequence;
		final int sequenceId;
		final long position;

		public Entry(int subSequence, int sequenceId, long position) {
			this.subSequence = subSequence;
			this.sequenceId = sequenceId;
			this.position = position;
		}

		public boolean isSmaller(Entry other) {
			if (subSequence < other.subSequence) {
				return true;
			}
			if (subSequence > other.subSequence) {
				return false;
			}

			if (sequenceId < other.sequenceId) {
				return true;
			}
			if (sequenceId > other.sequenceId) {
				return false;
			}

			if (position < other.position) {
				return true;
			}

			return false;
		}

		public void write(DataOutputStream stream) throws IOException {
			stream.writeInt(subSequence);
			stream.writeInt(sequenceId);
			stream.writeLong(position);
		}

		public static void writeTo(int subSequence, int sequenceId, long position, DataOutputStream stream)
				throws IOException {
			stream.writeInt(subSequence);
			stream.writeInt(sequenceId);
			stream.writeLong(position);
		}

		public static Entry newFrom(DataInputStream stream) throws IOException {
			int subSequence = stream.readInt();
			int sequenceId = stream.readInt();
			long position = stream.readLong();
			return new Entry(subSequence, sequenceId, position);
		}
	}

	private static class SortedEntriesInfo {
		private final long offset;
		private final int size;

		public SortedEntriesInfo(long offset, int size) {
			this.offset = offset;
			this.size = size;
		}

		@Override
		public String toString() {
			return "[" + offset + ";" + size + "]";
		}
	}

	private static class SortedEntriesBufferManager {
		private final IntBuffer intBuffer;
		private Entry actual;

		public SortedEntriesBufferManager(IntBuffer intBuffer) {
			this.intBuffer = intBuffer;
			this.actual = getNextEntry();
		}

		private Entry getNextEntry() {
			if (!intBuffer.hasRemaining()) {
				return null;
			}

			int subSequence = intBuffer.get();
			int sequenceId = intBuffer.get();
			long position = (((long) intBuffer.get()) << 32) | intBuffer.get();

			return new Entry(subSequence, sequenceId, position);
		}

		public boolean hasRemaining() {
			return actual != null;
		}

		public Entry consumeEntry() {
			Entry prev = actual;
			actual = getNextEntry();
			return prev;
		}

		public boolean isSmaller(SortedEntriesBufferManager other) {
			return actual.isSmaller(other.actual);
		}
	}

	private static class ReadSortedEntriesFromFile {
		DataInputStream stream;
		private final long totalEntries;
		private long count;
		int subSequence;
		Entry actual;

		public ReadSortedEntriesFromFile(DataInputStream stream, long totalEntries) throws IOException {
			this.stream = stream;
			this.totalEntries = totalEntries;
			this.count = 0;
			this.actual = getNextEntry();
		}

		public List<Entry> readEntries() throws IOException {
			this.subSequence = actual.subSequence;

			List<Entry> entries = Lists.newLinkedList();
			entries.add(actual);

			Entry e;
			for (e = getNextEntry(); e != null && e.subSequence == actual.subSequence; e = getNextEntry()) {
				entries.add(e);
			}
			actual = e;

			return entries;
		}

		private Entry getNextEntry() throws IOException {
			if (count < totalEntries) {
				count++;
				return Entry.newFrom(stream);
			}
			return null;
		}

		public boolean hasMore() {
			return actual != null;
		}

	}
}
