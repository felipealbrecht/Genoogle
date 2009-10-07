package bio.pih.index;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.proto.Io.InvertedIndexBuck;
import bio.pih.io.proto.Io.InvertedIndexBuckPosition;
import bio.pih.io.proto.Io.InvertedIndexFilePositions;

import com.google.common.collect.Lists;

/**
 * Class which uses SortBased method to create the inverted index. Possibly will be merged with the
 * {@link MemoryInvertedIndex}.
 * 
 * @author albrecht
 */
public class SortBasedMemoryInvertedIndex extends MemoryInvertedIndex {

	private static final int DISK_SPACE_BY_ENTRY = 4 * 3; // 4 bytes by int.
	private static final int MEMORY_BY_ENTRY = 20; // 8 for the class, 12 for the 3 ints.
	private static final int MINIMUM_ENTRY_SET = 10;

	private int totalSortMemory = 128 * 1024 * 1024; // 128 MEGABYTES.

	private File entriesTempFilePhase1;
	private DataOutputStream entriesOutputPhase1;
	private DataInputStream entriesInputPhase1;

	private File entriesTempFilePhase2;
	private DataOutputStream entriesOutputStreamPhase2;
	private DataInputStream entriesInputStreamPhase2;

	private int totalEntries = -1;

	public SortBasedMemoryInvertedIndex(SequenceDataBank databank, int subSequenceLength)
			throws ValueOutOfBoundsException {
		super(databank, subSequenceLength);
	}

	public SortBasedMemoryInvertedIndex(SequenceDataBank databank, int subSequenceLength, int totalSortMemory) {
		this(databank, subSequenceLength);
		this.setTotalSortMemory(totalSortMemory);
	}

