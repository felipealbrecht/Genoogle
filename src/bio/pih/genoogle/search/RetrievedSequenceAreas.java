package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.proto.Io.StoredSequence;

public class RetrievedSequenceAreas {
	
	private final ArrayList<RetrievedArea> LIST_EMPTY = new ArrayList<RetrievedArea>(0);
	
	private final int sequenceId;
	private final int biggestHspLength;
	private final ArrayList<RetrievedArea>[] areas;
	private final ArrayList<RetrievedArea>[] rcAreas;
	private final int frames;
	private final IndexedSequenceDataBank databank;

	@SuppressWarnings("unchecked")
	public RetrievedSequenceAreas(int sequenceId, IndexedSequenceDataBank databank, ArrayList<RetrievedArea> ... areas) {
		this.sequenceId = sequenceId;
		this.databank = databank;
		this.frames = areas.length / 2;
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

	public StoredSequence getStoredSequence() throws IOException {
		return databank.getSequenceFromId(sequenceId);
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

	@Override
	public String toString() {
		return sequenceId + " " + biggestHspLength + " " + Arrays.toString(areas) + " " + Arrays.toString(rcAreas);
	}
}
