package bio.pih.search;

import java.util.ArrayList;
import java.util.List;

import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.io.Utils;

import com.google.common.collect.Lists;

public class IndexRetrievedData {

	private final List<RetrievedArea>[] retrievedAreasArray;
	private final FuckingArrayList<RetrievedArea>[] openedAreasArray;
	private final SearchParams sp;
	private final int minLength;
	private final int subSequenceLength;
	private int totalAreas = -1;
	private int totalSequences = -1;

	@SuppressWarnings("unchecked")
	public IndexRetrievedData(int size, SearchParams sp, int minLength, int subSequenceLength) {
		this.sp = sp;
		this.minLength = minLength;
		this.subSequenceLength = subSequenceLength;
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

					// Check if the area end is away from the actual sequence
					// pos.
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

	public List<RetrievedArea>[] getRetrievedAreas() {
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
		int totalAreas = 0;
		int totalSequences = 0;
		for (int i = 0; i < retrievedAreasArray.length; i++) {
			if (retrievedAreasArray[i] != null) {
				totalAreas += retrievedAreasArray[i].size();
				totalSequences++;
			}
		}
		this.totalSequences  = totalSequences;
		this.totalAreas = totalAreas;
		return retrievedAreasArray;
	}

	public int getTotalAreas() {
		return totalAreas;
	}
	
	public int getTotalSequences() {
		return totalSequences;
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
			sb.append("([");
			sb.append(queryAreaBegin);
			sb.append(",");
			sb.append(queryAreaEnd);
			sb.append("]");
			sb.append("[");
			sb.append(sequenceAreaBegin);
			sb.append(",");
			sb.append(sequenceAreaEnd);
			sb.append("])");

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
