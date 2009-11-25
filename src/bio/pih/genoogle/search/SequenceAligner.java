package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.genoogle.alignment.DividedStringGenoogleSmithWaterman;
import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.io.Utils;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.search.results.Hit;
import bio.pih.genoogle.search.results.SearchResults;

import com.google.common.collect.Lists;

public class SequenceAligner implements Runnable {
	private final CountDownLatch countDown;
	private final BothStrandSequenceAreas retrievedArea;
	private final SearchResults sr;
	private final StoredSequence storedSequence;

	public SequenceAligner(CountDownLatch countDown, BothStrandSequenceAreas retrievedArea, SearchResults sr)
			throws IllegalSymbolException, IOException {
		this.countDown = countDown;
		this.retrievedArea = retrievedArea;
		this.sr = sr;
		this.storedSequence = retrievedArea.getStoredSequence();
	}

	@Override
	public void run() {
		try {
			extendAndAlignHSPs(this.retrievedArea, this.storedSequence);
		} catch (Exception e) {
			sr.addFail(e);
		} catch (AssertionError ae) {
			ae.printStackTrace();
		} finally {
			countDown.countDown();
		}
	}

	private void extendAndAlignHSPs(BothStrandSequenceAreas retrievedAreas, StoredSequence storedSequence)
			throws Exception {

		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int targetLength = DNASequenceEncoderToInteger.getSequenceLength(encodedSequence);

		DNAIndexSearcher searcher = retrievedAreas.getIndexSearcher();
		int queryLength = searcher.getQuery().length();

		Hit hit = new Hit(storedSequence.getName(), storedSequence.getGi(), storedSequence.getDescription(), storedSequence.getAccession(), targetLength, searcher.getDatabank().getName());

		List<RetrievedArea> areas = retrievedAreas.getAreas();
		if (areas.size() > 0) {
			int[] encodedQuery = searcher.getEncodedQuery();
			List<ExtendSequences> extendedSequences = extendAreas(encodedSequence, targetLength, queryLength,
					encodedQuery, areas, searcher);
			extendedSequences = mergeExtendedAreas(extendedSequences);
			alignHSPs(hit, queryLength, storedSequence, encodedSequence, targetLength, extendedSequences, searcher);
		}

		List<RetrievedArea> reverseComplementAreas = retrievedAreas.getReverseComplementAreas();
		if (reverseComplementAreas.size() > 0) {
			int[] reverseEncodedQuery = retrievedAreas.getReverIndexSearcher().getEncodedQuery();
			DNAIndexSearcher rcSearcher = retrievedAreas.getReverIndexSearcher();
			List<ExtendSequences> rcExtendedSequences = extendAreas(encodedSequence, targetLength, queryLength,
					reverseEncodedQuery, reverseComplementAreas, rcSearcher);
			rcExtendedSequences = mergeExtendedAreas(rcExtendedSequences);
			alignHSPs(hit, queryLength, storedSequence, encodedSequence, targetLength, rcExtendedSequences, rcSearcher);
		}

		sr.addHit(hit);
	}

	private List<ExtendSequences> extendAreas(int[] encodedSequence, int targetLength, int queryLength,
			int[] encodedQuery, List<RetrievedArea> areas, DNAIndexSearcher searcher) {
		List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();
		for (int i = 0; i < areas.size(); i++) {
			RetrievedArea retrievedArea = areas.get(i);
			int sequenceAreaBegin = retrievedArea.getSequenceAreaBegin();
			int sequenceAreaEnd = retrievedArea.getSequenceAreaEnd();
			if (sequenceAreaEnd > targetLength) {
				sequenceAreaEnd = targetLength;
			}
			int queryAreaBegin = retrievedArea.getQueryAreaBegin();
			int queryAreaEnd = retrievedArea.getQueryAreaEnd();
			if (queryAreaEnd > queryLength) {
				queryAreaBegin = queryLength;
			}

			ExtendSequences extensionResult = ExtendSequences.doExtension(encodedQuery, queryAreaBegin, queryAreaEnd,
					encodedSequence, sequenceAreaBegin, sequenceAreaEnd,
					searcher.getSearchParams().getSequencesExtendDropoff(),
					searcher.getDatabank().getSubSequenceLength(), searcher.getDatabank().getEncoder());

			if (extendedSequencesList.contains(extensionResult)) {
				continue;
			}

			extendedSequencesList.add(extensionResult);
		}
		return extendedSequencesList;
	}

	private void alignHSPs(Hit hit, int queryLength, StoredSequence storedSequence, int[] encodedSequence,
			int targetLength, List<ExtendSequences> extendedSequencesList, DNAIndexSearcher searcher)
			throws IllegalSymbolException {

		for (ExtendSequences extensionResult : extendedSequencesList) {
			int matchScore = sr.getParams().getMatchScore();
			int mismatchScore = sr.getParams().getMismatchScore();
			DividedStringGenoogleSmithWaterman smithWaterman = new DividedStringGenoogleSmithWaterman(matchScore, mismatchScore, mismatchScore, mismatchScore, mismatchScore, 2000);

			smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(),
					extensionResult.getTargetSequenceExtended());

			double normalizedScore = searcher.getStatistics().nominalToNormalizedScore(smithWaterman.getScore());
			double evalue = searcher.getStatistics().calculateEvalue(normalizedScore);
			HSP hsp = searcher.createHSP(extensionResult, smithWaterman, normalizedScore, evalue, queryLength,
					targetLength);
			hit.addHSP(hsp);
		}
	}

	private List<ExtendSequences> mergeExtendedAreas(List<ExtendSequences> extendedSequences) {
		ListIterator<ExtendSequences> iterator1 = extendedSequences.listIterator();
		while (iterator1.hasNext()) {
			ExtendSequences extSeqs1 = iterator1.next();
			ListIterator<ExtendSequences> iterator2 = extendedSequences.listIterator(iterator1.nextIndex());
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
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math.min(seq1QueryBegin,
						seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
				|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
			if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
					|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math.min(seq1QueryBegin,
						seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin))
				|| Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
			if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
					|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math.min(seq1QueryBegin,
						seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if ((Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin))
				|| Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
			if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math.min(seq1QueryBegin,
						seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.contains(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin, seq1TargetEnd)
				|| Utils.contains(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin, seq2TargetEnd)) {
			if (Utils.contains(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin, seq2QueryEnd)
					|| Utils.contains(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin, seq1QueryEnd)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq2.getEncodedTarget(), Math.min(seq1QueryBegin,
						seq2QueryBegin), queryEnd, Math.min(seq1TargetBegin, seq2TargetBegin), targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.isIn(seq1QueryBegin, seq1QueryEnd, seq2QueryBegin)) {
			if (Utils.isIn(seq1TargetBegin, seq1TargetEnd, seq2TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(), seq1QueryBegin, queryEnd, seq1TargetBegin, targetEnd, seq1.getEncoder());
			}
		}

		if (Utils.isIn(seq2QueryBegin, seq2QueryEnd, seq1QueryBegin)) {
			if (Utils.isIn(seq2TargetBegin, seq2TargetEnd, seq1TargetBegin)) {
				return new ExtendSequences(seq1.getEncodedQuery(), seq1.getEncodedTarget(), seq2QueryBegin, queryEnd, seq2TargetBegin, targetEnd, seq1.getEncoder());
			}
		}

		return null;
	}

}