	@Override
	public void constructIndex() {
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

	private void resetFilePhase1() throws IOException {
		if (entriesTempFilePhase1 != null && entriesTempFilePhase1.exists()) {
			entriesTempFilePhase1.delete();
		}
		entriesTempFilePhase1 = File.createTempFile(databank.getFullPath().getName(), ".sbms_1.tmp");
		entriesTempFilePhase1.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(getEntriesFilePhase1());
		entriesOutputPhase1 = new DataOutputStream(fileOutputStream);
		FileInputStream fileInputStream = new FileInputStream(getEntriesFilePhase1());
		entriesInputPhase1 = new DataInputStream(fileInputStream);
	}

	private void resetFilePhase2() throws IOException {
		if (entriesTempFilePhase2 != null && entriesTempFilePhase2.exists()) {
			entriesTempFilePhase2.delete();
		}
		entriesTempFilePhase2 = File.createTempFile(databank.getFullPath().getName(), ".sbms_2.tmp");
		entriesTempFilePhase2.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(getEntriesFilePhase2());
		entriesOutputStreamPhase2 = new DataOutputStream(fileOutputStream);
		FileInputStream fileInputStream = new FileInputStream(getEntriesFilePhase2());
		entriesInputStreamPhase2 = new DataInputStream(fileInputStream);
	}

	private DataOutputStream getEntriesOutpuPhase1() {
		if (entriesOutputPhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesOutputPhase1;
	}

	private DataInputStream getEntriesInputPhase1() {
		if (entriesInputPhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesInputPhase1;
	}

	private File getEntriesFilePhase1() {
		if (entriesTempFilePhase1 == null) {
			throw new IndexConstructionException("The index structure was not initialized");
		}
		return entriesTempFilePhase1;
	}

	private DataOutputStream getEntriesOutpuPhase2() {
		if (entriesOutputStreamPhase2 == null) {
			throw new IndexConstructionException("The index structure was not initialized.");
		}
		return entriesOutputStreamPhase2;
	}

	private File getEntriesFilePhase2() {
		if (entriesTempFilePhase2 == null) {
			throw new IndexConstructionException("The index structure was not initialized");
		}
		return entriesTempFilePhase2;
	}

	public void setTotalSortMemory(int size) {
		if (size < MINIMUM_ENTRY_SET * MEMORY_BY_ENTRY) {
			throw new IndexConstructionException("The sort memory size is too small.");
		}
		this.totalSortMemory = size;
	}

	@Override
	public void addSequence(int sequenceId, int[] encodedSequence, int subSequenceOffSet) {
		SubSequenceEntries[] entries = new SubSequenceEntries[indexSize];
		List<SubSequenceEntries> addedEntries = Lists.newLinkedList();
		int length = encodedSequence.length;

		for (int arrayPos = SequenceEncoder.getPositionBeginBitsVector(); arrayPos < length; arrayPos++) {
			int sequencePos = (arrayPos - SequenceEncoder.getPositionBeginBitsVector()) * subSequenceOffSet;

			if (entries[encodedSequence[arrayPos]] == null) {
				entries[encodedSequence[arrayPos]] = new SubSequenceEntries(encodedSequence[arrayPos]);
				addedEntries.add(entries[encodedSequence[arrayPos]]);
			}
			entries[encodedSequence[arrayPos]].addEntry(sequencePos);
		}

		for (SubSequenceEntries subSequenceEntries : addedEntries) {
			int subSequence = subSequenceEntries.getSubSequence();
			for (Integer position : subSequenceEntries.getEntries()) {
				try {
					Entry entry = new Entry(subSequence, sequenceId, position);
					entry.writeTo(getEntriesOutpuPhase1());
					totalEntries++;
				} catch (IOException e) {
					throw new IndexConstructionException(e);
				}
			}
		}
	}

	private static Comparator<Entry> ENTRY_COMPARATOR = new Comparator<Entry>() {
		@Override
		public int compare(Entry o1, Entry o2) {
			if (o1.subSequence != o2.subSequence) {
				return o1.subSequence - o2.subSequence;
			}

			if (o1.sequenceId != o2.sequenceId) {
				return o1.sequenceId - o1.sequenceId;
			}

			return o1.position - o1.position;
		}
	};

	@Override
	public void finishConstruction() {
		List<SortedEntriesInfo> sortedEntriesInfos = Lists.newLinkedList();

		try {
			indexConstructionPhase2(sortedEntriesInfos);
			indexConstructionPhase3(sortedEntriesInfos);
			indexConstructionPhase4();
		} catch (IOException e) {
			throw new IndexConstructionException(e);
		}
	}

	private void indexConstructionPhase4() throws IOException {
		FileChannel memoryInvertedIndexFileChannel = new FileOutputStream(getMemoryInvertedIndexFile(), true).getChannel();

		InvertedIndexFilePositions.Builder invertedIndexFilePositionsBuilder = InvertedIndexFilePositions.newBuilder();
		invertedIndexFilePositionsBuilder.setSize(indexSize);

		ReadSortedEntriesFromFile entriesFromFile = new ReadSortedEntriesFromFile(getEntriesInputPhase1(), totalEntries);

		while (entriesFromFile.hasMore()) {
			List<Entry> subSequenceEntries = entriesFromFile.readEntries();
			int subSequence = entriesFromFile.subSequence;

			long offset = memoryInvertedIndexFileChannel.position();
			if (offset > Integer.MAX_VALUE) {
				throw new IOException("The offset position is too big.");
			}

			InvertedIndexBuck.Builder buckBuilder = InvertedIndexBuck.newBuilder();
			for (Entry e : subSequenceEntries) {
				buckBuilder.addBuck(SubSequenceIndexInfo.newIndexInfo(e.sequenceId, e.position));
			}

			InvertedIndexBuck buck = buckBuilder.build();
			byte[] buckArray = buck.toByteArray();
			memoryInvertedIndexFileChannel.write(ByteBuffer.wrap(buckArray));

			InvertedIndexBuckPosition.Builder buckPositionBuilder = InvertedIndexBuckPosition.newBuilder();
			buckPositionBuilder.setBuck(subSequence);
			buckPositionBuilder.setOffset((int) offset);
			buckPositionBuilder.setLength(buckArray.length);
			InvertedIndexBuckPosition buckPosition = buckPositionBuilder.build();
			invertedIndexFilePositionsBuilder.addPosition(buckPosition);
		}
		memoryInvertedIndexFileChannel.close();
		InvertedIndexFilePositions indexFilePositions = invertedIndexFilePositionsBuilder.build();

		FileChannel indexFilePositionsFileChannel = new FileOutputStream(getMemoryInvertedOffsetIndexFile(), true).getChannel();
		indexFilePositionsFileChannel.write(ByteBuffer.wrap(indexFilePositions.toByteArray()));
		indexFilePositionsFileChannel.close();
	}

	private void indexConstructionPhase3(List<SortedEntriesInfo> sortedEntriesInfos) throws IOException {
		{
			List<SortedEntriesBufferManager> entryBufferManagers = Lists.newArrayList();

			FileChannel channel = new FileInputStream(getEntriesFilePhase2()).getChannel();
			for (SortedEntriesInfo info : sortedEntriesInfos) {
				MappedByteBuffer map = channel.map(MapMode.READ_ONLY, info.offset, info.size);
				entryBufferManagers.add(new SortedEntriesBufferManager(map.asIntBuffer()));
			}

			resetFilePhase1();

			int totalWrote = 0;
			// merging
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
				smallerOwner.consumeEntry().writeTo(getEntriesOutpuPhase1());

				if (!smallerOwner.hasRemaining()) {
					entryBufferManagers.remove(smallerOwner);
				}
			}
		}
	}

	private void indexConstructionPhase2(List<SortedEntriesInfo> sortedEntriesInfos) throws IOException {
		int totalMemory = 0;
		int offset = 0;
		int beginOffset = 0;
		int processedEntries = 0;

		while (processedEntries < totalEntries) {

			List<Entry> allEntries = Lists.newArrayList();
			beginOffset = offset;
			totalMemory = 0;

			while (totalMemory < totalSortMemory && processedEntries < totalEntries) {
				allEntries.add(Entry.newFrom(getEntriesInputPhase1()));
				processedEntries++;
				totalMemory += MEMORY_BY_ENTRY;
				offset += DISK_SPACE_BY_ENTRY;
			}

			Collections.sort(allEntries, ENTRY_COMPARATOR);
			sortedEntriesInfos.add(new SortedEntriesInfo(beginOffset, allEntries.size() * DISK_SPACE_BY_ENTRY));

			for (Entry entry : allEntries) {
				entry.writeTo(getEntriesOutpuPhase2());
			}
		}
	}

	private static class Entry {
		final int subSequence;
		final int sequenceId;
		final int position;

		public Entry(int subSequence, int sequenceId, int position) {
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

		public void writeTo(DataOutputStream stream) throws IOException {
			stream.writeInt(subSequence);
			stream.writeInt(sequenceId);
			stream.writeInt(position);
		}

		public static Entry newFrom(DataInputStream stream) throws IOException {
			int subSequence = stream.readInt();
			int sequenceId = stream.readInt();
			int position = stream.readInt();
			return new Entry(subSequence, sequenceId, position);
		}

		@Override
		public String toString() {
			return "{" + DNASequenceEncoderToInteger.getEncoder(10).decodeIntegerToString(subSequence) + ":"
					+ sequenceId + ":" + position + "}";
		}
	}

	private static class SubSequenceEntries {
		int subSequence;
		LinkedList<Integer> entries;

		public SubSequenceEntries(int subSequence) {
			this.subSequence = subSequence;
			entries = Lists.newLinkedList();
		}

		private void addEntry(int position) {
			entries.add(position);
		}

		public int getSubSequence() {
			return subSequence;
		}

		public LinkedList<Integer> getEntries() {
			return entries;
		}
	}

	private static class SortedEntriesInfo {
		private final int offset;
		private final int size;

		public SortedEntriesInfo(int offset, int size) {
			this.offset = offset;
			this.size = size;
		}

		@Override
		public String toString() {
			return "[" + offset + ";" + size + "]";
		}
	}

	private static class SortedEntriesBufferManager {
		private final IntBuffer buffer;
		private Entry actual;

		public SortedEntriesBufferManager(IntBuffer buffer) {
			this.buffer = buffer;
			this.actual = getNextEntry();
		}

		private Entry getNextEntry() {
			if (!buffer.hasRemaining()) {
				return null;
			}

			int subSequence = buffer.get();
			int sequenceId = buffer.get();
			int position = buffer.get();

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
		private final int totalEntries;
		private int count;
		int subSequence;
		Entry actual;

		public ReadSortedEntriesFromFile(DataInputStream stream, int totalEntries) throws IOException {
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
				entries.add(actual);
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
