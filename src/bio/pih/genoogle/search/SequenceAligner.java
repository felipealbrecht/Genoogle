/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CountDownLatch;

import pih.bio.genoogle.seq.protein.Converter;
import bio.pih.genoogle.alignment.DividedStringGenoogleSmithWaterman;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.Utils;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.search.results.Hit;
import bio.pih.genoogle.search.results.SearchResults;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;

import com.google.common.collect.Lists;

/**
 * Class responsible to extend and align the HSPs.
 * 
 * @author albrecht
 */
public class SequenceAligner implements Runnable {
	private final CountDownLatch countDown;
	private final BothStrandSequenceAreas retrievedAreas;
	private final SearchResults sr;
	private final StoredSequence storedSequence;
	private final SequenceEncoder encoderExtension;
	private final SequenceEncoder encoderAlignment;
	private final SequenceEncoder encoderDatabank;
	private final AbstractSequenceDataBank databank;

	/**
	 * @param countDown
	 *            Synchronizer use to wait until all HSPs from all Sub sub banks are extended and
	 *            aligned.
	 * @param retrievedAreas
	 *            retrievedAre which the HSPs that will be extended and retrieved.
	 * @param sr
	 *            Where the results are stored.
	 */
	public SequenceAligner(CountDownLatch countDown, BothStrandSequenceAreas retrievedAreas, SearchResults sr, AbstractSequenceDataBank databank)
			throws IOException {
		this(countDown, retrievedAreas, sr, databank, databank.getEncoder(), databank.getEncoder(), databank.getEncoder());
	}
	
	public SequenceAligner(CountDownLatch countDown, BothStrandSequenceAreas retrievedAreas, SearchResults sr, AbstractSequenceDataBank databank, 
			SequenceEncoder encoderDatabank, SequenceEncoder encoderExtension, SequenceEncoder encoderAlignment)
	throws IOException {
		this.countDown = countDown;
		this.retrievedAreas = retrievedAreas;
		this.sr = sr;
		this.databank = databank;
		this.storedSequence = retrievedAreas.getStoredSequence();
		this.encoderDatabank = encoderDatabank;
		this.encoderExtension = encoderExtension;
		this.encoderAlignment = encoderAlignment;
}

	@Override
	public void run() {
		try {
			extendAndAlignHSPs(this.retrievedAreas, this.storedSequence);
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

		int[] encodedDatabankSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int targetLength = SequenceEncoder.getSequenceLength(encodedDatabankSequence);

		
		int[] convertedDatabankSequence = null;
		int[] reducedDatabankSequence = null;
		SymbolList converted = null;
		
		if (encoderDatabank != encoderExtension || encoderDatabank != encoderAlignment) {
			String databankSequence = encoderDatabank.decodeIntegerArrayToString(encodedDatabankSequence);			
			converted = Converter.dnaToProtein(LightweightSymbolList.createProtein(databankSequence));
			convertedDatabankSequence = encoderAlignment.encodeSymbolListToIntegerArray(converted);
			SymbolList reduced = Converter.proteinToReducedAA(converted);
			reducedDatabankSequence = encoderExtension.encodeSymbolListToIntegerArray(reduced);			
		} else {
			 convertedDatabankSequence = encodedDatabankSequence;
			 reducedDatabankSequence = encodedDatabankSequence;
			 String databankSequence = encoderDatabank.decodeIntegerArrayToString(encodedDatabankSequence);
			 converted = new LightweightSymbolList(databank.getAlphabet(),databankSequence);
		}
		
		
		IndexSearcher searcher = retrievedAreas.getIndexSearcher();
		int queryLength = searcher.getQuery().getLength();

		Hit hit = new Hit(storedSequence.getName(), storedSequence.getGi(), storedSequence.getDescription(), storedSequence.getAccession(), targetLength, databank.getAbsolutParent().getName());

		List<RetrievedArea> areas = retrievedAreas.getAreas();
		if (areas.size() > 0) {
			int[] encodedQuery = searcher.getEncodedQuery();
			List<ExtendSequences> extendedSequences = extendAreas(reducedDatabankSequence, targetLength, queryLength,
					encodedQuery, areas, searcher);
			extendedSequences = mergeExtendedAreas(extendedSequences);
			alignHSPs(hit, queryLength, storedSequence, targetLength, extendedSequences, searcher);
		}

		List<RetrievedArea> reverseComplementAreas = retrievedAreas.getReverseComplementAreas();
		if (reverseComplementAreas.size() > 0) {
			int[] reverseEncodedQuery = retrievedAreas.getReverIndexSearcher().getEncodedQuery();
			IndexSearcher rcSearcher = retrievedAreas.getReverIndexSearcher();
			List<ExtendSequences> rcExtendedSequences = extendAreas(encodedDatabankSequence, targetLength, queryLength,
					reverseEncodedQuery, reverseComplementAreas, rcSearcher);
			rcExtendedSequences = mergeExtendedAreas(rcExtendedSequences);
			alignHSPs(hit, queryLength, storedSequence, targetLength, rcExtendedSequences, rcSearcher);
		}

		sr.addHit(hit);
	}

	private List<ExtendSequences> extendAreas(int[] encodedSequence, int targetLength, int queryLength,
			int[] encodedQuery, List<RetrievedArea> areas, IndexSearcher searcher) {
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
					searcher.getSearchParams().getSequencesExtendDropoff(), encoderExtension, encoderAlignment);

			if (extendedSequencesList.contains(extensionResult)) {
				continue;
			}

			extendedSequencesList.add(extensionResult);
		}
		return extendedSequencesList;
	}

	private void alignHSPs(Hit hit, int queryLength, StoredSequence storedSequence,
			int targetLength, List<ExtendSequences> extendedSequencesList, IndexSearcher searcher) {

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

	/**
	 * Check if the extended areas has overlapped positions and merge them.
	 * 
	 * @param extendedSequences
	 * @return {@link List} of {@link ExtendSequences} that are merged when they have overlapped
	 *         areas.
	 */
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

	/**
	 * Check if the {@link ExtendSequences} seq1 and seq2 are overlapped.
	 * 
	 * @param seq1
	 *            an {@link ExtendSequences}
	 * @param seq2
	 *            an {@link ExtendSequences}
	 * @return a merged {@link ExtendSequences} or <code>null</code> if was not merged.
	 */
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