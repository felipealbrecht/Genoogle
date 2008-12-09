package bio.pih.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.io.proto.Io.StoredSequence;

import com.google.common.collect.Lists;

public class IndexRetrievedData {

	private final List<RetrievedArea>[] retrievedAreasArray;
	private final FuckingArrayList<RetrievedArea>[] openedAreasArray;
	private final SearchParams sp;
	private final int minLength;
	private final int subSequenceLength;
	private int sumLength = -1;
	private int totalAreas = -1;
	private final DNAIndexSearcher searcher;
	private List<SequenceRetrievedAreas> result;

	@SuppressWarnings("unchecked")
	public IndexRetrievedData(int size, SearchParams sp, int minLength, int subSequenceLength,
			DNAIndexSearcher searcher) {
		this.sp = sp;
		this.minLength = minLength;
		this.subSequenceLength = subSequenceLength;
		this.searcher = searcher;

		retrievedAreasArray = new List[size];
		openedAreasArray = new FuckingArrayList[size];
	}

	void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
		int start = EncoderSubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
		int sequenceId = EncoderSubSequenceIndexInfo.getSequenceId(subSequenceInfoIntRepresention);

		mergeOrRemoveOrNew(queryPos, start, sequenceId);
	}

	private void mergeOrRemoveOrNew(int queryPos, int sequencePos, int sequenceId) {
		boolean merged = false;
		FuckingArrayList<RetrievedArea> openedList = openedAreasArray[sequenceId];

		if (openedList == null) {
			openedList = new FuckingArrayList<RetrievedArea>();
			openedAreasArray[sequenceId] = openedList;
			RetrievedArea retrievedArea = new RetrievedArea(queryPos, sequencePos,
					subSequenceLength);
			openedList.add(retrievedArea);

		} else {
			int fromIndex = -1;
			int toIndex = -1;

			int size = openedList.size();
			for (int pos = 0; pos < size; pos++) {
				RetrievedArea openedArea = openedList.get(pos);
				// Try merge with previous area.
				if (openedArea.setTestAndSet(queryPos, sequencePos,
						sp.getMaxSubSequencesDistance(), subSequenceLength)) {
					merged = true;

					// Check if the area end is away from the actual sequence position.
				} else if (queryPos - openedArea.queryAreaEnd > sp.getMaxSubSequencesDistance()) {
					// Mark the areas to remove.
					if (fromIndex == -1) {
						fromIndex = pos;
						toIndex = pos;
					} else {
						toIndex = pos;
					}
					if (openedArea.length() >= minLength) {
						if (retrievedAreasArray[sequenceId] == null) {
							retrievedAreasArray[sequenceId] = Lists.newArrayList();
						}
						retrievedAreasArray[sequenceId].add(openedArea);
					}
				}
			}

			if (fromIndex != -1) {
				openedList.removeRange(fromIndex, toIndex + 1);
			}

			if (!merged) {
				RetrievedArea retrievedArea = new RetrievedArea(queryPos, sequencePos,
						subSequenceLength);
				openedList.add(retrievedArea);
			}
		}
	}

	public void finish() {
		for (int sequenceId = 0; sequenceId < openedAreasArray.length; sequenceId++) {
			List<RetrievedArea> openedAreaList = openedAreasArray[sequenceId];
			if (openedAreaList != null) {
				for (RetrievedArea openedArea : openedAreaList) {
					if (openedArea.length() >= minLength) {
						if (retrievedAreasArray[sequenceId] == null) {
							retrievedAreasArray[sequenceId] = Lists.newArrayList();
						}
						retrievedAreasArray[sequenceId].add(openedArea);
					}
				}
			}
		}

		this.totalAreas = 0;
		this.sumLength = 0;

		this.result = Lists.newLinkedList();
		for (int i = 0; i < retrievedAreasArray.length; i++) {
			if (retrievedAreasArray[i] != null) {
				this.totalAreas += retrievedAreasArray[i].size();
				SequenceRetrievedAreas sequenceRetrievedAreas = new SequenceRetrievedAreas(i,
						searcher, retrievedAreasArray[i]);
				result.add(sequenceRetrievedAreas);
				sumLength += sequenceRetrievedAreas.getSumLengths();
			}
		}
	}

	public int getTotalAreas() {
		return totalAreas;
	}

	public int getTotalSequences() {
		return this.result.size();
	}

	public List<SequenceRetrievedAreas> getSequencesRetrievedAreas() {
		return result;
	}

	public int getSumLength() {
		return sumLength;
	}

	public final static class SequenceRetrievedAreas {
		final int sequenceId;
		final int sumLengths;
		final DNAIndexSearcher indexSearcher;
		final List<RetrievedArea> retrievedAreas;

		public SequenceRetrievedAreas(int sequenceId, DNAIndexSearcher indexSearcher,
				List<RetrievedArea> retrievedAreas) {
			this.sequenceId = sequenceId;
			this.indexSearcher = indexSearcher;
			this.retrievedAreas = retrievedAreas;
			this.sumLengths = sumTotalLengths(retrievedAreas);
		}

		private int sumTotalLengths(List<RetrievedArea> areas) {
			int total = 0;
			for (RetrievedArea area : areas) {
				total += area.length();
			}
			return total;
		}

		public int getSumLengths() {
			return sumLengths;
		}

		public List<RetrievedArea> getRetrievedAreas() {
			return retrievedAreas;
		}

		public int getSequenceId() {
			return sequenceId;
		}

		public StoredSequence getStoredSequence() throws IOException {
			return indexSearcher.getDatabank().getSequenceFromId(sequenceId);
		}

		public IndexedDNASequenceDataBank getDatabank() {
			return indexSearcher.getDatabank();
		}

		public DNAIndexSearcher getIndexSearcher() {
			return indexSearcher;
		}
		
	}

	public final static class RetrievedArea {
		int queryAreaBegin;
		int queryAreaEnd;
		int sequenceAreaBegin;
		int sequenceAreaEnd;
		int length;

		public RetrievedArea(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaBegin + subSequenceLength;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaBegin + subSequenceLength;
			this.length = subSequenceLength;
		}

		public int length() {
			return this.length;
		}

		public boolean setTestAndSet(int newQueryPos, int newSequencePos,
				int maxSubSequenceDistance, int subSequenceLength) {

			if (Utils.isIn(queryAreaBegin, queryAreaEnd + maxSubSequenceDistance, newQueryPos)) {
				if (Utils.isIn(sequenceAreaBegin, sequenceAreaEnd + maxSubSequenceDistance,
						newSequencePos)) {

					int newQueryPosEnd = newQueryPos + subSequenceLength;
					if (newQueryPosEnd > this.queryAreaEnd) {
						this.queryAreaEnd = newQueryPosEnd;
					}

					int newSequencePosEnd = newSequencePos + subSequenceLength;
					if (newSequencePosEnd > this.sequenceAreaEnd) {
						this.sequenceAreaEnd = newSequencePosEnd;
					}

					this.length = Math.min(queryAreaEnd - queryAreaBegin, sequenceAreaEnd
							- sequenceAreaBegin);
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			sb.append(queryAreaBegin);
			sb.append(",");
			sb.append(queryAreaEnd);
			sb.append("]");
			sb.append("[");
			sb.append(sequenceAreaBegin);
			sb.append(",");
			sb.append(sequenceAreaEnd);
			sb.append(")");

			return sb.toString();
		}
	}

	private static class FuckingArrayList<E> extends ArrayList<E> {
		private static final long serialVersionUID = -7142636234255880892L;

		public FuckingArrayList() {
		}

		@Override
		public void removeRange(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}

}
