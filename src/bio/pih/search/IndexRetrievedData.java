package bio.pih.search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.io.proto.Io.StoredSequence;
import bio.pih.util.CircularArrayList;
import bio.pih.util.CircularArrayList.Iterator;

import com.google.common.collect.Lists;

public class IndexRetrievedData {

	private final List<RetrievedArea>[] retrievedAreasArray;
	private final CircularArrayList[] openedAreasArray;
	private final int minLength;
	private final int subSequenceLength;
	private final int maxSubSequenceDistance;

	@SuppressWarnings("unchecked")
	public IndexRetrievedData(int size, SearchParams sp, int minLength, int subSequenceLength, DNAIndexSearcher searcher) {

		this.minLength = minLength;
		this.subSequenceLength = subSequenceLength;
		this.maxSubSequenceDistance = sp.getMaxSubSequencesDistance();

		retrievedAreasArray = new List[size];
		openedAreasArray = new CircularArrayList[size];
	}

	final void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
		int sequencePos = SubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
		int sequenceId = SubSequenceIndexInfo.getSequenceId(subSequenceInfoIntRepresention);

		mergeOrRemoveOrNew(queryPos, sequencePos, sequenceId);
	}

	private final void mergeOrRemoveOrNew(int queryPos, int sequencePos, int sequenceId) {

		boolean merged = false;

		CircularArrayList openedList = openedAreasArray[sequenceId];

		if (openedList == null) {
			openedList = new CircularArrayList();
			openedAreasArray[sequenceId] = openedList;
			openedList.addFast(queryPos, sequencePos, subSequenceLength);

		} else {
			int totalRemove = 0;

			int size = openedList.size();
			if (size == 0) {
				merged = false;
			} else {				
				Iterator iterator = openedList.getIterator();
				while (iterator.hasNext()) {
					final RetrievedArea openedArea = iterator.next();
					// Try merge with previous area.
					if (openedArea.testAndSet(queryPos, sequencePos, maxSubSequenceDistance, subSequenceLength)) {
						merged = true;

						openedList.rePos(openedArea, iterator.getPos());

						// Check if the area end is away from the actual sequence position.
					} else if (queryPos - openedArea.queryAreaEnd > maxSubSequenceDistance) {
						// Count areas to remove.
						totalRemove++;
						if (openedArea.length() >= minLength) {
							if (retrievedAreasArray[sequenceId] == null) {
								retrievedAreasArray[sequenceId] = Lists.newArrayList();
							}
							retrievedAreasArray[sequenceId].add(openedArea.copy());
						}
					}
				}
			}

			if (totalRemove != 0) {
				openedList.removeElements(totalRemove);
			}

			if (!merged) {
				openedList.add(queryPos, sequencePos, subSequenceLength);
			}
		}
	}

	public List<RetrievedArea>[] finish() {
		for (int sequenceId = 0; sequenceId < openedAreasArray.length; sequenceId++) {
			CircularArrayList openedAreaList = openedAreasArray[sequenceId];
			if (openedAreaList != null) {
				Iterator iterator = openedAreaList.getIterator();
				while (iterator.hasNext()) {
					RetrievedArea openedArea = iterator.next();
					assert (openedArea != null);
					if (openedArea.length() >= minLength) {
						if (retrievedAreasArray[sequenceId] == null) {
							retrievedAreasArray[sequenceId] = Lists.newArrayList();
						}
						retrievedAreasArray[sequenceId].add(openedArea);
					}
				}
			}
		}

		return retrievedAreasArray;
	}

	public List<RetrievedArea>[] getRetrievedAreasArray() {
		return retrievedAreasArray;
	}

	public final static class BothStrandSequenceAreas {
		int sequenceId;
		final DNAIndexSearcher indexSearcher;
		final DNAIndexReverseComplementSearcher reverseComplementIndexSearcher;
		final int sumLengths;
		List<RetrievedArea> areas;
		List<RetrievedArea> rcAreas;

		@SuppressWarnings("unchecked")
		public BothStrandSequenceAreas(int sequenceId, DNAIndexSearcher indexSearcher,
				DNAIndexReverseComplementSearcher reverseComplementIndexSearcher, List<RetrievedArea> areas,
				List<RetrievedArea> rcAreas) {
			this.sequenceId = sequenceId;
			this.indexSearcher = indexSearcher;
			this.reverseComplementIndexSearcher = reverseComplementIndexSearcher;
			this.areas = areas != null ? areas : Collections.EMPTY_LIST;
			this.rcAreas = rcAreas != null ? rcAreas : Collections.EMPTY_LIST;
			this.sumLengths = sumTotalLengths(this.areas) + sumTotalLengths(this.rcAreas);
		}

		private int sumTotalLengths(List<RetrievedArea> areas) {
			int total = 0;
			for (RetrievedArea area : areas) {
				total += area.length();
			}
			return total;
		}

		public List<RetrievedArea> getAreas() {
			return areas;
		}

		public List<RetrievedArea> getReverseComplementAreas() {
			return rcAreas;
		}

		public int getSumLengths() {
			return sumLengths;
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

		public DNAIndexSearcher getReverIndexSearcher() {
			return reverseComplementIndexSearcher;
		}
	}

	public final static class RetrievedArea implements Cloneable {
		private int queryAreaBegin;
		private int queryAreaEnd;
		private int sequenceAreaBegin;
		private int sequenceAreaEnd;
		private int length;

		public RetrievedArea(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
			reset(queryAreaBegin, sequenceAreaBegin, subSequenceLength);
		}

		private RetrievedArea(int queryAreaBegin, int queryAreaEnd, int sequenceAreaBegin, int sequenceAreaEnd,
				int length) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaEnd;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaEnd;
			this.length = length;
		}

		public void reset(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaBegin + subSequenceLength;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaBegin + subSequenceLength;
			this.length = subSequenceLength;
		}

		public RetrievedArea copy() {
			return new RetrievedArea(queryAreaBegin, queryAreaEnd, sequenceAreaBegin, sequenceAreaEnd, length);
		}

		public int length() {
			return this.length;
		}

		public boolean testAndSet(final int newQueryPos, final int newSequencePos, final int maxSubSequenceDistance,
				final int subSequenceLength) {

			if (Utils.isIn(queryAreaBegin, queryAreaEnd + maxSubSequenceDistance, newQueryPos)) {
				if (Utils.isIn(sequenceAreaBegin, sequenceAreaEnd + maxSubSequenceDistance, newSequencePos)) {

					int newQueryPosEnd = newQueryPos + subSequenceLength;
					if (newQueryPosEnd > this.queryAreaEnd) {
						this.queryAreaEnd = newQueryPosEnd;
					}

					int newSequencePosEnd = newSequencePos + subSequenceLength;
					if (newSequencePosEnd > this.sequenceAreaEnd) {
						this.sequenceAreaEnd = newSequencePosEnd;
					}

					this.length = Math.min(queryAreaEnd - queryAreaBegin, sequenceAreaEnd - sequenceAreaBegin);
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
			sb.append("]:");
			sb.append(length);
			sb.append(")");

			return sb.toString();
		}

		public int getQueryAreaBegin() {
			return queryAreaBegin;
		}

		public int getQueryAreaEnd() {
			return queryAreaEnd;
		}

		public int getSequenceAreaBegin() {
			return sequenceAreaBegin;
		}

		public int getSequenceAreaEnd() {
			return sequenceAreaEnd;
		}

	}
}
