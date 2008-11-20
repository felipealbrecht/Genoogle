package bio.pih.search;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.proto.Io.StoredSequence;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
import bio.pih.statistics.Statistics;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the
 * searchers.
 * 
 * @author albrecht
 */
public class DNASearcher extends AbstractSearcher {

	private static final Logger logger = Logger.getLogger(DNASearcher.class.getName());

	private static SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(
			DNATools.getDNA(), 1, -3);

	protected final IndexedDNASequenceDataBank databank;

	/**
	 * @param id
	 * @param sp
	 * @param databank
	 */
	public DNASearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank) {
		super(id, sp, databank);
		this.databank = databank;
	}

	String thisToString = null;
	private Statistics statitics;

	@Override
	public String toString() {
		if (thisToString == null) {
			StringBuilder sb = new StringBuilder(Integer.toString(this.hashCode()));
			sb.append("-");
			sb.append(databank.toString());
			thisToString = sb.toString();
		}
		return thisToString;
	}

	@Override
	public SearchResults call() throws Exception {
		try {
			doSearch();
		} catch (Exception e) {
			sr.addFail(e);
			status.setResults(sr);
			status.setActualStep(SearchStep.FATAL_ERROR);
		}
		return sr;
	}

	protected void doSearch() throws Exception {
		SymbolList querySequence = getQuery();
		int queryLength = querySequence.length();
		if (queryLength < databank.getSubSequenceLength()) {
			throw new RuntimeException("Sequence: \"" + querySequence.seqString() +"\" is too short");
		}
		
		this.statitics = new Statistics(1, -3, querySequence, databank.getTotalDataBaseSize(),
				databank.getTotalNumberOfSequences(), sp.getMinEvalue());

		status.setActualStep(SearchStep.INITIALIZED);
		logger.info("[" + this.toString() + "] Begining the search at " + databank.getName()
				+ " with the sequence with " + querySequence.length() + "bases "
				+ querySequence.seqString());

		int[] iess = getEncodedSubSequences(querySequence);
		int[] encodedQuery = encoder.encodeSymbolListToIntegerArray(querySequence);
		int threshould = sp.getMinSimilarity();

		long init = System.currentTimeMillis();
		IndexRetrievedData retrievedData = getIndexPositions(iess, threshould);

		logger.info("[" + this.toString() + "] Index search time:"
				+ (System.currentTimeMillis() - init));
		status.setActualStep(SearchStep.INDEX_SEARCH);
		List<RetrievedArea>[] sequencesRetrievedAreas = retrievedData.getRetrievedAreas();

		status.setActualStep(SearchStep.EXTENDING);
		int hitNum = 0;
		for (int sequenceId = 0; sequenceId < sequencesRetrievedAreas.length; sequenceId++) {
			List<RetrievedArea> retrievedSequenceAreas = sequencesRetrievedAreas[sequenceId];
			if (retrievedSequenceAreas == null || retrievedSequenceAreas.size() == 0) {
				continue;
			}

			StoredSequence storedSequence = databank.getSequenceFromId(sequenceId);
			ByteString encodedByteSequence = storedSequence.getEncodedSequence();
			byte[] byteArray = encodedByteSequence.toByteArray();
			int[] encodedSequence = new int[byteArray.length / 4];
			ByteBuffer.wrap(byteArray).asIntBuffer().get(encodedSequence);

			int hspNum = 0;
			List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();
			for (RetrievedArea retrievedArea : retrievedSequenceAreas) {
				int sequenceAreaBegin = retrievedArea.sequenceAreaBegin;
				int sequenceAreaEnd = retrievedArea.sequenceAreaEnd;
				if (sequenceAreaEnd > DNASequenceEncoderToInteger
						.getSequenceLength(encodedSequence)) {
					sequenceAreaEnd = DNASequenceEncoderToInteger
							.getSequenceLength(encodedSequence);
				}
				int queryAreaBegin = retrievedArea.queryAreaBegin;
				int queryAreaEnd = retrievedArea.queryAreaEnd;
				if (queryAreaEnd > querySequence.length()) {
					queryAreaBegin = querySequence.length();
				}

				ExtendSequences extensionResult = ExtendSequences.doExtension(encodedQuery,
						queryAreaBegin, queryAreaEnd, encodedSequence, sequenceAreaBegin,
						sequenceAreaEnd, sp.getSequencesExtendDropoff(), subSequenceLegth, encoder);

				if (!extendedSequencesList.contains(extensionResult)) {
					extendedSequencesList.add(extensionResult);
				}

			}

			extendedSequencesList = mergeExtendedAreas(extendedSequencesList);

			status.setActualStep(SearchStep.ALIGNMENT);
			if (extendedSequencesList.size() > 0) {
				Hit hit = new Hit(hitNum++, storedSequence.getName(), storedSequence.getGi(),
						storedSequence.getDescription(), storedSequence.getAccession(),
						SequenceEncoder.getSequenceLength(encodedSequence), databank.getName());
				for (ExtendSequences extensionResult : extendedSequencesList) {
					GenoogleSmithWaterman smithWaterman = new GenoogleSmithWaterman(-1, 3, 3, 3, 3,
							substitutionMatrix);
					smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(),
							extensionResult.getTargetSequenceExtended());

					double normalizedScore = statitics.nominalToNormalizedScore(smithWaterman
							.getScore());
					double evalue = statitics.calculateEvalue(normalizedScore);
					addHit(hspNum, hit, extensionResult, smithWaterman, normalizedScore,
							evalue, queryLength);
				}
				sr.addHit(hit);
			}
		}

		status.setActualStep(SearchStep.SELECTING);

		Collections.sort(sr.getHits(), Hit.COMPARATOR);

		status.setResults(sr);
		logger.info("[" + this.toString() + "] Search time:" + (System.currentTimeMillis() - init));
		status.setActualStep(SearchStep.FINISHED);
	}

	private List<ExtendSequences> mergeExtendedAreas(List<ExtendSequences> extendedSequences) {
		ListIterator<ExtendSequences> iterator1 = extendedSequences.listIterator();
		while (iterator1.hasNext()) {
			ExtendSequences extSeqs1 = iterator1.next();
			ListIterator<ExtendSequences> iterator2 = extendedSequences.listIterator(iterator1
					.nextIndex());
			while (iterator2.hasNext()) {
				ExtendSequences extSeqs2 = iterator2.next();
				ExtendSequences merged = tryToMerge(extSeqs1, extSeqs2);
				if (merged != null) {
					extendedSequences.remove(extSeqs1);
					extendedSequences.remove(extSeqs2);
					extendedSequences.add(merged);
					return mergeExtendedAreas(extendedSequences);
				}
			}
		}
		return extendedSequences;
	}

	private ExtendSequences tryToMerge(ExtendSequences seq1, ExtendSequences seq2) {
		int seq1QueryBegin = seq1.getBeginQuerySegment();
		int seq1QueryEnd = seq1.getEndQuerySegment();
		int seq1TargetBegin = seq1.getBeginTargetSegment();
		int seq1TargetEnd = seq1.getEndTargetSegment();

		int seq2QueryBegin = seq2.getBeginQuerySegment();
		int seq2QueryEnd = seq2.getEndQuerySegment();
		int seq2TargetBegin = seq2.getBeginTargetSegment();
		int seq2TargetEnd = seq2.getEndTargetSegment();

		int queryEnd = Math.max(seq1QueryEnd, seq2QueryEnd);
		int targetEnd = Math.max(seq1TargetEnd, seq2TargetEnd);

		if (contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
				|| contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
			if ((isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
					|| isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
				|| contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
			if ((isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
					|| isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
				|| isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
			if (contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
					|| contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
				|| isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
			if (contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
				|| contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
			if (contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin)) {
			if (isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(),
						seq1QueryBegin, queryEnd, seq1TargetBegin, targetEnd, seq1.getEncoder());
			}
		}

		if (isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
			if (isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(),
						seq2QueryBegin, queryEnd, seq2TargetBegin, targetEnd, seq1.getEncoder());
			}
		}

		return null;
	}

	private IndexRetrievedData getIndexPositions(int[] iess, int threshould)
			throws ValueOutOfBoundsException, IOException, InvalidHeaderData {

		IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getNumberOfSequences(),
				sp, statitics.getMinLengthDropOut(), databank.getSubSequenceLength());

		status.setActualStep(SearchStep.INDEX_SEARCH);
		for (int ss = 0; ss < iess.length; ss++) {
			retrieveIndexPosition(iess[ss], threshould, retrievedData, ss);
		}
		return retrievedData;
	}

	private void retrieveIndexPosition(int encodedSubSequence, int threshould,
			IndexRetrievedData retrievedData, int queryPos) throws ValueOutOfBoundsException,
			IOException, InvalidHeaderData {

		List<Integer> similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);
		for (Integer similarSubSequence : similarSubSequences) {
			long[] indexPositions = databank.getMachingSubSequence(similarSubSequence);
			for (long subSequenceIndexInfo : indexPositions) {
				retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
			}
		}
	}

	private int[] getEncodedSubSequences(SymbolList querySequence) {
		int[] iess = new int[querySequence.length() - (subSequenceLegth - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getOverlappedFactory()
				.newSymbolListWindowIterator(querySequence, subSequenceLegth);
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			SymbolList subSequence = symbolListWindowIterator.next();
			iess[pos] = encoder.encodeSubSymbolListToInteger(subSequence);
		}
		return iess;
	}

	private static boolean isIn(int begin, int end, int pos) {
		if ((pos >= begin) && (pos <= end)) {
			return true;
		}
		return false;
	}

	private static boolean contains(int seq1Begin, int seq1End, int seq2Begin, int seq2End) {
		if ((seq2Begin >= seq1Begin) && (seq2End <= seq1End)) {
			return true;
		}
		return false;
	}

	private static final class IndexRetrievedData {
				
		private final List<RetrievedArea>[] retrievedAreasArray;
		private final FuckingArrayList<RetrievedArea>[] openedAreasArray;
		private final SearchParams sp;
		private final int minLength;
		private final int subSequenceLength;

		@SuppressWarnings("unchecked")
		public IndexRetrievedData(int size, SearchParams sp, int minLength, int subSequenceLength) {
			this.sp = sp;
			this.minLength = minLength;
			this.subSequenceLength = subSequenceLength;
			retrievedAreasArray = new List[size];
			openedAreasArray = new FuckingArrayList[size];
			for (int i = 0; i < size; i++) {
				openedAreasArray[i] = new FuckingArrayList();
			}
		}

		void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
			int start = EncoderSubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
			int sequenceId = EncoderSubSequenceIndexInfo
					.getSequenceId(subSequenceInfoIntRepresention);

			mergeOrRemoveOrNew(queryPos, start, sequenceId);
		}

		private void mergeOrRemoveOrNew(int queryPos, int sequencePos, int sequenceId) {
			boolean merged = false;
			FuckingArrayList<RetrievedArea> openedList = openedAreasArray[sequenceId];
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
			for (int i = 0; i < retrievedAreasArray.length; i++) {
				if (retrievedAreasArray[i] != null) {
					totalAreas += retrievedAreasArray[i].size();
				}
			}

			logger.info("[" + this.toString() + "] TotalAreas: " + totalAreas);

			return retrievedAreasArray;
		}
	}

	private final static class RetrievedArea {
		int queryAreaBegin;
		int queryAreaEnd;
		int sequenceAreaBegin;
		int sequenceAreaEnd;
		int length;

		public RetrievedArea(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaBegin + subSequenceLength - 1;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaBegin + subSequenceLength - 1;
			this.length = subSequenceLength;
		}

		public int length() {
			return this.length;
		}

		public boolean setTestAndSet(int newQueryPos, int newSequencePos,
				int maxSubSequenceDistance, int subSequenceLength) {

			if (isIn(queryAreaBegin, queryAreaEnd, newQueryPos)
					|| (queryAreaEnd + maxSubSequenceDistance > newQueryPos)) {

				if (isIn(sequenceAreaBegin, sequenceAreaEnd, newSequencePos)
						|| (sequenceAreaEnd + maxSubSequenceDistance > newSequencePos)) {

					this.queryAreaEnd = newQueryPos + (subSequenceLength - 1);
					this.sequenceAreaEnd = newSequencePos + (subSequenceLength - 1);
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

		public FuckingArrayList() { }
		
		public FuckingArrayList(int i) {
			super(i);
		}
		
		@Override
		public void removeRange(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}
}