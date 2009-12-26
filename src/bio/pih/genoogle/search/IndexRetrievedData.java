/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.Utils;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.util.CircularArrayList;
import bio.pih.genoogle.util.CircularArrayList.Iterator;

import com.google.common.collect.Lists;

/**
 * Stores the data retrieved from the Inverted Index.
 * 
 * @author albrecht
 */
public class IndexRetrievedData {

	private final List<RetrievedArea>[] retrievedAreasArray;
	private final CircularArrayList[] openedAreasArray;
	private final int minLength;
	private final int subSequenceLength;
	private final int maxSubSequenceDistance;

	/**
	 * Constructor.
	 * @param size Quantity of sequences stored in the data bank.
	 * @param sp Search parameters.
	 * @param subSequenceLength Sub sequences length.
	 * @param searcher Index searcher that is used.
	 */
	@SuppressWarnings("unchecked")
	public IndexRetrievedData(int size, SearchParams sp, int subSequenceLength, IndexSearcher searcher) {

		this.minLength = sp.getMinHspLength();
		this.subSequenceLength = subSequenceLength;
		this.maxSubSequenceDistance = sp.getMaxSubSequencesDistance();

		retrievedAreasArray = new List[size];
		openedAreasArray = new CircularArrayList[size];
	}

	/**
	 * Insert a found subSequences and check if it will be merged or added as a new area.
	 * @param queryPos sub-sequence position in the query.
	 * @param subSequenceInfoIntRepresention representation of the sub sequence by {@link SubSequenceIndexInfo}.
	 */
	final void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
		int sequencePos = SubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
		int sequenceId = SubSequenceIndexInfo.getSequenceId(subSequenceInfoIntRepresention);

		mergeOrRemoveOrNew(queryPos, sequencePos, sequenceId);
	}

	/**
	 * Merge the subsequence or create a new retrieved area. 
	 * @param queryPos Position in the query.
	 * @param sequencePos Position in the data bank sequence.
	 * @param sequenceId Data bank sequence id.
	 */
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

	/**
	 * Finish the index searching process. It will close all retrieved areas and it will check if the areas has the minumun length.
	 * @return all {@link RetrievedArea} that has at least the minimum length.
	 */
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

	/**
	 * Get the retrieved areas.
	 * @return all {@link RetrievedArea} that has at least the minimum length.
	 */
	public List<RetrievedArea>[] getRetrievedAreasArray() {
		return retrievedAreasArray;
	}

	public final static class BothStrandSequenceAreas {
		int sequenceId;
		final IndexSearcher indexSearcher;
		final IndexReverseComplementSearcher reverseComplementIndexSearcher;
		final int biggestHspLength;
		List<RetrievedArea> areas;
		List<RetrievedArea> rcAreas;

		@SuppressWarnings("unchecked")
		public BothStrandSequenceAreas(int sequenceId, IndexSearcher indexSearcher,
				IndexReverseComplementSearcher reverseComplementIndexSearcher, List<RetrievedArea> areas,
				List<RetrievedArea> rcAreas) {
			this.sequenceId = sequenceId;
			this.indexSearcher = indexSearcher;
			this.reverseComplementIndexSearcher = reverseComplementIndexSearcher;
			this.areas = areas.size() > 0 ? areas : Collections.EMPTY_LIST;
			this.rcAreas = rcAreas.size() > 0 ? rcAreas : Collections.EMPTY_LIST;
			this.biggestHspLength = Math.max( getBiggestHspLength(this.areas), getBiggestHspLength(this.rcAreas));
		}

		private int getBiggestHspLength(List<RetrievedArea> areas) {
			int biggest = 0;
			for (RetrievedArea area : areas) {
				if (area.length() > biggest) {
					biggest = area.length();
				}
			}
			return biggest;
		}

		public List<RetrievedArea> getAreas() {
			return areas;
		}

		public List<RetrievedArea> getReverseComplementAreas() {
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

	/**
	 * HSP
	 * 
	 * @author albrecht
	 */
	public final static class RetrievedArea {
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
