package bio.pih.search;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.StringGenoogleSmithWaterman;
import bio.pih.encoder.DNAMaskEncoder;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.io.proto.Io.StoredSequence;
import bio.pih.search.IndexRetrievedData.RetrievedArea;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
import bio.pih.statistics.Statistics;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the
 * searchers.
 * 
 * @author albrecht
 */
public class DNASearcher extends AbstractSearcher {

	private static final Logger logger = Logger.getLogger(DNASearcher.class.getName());
	protected final IndexedDNASequenceDataBank databank;

	/**
	 * @param id
	 * @param sp
	 * @param databank
	 */
	public DNASearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank,
			ExecutorService executor) {
		super(id, sp, databank, executor);
		this.databank = databank;
	}

	String thisToString = null;
	private Statistics statistics;

	@Override
	public String toString() {
		if (thisToString == null) {
			StringBuilder sb = new StringBuilder(Integer.toString(this.hashCode()));
			sb.append("(direct) -");
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
			throw new RuntimeException("Sequence: \"" + querySequence.seqString()
					+ "\" is too short");
		}

		this.statistics = new Statistics(1, -3, querySequence, databank.getTotalDataBaseSize(),
				databank.getTotalNumberOfSequences(), sp.getMinEvalue());

		sr.setMinSubSequenceLength(this.statistics.getMinLengthDropOut());

		status.setActualStep(SearchStep.INITIALIZED);
		logger.info("[" + this.toString() + "] Begining the search at " + databank.getName()
				+ " with the sequence with " + querySequence.length() + "bases "
				+ querySequence.seqString() + " and min subSequenceLength >= "
				+ this.statistics.getMinLengthDropOut());

		int[] iess = getEncodedSubSequences(querySequence, databank.getMaskEncoder());
		int[] encodedQuery = encoder.encodeSymbolListToIntegerArray(querySequence);

		long init = System.currentTimeMillis();
		IndexRetrievedData retrievedData = getIndexPositions(iess);

		status.setActualStep(SearchStep.INDEX_SEARCH);
		List<RetrievedArea>[] sequencesRetrievedAreas = retrievedData.getRetrievedAreas();
		logger.info("[" + this.toString() + "] Index search time:"
				+ (System.currentTimeMillis() - init) + " with " + retrievedData.getTotalAreas()
				+ " areas in " + retrievedData.getTotalSequences() + " sequences.");

		status.setActualStep(SearchStep.EXTENDING);

		int threads = Math.min(32, retrievedData.getTotalSequences());
		if (threads > 0) {
			CountDownLatch countDown = new CountDownLatch(retrievedData.getTotalSequences());
									
			for (int sequenceId: retrievedData.getSequencesId()) {
				List<RetrievedArea> retrievedSequenceAreas = sequencesRetrievedAreas[sequenceId];
				if (retrievedSequenceAreas == null || retrievedSequenceAreas.size() == 0) {
					throw new NullPointerException("Was expecting sequenceId: "+ sequenceId +", but null was found.");
				}

				StoredSequence storedSequence = databank.getSequenceFromId(sequenceId);
				SequenceAligner sequenceAligner = new SequenceAligner(countDown, queryLength, encodedQuery,
						retrievedSequenceAreas, storedSequence);
				
				executor.submit(sequenceAligner);
			}
			countDown.await();
			status.setResults(sr);
		}
		logger.info("[" + this.toString() + "] Search time:" + (System.currentTimeMillis() - init)
				+ " with " + sr.getHits().size() + " hits.");
		status.setActualStep(SearchStep.FINISHED);
	}

	private IndexRetrievedData getIndexPositions(int[] iess) throws ValueOutOfBoundsException,
			IOException, InvalidHeaderData {

		int subSequenceLength = databank.getMaskEncoder() == null ? databank.getSubSequenceLength()
				: databank.getMaskEncoder().getPatternLength();
		IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getNumberOfSequences(),
				sp, statistics.getMinLengthDropOut(), subSequenceLength);

		status.setActualStep(SearchStep.INDEX_SEARCH);
		for (int ss = 0; ss < iess.length; ss++) {
			retrieveIndexPosition(iess[ss], retrievedData, ss);
		}
		return retrievedData;
	}

	boolean useSimilarSubSequences = false;

	private void retrieveIndexPosition(int encodedSubSequence, IndexRetrievedData retrievedData,
			int queryPos) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {

		if (useSimilarSubSequences) {
			List<Integer> similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);
			for (Integer similarSubSequence : similarSubSequences) {
				long[] indexPositions = databank.getMatchingSubSequence(similarSubSequence);
				for (long subSequenceIndexInfo : indexPositions) {
					retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
				}
			}
		} else {
			long[] indexPositions = databank.getMatchingSubSequence(encodedSubSequence);
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

	private int[] getEncodedSubSequences(SymbolList querySequence, DNAMaskEncoder maskEncoder) {
		if (maskEncoder == null) {
			return getEncodedSubSequences(querySequence);
		}

		int[] iess = new int[querySequence.length() - (maskEncoder.getPatternLength() - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getOverlappedFactory().newSymbolListWindowIterator(querySequence,
						maskEncoder.getPatternLength());
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			iess[pos] = maskEncoder.applyMask(symbolListWindowIterator.next());
		}
		return iess;
	}

	private class SequenceAligner implements Runnable {
		private final int queryLength;
		private final int[] encodedQuery;
		private final List<RetrievedArea> retrievedSequenceAreas;
		private final StoredSequence storedSequence;
		private final CountDownLatch countDown;

		public SequenceAligner(CountDownLatch countDown, int queryLength, int[] encodedQuery,
				List<RetrievedArea> retrievedSequenceAreas, StoredSequence storedSequence)
				throws IllegalSymbolException {
			this.countDown = countDown;
			this.queryLength = queryLength;
			this.encodedQuery = encodedQuery;
			this.retrievedSequenceAreas = retrievedSequenceAreas;
			this.storedSequence = storedSequence;
		}

		@Override
		public void run() {
			try {
				extendAndAlignHSPs(this.queryLength, this.encodedQuery,
						this.retrievedSequenceAreas, this.storedSequence);
			} catch (IllegalSymbolException e) {
				sr.addFail(e);
			} finally {
				countDown.countDown();
			}
		}

		private void extendAndAlignHSPs(int queryLength, int[] encodedQuery,
				List<RetrievedArea> retrievedSequenceAreas, StoredSequence storedSequence)
				throws IllegalSymbolException {

			int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
			int targetLength = DNASequenceEncoderToInteger.getSequenceLength(encodedSequence);
			
			List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();
			
			for (RetrievedArea retrievedArea : retrievedSequenceAreas) {
				
				int sequenceAreaBegin = retrievedArea.sequenceAreaBegin;
				int sequenceAreaEnd = retrievedArea.sequenceAreaEnd;
				if (sequenceAreaEnd > targetLength) {
					sequenceAreaEnd = targetLength;
				}
				int queryAreaBegin = retrievedArea.queryAreaBegin;
				int queryAreaEnd = retrievedArea.queryAreaEnd;
				if (queryAreaEnd > queryLength) {
					queryAreaBegin = queryLength;
				}

				ExtendSequences extensionResult = ExtendSequences.doExtension(encodedQuery,
						queryAreaBegin, queryAreaEnd, encodedSequence, sequenceAreaBegin,
						sequenceAreaEnd, sp.getSequencesExtendDropoff(), databank
								.getSubSequenceLength(), encoder);

				if (!extendedSequencesList.contains(extensionResult)) {
					extendedSequencesList.add(extensionResult);
				}
			}

			if (extendedSequencesList.size() > 0) {
				extendedSequencesList = mergeExtendedAreas(extendedSequencesList);
				Hit hit = alignHSPs(queryLength, storedSequence, encodedSequence, targetLength,
						extendedSequencesList);
				sr.addHit(hit);
			}
		}

		private Hit alignHSPs(int queryLength, StoredSequence storedSequence,
				int[] encodedSequence, int targetLength, List<ExtendSequences> extendedSequencesList)
				throws IllegalSymbolException {
			
			Hit hit = new Hit(storedSequence.getName(), storedSequence.getGi(), 
					storedSequence.getDescription(), storedSequence.getAccession(), 
					SequenceEncoder.getSequenceLength(encodedSequence), databank.getName());
			
			for (ExtendSequences extensionResult : extendedSequencesList) {
				StringGenoogleSmithWaterman smithWaterman = new StringGenoogleSmithWaterman(1, -3, -3, -3, -3);
				smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended().seqString(),
						extensionResult.getTargetSequenceExtended().seqString());

				double normalizedScore = statistics.nominalToNormalizedScore(smithWaterman
						.getScore());
				double evalue = statistics.calculateEvalue(normalizedScore);
				HSP hsp = createHSP(extensionResult, smithWaterman, normalizedScore, evalue, queryLength,
						targetLength);
				hit.addHSP(hsp);
			}
			return hit;
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

			if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
					|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin,
							seq2TargetEnd)) {
				if ((Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
						|| Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(),
							Math.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(
									seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
				}
			}

			if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
						|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(),
							Math.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(
									seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
				}
			}

			if ((Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
					|| Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
				if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
						|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin,
								seq2TargetEnd)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(),
							Math.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(
									seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
				}
			}

			if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
					|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
						|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin,
								seq1QueryEnd)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(),
							Math.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(
									seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
				}
			}

			if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
					|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin,
							seq2TargetEnd)) {
				if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
						|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin,
								seq1QueryEnd)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(),
							Math.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(
									seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
				}
			}

			if (Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin)) {
				if (Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(),
							seq1QueryBegin, queryEnd, seq1TargetBegin, targetEnd, seq1.getEncoder());
				}
			}

			if (Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
				if (Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
					return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(),
							seq2QueryBegin, queryEnd, seq2TargetBegin, targetEnd, seq1.getEncoder());
				}
			}

			return null;
		}

	}

}