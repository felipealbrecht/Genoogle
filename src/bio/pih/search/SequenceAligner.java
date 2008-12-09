package bio.pih.search;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.alignment.StringGenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.Utils;
import bio.pih.io.proto.Io.StoredSequence;
import bio.pih.search.IndexRetrievedData.RetrievedArea;
import bio.pih.search.IndexRetrievedData.SequenceRetrievedAreas;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Lists;

public class SequenceAligner implements Runnable {
	private final CountDownLatch countDown;
	private final SequenceRetrievedAreas retrievedArea;
	private final DNAIndexSearcher searcher;
	private final SearchResults sr;
	private final SearchParams sp;
	private final int queryLength;
	private final int[] encodedQuery;
	private final StoredSequence storedSequence;

	public SequenceAligner(CountDownLatch countDown, SequenceRetrievedAreas retrievedArea,
			SearchResults sr) throws IllegalSymbolException, IOException {
		this.countDown = countDown;
		this.retrievedArea = retrievedArea;
		this.sr = sr;
		this.searcher = retrievedArea.getIndexSearcher();
		this.sp = searcher.getSearchparams();
		this.queryLength = searcher.getQuery().length();
		this.encodedQuery = searcher.getEncodedQuery();
		this.storedSequence = retrievedArea.getStoredSequence();
	}

	@Override
	public void run() {
		try {
			extendAndAlignHSPs(this.queryLength, this.encodedQuery, this.retrievedArea,
					this.storedSequence);
		} catch (IllegalSymbolException e) {
			sr.addFail(e);
		} finally {
			countDown.countDown();
		}
	}

	private void extendAndAlignHSPs(int queryLength, int[] encodedQuery,
			SequenceRetrievedAreas sequenceRetrievedAreas, StoredSequence storedSequence)
			throws IllegalSymbolException {

		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int targetLength = DNASequenceEncoderToInteger.getSequenceLength(encodedSequence);

		List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();

		for (RetrievedArea retrievedArea : sequenceRetrievedAreas.getRetrievedAreas()) {
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
					sequenceAreaEnd, sp.getSequencesExtendDropoff(), searcher.getDatabank()
							.getSubSequenceLength(), searcher.getDatabank().getEncoder());

			if (extendedSequencesList.contains(extensionResult)) {
				continue;
			}
			
			extendedSequencesList.add(extensionResult);

			if (extendedSequencesList.size() > 0) {
				extendedSequencesList = mergeExtendedAreas(extendedSequencesList);
				Hit hit = alignHSPs(queryLength, storedSequence, encodedSequence, targetLength,
						extendedSequencesList);
				sr.addHit(hit);
			}
		}
	}

	private Hit alignHSPs(int queryLength, StoredSequence storedSequence, int[] encodedSequence,
			int targetLength, List<ExtendSequences> extendedSequencesList)
			throws IllegalSymbolException {

		Hit hit = new Hit(storedSequence.getName(), storedSequence.getGi(), storedSequence
				.getDescription(), storedSequence.getAccession(), SequenceEncoder
				.getSequenceLength(encodedSequence), searcher.getDatabank().getName());

		for (ExtendSequences extensionResult : extendedSequencesList) {
			StringGenoogleSmithWaterman smithWaterman = new StringGenoogleSmithWaterman(1, -3, -3,
					-3, -3);
			smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(),
					extensionResult.getTargetSequenceExtended());

			double normalizedScore = searcher.getStatistics().nominalToNormalizedScore(
					smithWaterman.getScore());
			double evalue = searcher.getStatistics().calculateEvalue(normalizedScore);
			HSP hsp = searcher.createHSP(extensionResult, smithWaterman, normalizedScore, evalue,
					queryLength, targetLength);
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
				|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
			if ((Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
					|| Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
				|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
			if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
					|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
				|| Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
			if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
					|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin,
							seq2TargetEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
				|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
			if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
				|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
			if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math
						.min(seq1QueryBegin, seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin,
						seq2TargetBegin), targetEnd, seq1.getEncoder());
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