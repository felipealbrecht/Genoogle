package bio.pih.search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.SequenceInformation;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
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

	Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * @param id
	 * @param sp
	 * @param bank
	 * @param sm
	 * @param parent
	 */
	public DNASearcher(long id, SearchParams sp, SequenceDataBank bank, SearchManager sm,
			Searcher parent) {
		super(id, sp, bank, sm, parent);
		ss = new IndexedDatabankSimilarSearcher(sp, (IndexedSequenceDataBank) bank);
		ss.setName("DNASearcher on " + bank.getName());
	}

	@Override
	public void doSearch() {
		ss.start();
	}

	protected class IndexedDatabankSimilarSearcher extends Thread {
		private final IndexedSequenceDataBank databank;
		private final SearchParams sp;

		/**
		 * Constructor for the inner class that construct a searcher to find sequences that are similar
		 * with the sequence into databank.
		 * 
		 * @param sp
		 * @param databank
		 */
		public IndexedDatabankSimilarSearcher(SearchParams sp, IndexedSequenceDataBank databank) {
			this.sp = sp;
			this.databank = databank;
		}

		@Override
		public void run() {
			SymbolList querySequence = sp.getQuery();

			status.setActualStep(SearchStep.INITIALIZED);

			logger.info("Begining the search of sequence with " + querySequence.length() + "bases "
					+ querySequence.seqString());

			short[] iess = getEncodedSubSequences(querySequence);
			int threshould = sp.getMinSimilarity();

			long init = System.currentTimeMillis();
			IndexRetrievedData retrievedData = getIndexPositions(iess, threshould);

			logger.info("Search total time:" + (System.currentTimeMillis() - init));
			status.setActualStep(SearchStep.COMPUTING_MATCHS);
			Stack<RetrievedArea>[] sequencesRetrievedAreas = retrievedData.getRetrievedAreas();
			logger.info("sequencesHits: " + sequencesRetrievedAreas.length);

			SearchResults sr = new SearchResults(sp);

			int hitNum = 0;
			for (int sequenceId = 0; sequenceId < sequencesRetrievedAreas.length; sequenceId++) {
				Stack<RetrievedArea> retrievedSequenceAreas = sequencesRetrievedAreas[sequenceId];
				SequenceInformation sequenceInformation = null;
				SymbolList hitSequence = null;

				try {
					sequenceInformation = databank.getSequenceInformationFromId(sequenceId);
					hitSequence = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToSymbolList(
							sequenceInformation.getEncodedSequence());
				} catch (Exception e) {
					logger.fatal("Fatar error while loading sequence " + sequenceId + " from datatabank "
							+ databank.getName() + ".", e);
					status.setActualStep(SearchStep.FATAL_ERROR);
					return;
				}

				int hspNum = 0;
				List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();

				for (RetrievedArea retrievedArea : retrievedSequenceAreas) {
					status.setActualStep(SearchStep.SEEDS);

					int sequenceAreaBegin = retrievedArea.sequenceAreaBegin;
					int sequenceAreaEnd = retrievedArea.sequenceAreaEnd;
					int queryAreaBegin = retrievedArea.queryAreaBegin;
					int queryAreaEnd = retrievedArea.queryAreaEnd;
					int querySegmentLength = queryAreaEnd - queryAreaBegin;

					status.setActualStep(SearchStep.ALIGNMENT);

					if (querySegmentLength <= sp.getMinQuerySequenceSubSequence()) {
						continue;
					}

					ExtendSequences extensionResult = ExtendSequences.doExtension(querySequence,
							queryAreaBegin, queryAreaEnd, hitSequence, sequenceAreaBegin, sequenceAreaEnd, sp
									.getSequencesExtendDropoff(), queryAreaBegin, sequenceAreaBegin);

					if (extendedSequencesList.contains(extensionResult)) {
						continue;
					}

					if (extensionResult.getQuerySequenceExtended().length() > sp
							.getMinQuerySequenceSubSequence()
							&& extensionResult.getTargetSequenceExtended().length() > sp.getMinMatchAreaLength()) {
						extendedSequencesList.add(extensionResult);
					}
				}

				if (hitSequence != null && extendedSequencesList.size() > 0) {
					Hit hit = new Hit(hitNum++, sequenceInformation.getName(), sequenceInformation
							.getAccession(), sequenceInformation.getDescription(), hitSequence.length(), databank
							.getName());
					for (ExtendSequences extensionResult : extendedSequencesList) {
						SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(databank.getAlphabet(),
								1, -1);
						GenoogleSmithWaterman smithWaterman = new GenoogleSmithWaterman(-1, 3, 2.5, 2.5, 2,
								substitutionMatrix);
						smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(),
								extensionResult.getTargetSequenceExtended());
						hit.addHSP(new HSP(hspNum++, smithWaterman, extensionResult.getQueryOffset(),
								extensionResult.getTargetOffset()));
					}
					sr.addHit(hit);
				}
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(sr.getHits(), Hit.COMPARATOR);

			status.setResults(sr);
			status.setActualStep(SearchStep.FINISHED);
		}

		private IndexRetrievedData getIndexPositions(short[] iess, int threshould) {

			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(), 50,
					sp);

			status.setActualStep(SearchStep.INDEX_SEARCH);
			try {
				for (int ss = 0; ss < iess.length; ss++) {
					retrieveIndexPosition(iess[ss], threshould, retrievedData, ss);
				}
			} catch (Exception e) {
				logger.fatal("Fatar error while searching at index of the datatabank " + databank.getName()
						+ ".", e);
				status.setActualStep(SearchStep.FATAL_ERROR);
				return null;
			}
			return retrievedData;
		}

		private void retrieveIndexPosition(short encodedSubSequence, int threshould,
				IndexRetrievedData retrievedData, int queryPos) throws ValueOutOfBoundsException,
				IOException, InvalidHeaderData {

			int[] similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);

			for (int i = 0; i < similarSubSequences.length; i++) {
				if (SimilarSubSequencesIndex.getScore(similarSubSequences[i]) < threshould) {
					break;
				}
				int[] indexPositions = databank.getMachingSubSequence((short) similarSubSequences[i]);
				for (long subSequenceIndexInfo : indexPositions) {
					retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
				}
			}
		}
	}

	private short[] getEncodedSubSequences(SymbolList querySequence) {
		short[] iess = new short[querySequence.length() - (8 - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getOverlappedFactory().newSymbolListWindowIterator(querySequence, 8);
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			SymbolList subSequence = symbolListWindowIterator.next();
			iess[pos] = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(
					subSequence);
		}
		return iess;
	}

	private static class IndexRetrievedData {
		Stack<RetrievedArea>[] retrievedAreas;
		private final SearchParams sp;

		public IndexRetrievedData(int size, int initialSize, SearchParams sp) {
			this.sp = sp;
			createStackArray(size);
		}

		@SuppressWarnings("unchecked")
		private void createStackArray(int size) {
			retrievedAreas = new Stack[size];
		}

		void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
			int start = EncoderSubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
			int sequenceId = EncoderSubSequenceIndexInfo.getSequenceId(subSequenceInfoIntRepresention);

			Stack<RetrievedArea> sequenceRetrievedAreas = retrievedAreas[sequenceId];
			if (sequenceRetrievedAreas == null) {
				RetrievedArea retrievedArea = new RetrievedArea(queryPos, queryPos + 7, start, start + 7);
				Stack<RetrievedArea> stack = new Stack<RetrievedArea>();
				stack.push(retrievedArea);
				retrievedAreas[sequenceId] = stack;
			} else {
				RetrievedArea topElement = sequenceRetrievedAreas.firstElement();
				if (!topElement.setTestAndSet(queryPos, sp.getMaxQuerySequenceSubSequencesDistance(),
						start, sp.getMaxQuerySequenceSubSequencesDistance())) {
					if (topElement.length() < sp.getMinMatchAreaLength()) {
						sequenceRetrievedAreas.pop();
					}
				}
			}
		}

		public Stack<RetrievedArea>[] getRetrievedAreas() {
			return retrievedAreas;
		}
	}

	private static class RetrievedArea {
		int queryAreaBegin;
		int queryAreaEnd;
		int sequenceAreaBegin;
		int sequenceAreaEnd;

		public RetrievedArea(int queryAreaBegin, int queryAreaEnd, int sequenceAreaBegin,
				int sequenceAreaEnd) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaEnd;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaEnd;
		}

		public int length() {
			return Math.min(queryAreaEnd - queryAreaBegin, sequenceAreaEnd - sequenceAreaBegin);
		}

		public boolean setTestAndSet(int newQueryAreaBegin, int maxQueryAreaDistance,
				int newSequenceAreaBegin, int maxSequenceAreaDistance) {
			int queryAreaEndOffset = newQueryAreaBegin - queryAreaEnd;
			if ((newQueryAreaBegin >= queryAreaBegin && newQueryAreaBegin <= queryAreaEnd)
					|| (queryAreaEndOffset > 0 && queryAreaEndOffset <= maxQueryAreaDistance)) {

				int sequenceAreaEndOffset = newSequenceAreaBegin - sequenceAreaEnd;
				if ((newSequenceAreaBegin >= sequenceAreaBegin && newSequenceAreaBegin <= sequenceAreaBegin)
						|| (sequenceAreaEndOffset > 0 && sequenceAreaEndOffset <= maxSequenceAreaDistance)) {

					this.queryAreaEnd = newQueryAreaBegin + 7;
					this.sequenceAreaEnd = newSequenceAreaBegin + 7;
					return true;
				}
			}
			return false;
		}
	}
}
