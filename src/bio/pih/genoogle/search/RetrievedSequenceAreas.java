package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.proto.Io.StoredSequence;

public class RetrievedSequenceAreas {
	
	public static Comparator<RetrievedSequenceAreas> AREAS_LENGTH_COMPARATOR = new Comparator<RetrievedSequenceAreas>() {
		@Override
		public int compare(final RetrievedSequenceAreas o1, final RetrievedSequenceAreas o2) {
			return o2.getBiggestLength() - o1.getBiggestLength();
		}
	};

	
	private final ArrayList<RetrievedArea> LIST_EMPTY = new ArrayList<RetrievedArea>(0);
	
	private int sequenceId;
	private final IndexSearcher indexSearcher;
	private final IndexReverseComplementSearcher reverseComplementIndexSearcher;
	private final int biggestHspLength;
	private final ArrayList<RetrievedArea>[] areas;
	private final ArrayList<RetrievedArea>[] rcAreas;
	private final int frames;

	public RetrievedSequenceAreas(int sequenceId, IndexSearcher indexSearcher, IndexReverseComplementSearcher reverseComplementIndexSearcher, int frames, ArrayList<RetrievedArea> ... areas) {
		this.sequenceId = sequenceId;
		this.indexSearcher = indexSearcher;
		this.reverseComplementIndexSearcher = reverseComplementIndexSearcher;
		this.frames = frames;
		this.areas = new ArrayList[frames];
		this.rcAreas = new ArrayList[frames];
		for (int i = 0; i < frames; i++) {
			this.areas[i] = areas[i].size() > 0 ? areas[i] : LIST_EMPTY;
			this.rcAreas[i] = areas[i+frames].size() > 0 ? areas[i+frames] : LIST_EMPTY;
		}
		this.biggestHspLength = Math.max(getBiggestHspLength(this.areas), getBiggestHspLength(this.rcAreas));
	}

	private int getBiggestHspLength(List<RetrievedArea>[] areas) {
		int biggest = 0;
		for (int i = 0; i < this.frames; i++) {
			for (RetrievedArea area : areas[i]) {
				if (area.length() > biggest) {
					biggest = area.length();
				}
			}
		}
		return biggest;
	}

	public int getFrames() {
		return frames;
	}
	
	public List<RetrievedArea>[] getAreas() {
		return areas;
	}

	public List<RetrievedArea>[] getReverseComplementAreas() {
		return rcAreas;
	}

	public int getBiggestLength() {
		return biggestHspLength;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public StoredSequence getStoredSequence() throws IOException {
		return indexSearcher.getDatabank().getSequenceFromId(sequenceId);
	}

	public IndexedSequenceDataBank getDatabank() {
		return indexSearcher.getDatabank();
	}

	public IndexSearcher getIndexSearcher() {
		return indexSearcher;
	}

	public IndexSearcher getReverIndexSearcher() {
		return reverseComplementIndexSearcher;
	}

	@Override
	public String toString() {
		return sequenceId + " " + biggestHspLength + " " + areas + " " + rcAreas;
	}
}
