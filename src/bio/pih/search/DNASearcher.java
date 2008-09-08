package bio.pih.search;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.io.proto.Io.StoredSequence;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

/**
 * Interface witch defines methods for search for similar DNA sequences and
 * checks the status of the searchers.
 * 
 * @author albrecht
 */
public class DNASearcher extends AbstractSearcher {

	private static final Logger logger = Logger.getLogger(DNASearcher.class.getName());
	private static final DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getDefaultEncoder();

	private static SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(
			DNATools.getDNA(), 1, -1);

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
		 * Constructor for the inner class that construct a searcher to find
		 * sequences that are similar with the sequence into databank.
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

			int[] iess = getEncodedSubSequences(querySequence);
			int threshould = sp.getMinSimilarity();

			long init = System.currentTimeMillis();
			IndexRetrievedData retrievedData = getIndexPositions(iess, threshould);

			logger.info("Index search time:" + (System.currentTimeMillis() - init));
			status.setActualStep(SearchStep.INDEX_SEARCH);
			List<RetrievedArea>[] sequencesRetrievedAreas = retrievedData.getRetrievedAreas();

			SearchResults sr = new SearchResults(sp);

			status.setActualStep(SearchStep.EXTENDING);
			int hitNum = 0;
			for (int sequenceId = 0; sequenceId < sequencesRetrievedAreas.length; sequenceId++) {
				List<RetrievedArea> retrievedSequenceAreas = sequencesRetrievedAreas[sequenceId];
				if (retrievedSequenceAreas == null || retrievedSequenceAreas.size() == 0) {
					continue;
				}
				StoredSequence storedSequence = null;
				SymbolList hitSequence = null;

				try {
					storedSequence = databank.getSequenceFromId(sequenceId);
					ByteString encodedSequence = storedSequence.getEncodedSequence();
					byte[] byteArray = encodedSequence.toByteArray();
					final int[] ret = new int[byteArray.length/4];
					ByteBuffer.wrap(byteArray).asIntBuffer().get(ret);
					hitSequence = encoder.decodeIntegerArrayToSymbolList(ret);
				} catch (Exception e) {
					logger.fatal("Fatar error while loading sequence " + sequenceId
							+ " from datatabank " + databank.getName() + ".", e);
					status.setActualStep(SearchStep.FATAL_ERROR);
					return;
				}

				int hspNum = 0;
				List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();
				for (RetrievedArea retrievedArea : retrievedSequenceAreas) {
					int sequenceAreaBegin = retrievedArea.sequenceAreaBegin;
					int sequenceAreaEnd = retrievedArea.sequenceAreaEnd;
					int queryAreaBegin = retrievedArea.queryAreaBegin;
					int queryAreaEnd = retrievedArea.queryAreaEnd;

					ExtendSequences extensionResult = ExtendSequences.doExtension(querySequence,
							queryAreaBegin, queryAreaEnd, hitSequence, sequenceAreaBegin,
							sequenceAreaEnd, sp.getSequencesExtendDropoff(), queryAreaBegin,
							sequenceAreaBegin);

					if (extendedSequencesList.contains(extensionResult)) {
						continue;
					}

					if (extensionResult.getQuerySequenceExtended().length() > sp.getMinQuerySequenceSubSequence()
							&& extensionResult.getTargetSequenceExtended().length() > sp.getMinMatchAreaLength()) {
						extendedSequencesList.add(extensionResult);
					}
				}

				status.setActualStep(SearchStep.ALIGNMENT);
				if (hitSequence != null && extendedSequencesList.size() > 0) {
					Hit hit = new Hit(hitNum++, storedSequence.getName(),
							storedSequence.getAccession(), storedSequence.getDescription(),
							hitSequence.length(), databank.getName());
					for (ExtendSequences extensionResult : extendedSequencesList) {

						GenoogleSmithWaterman smithWaterman = new GenoogleSmithWaterman(-1, 2, 3,
								3, 1, substitutionMatrix);
						smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(),
								extensionResult.getTargetSequenceExtended());
						hit.addHSP(new HSP(hspNum++, smithWaterman,
								extensionResult.getQueryOffset(), extensionResult.getTargetOffset()));
					}
					sr.addHit(hit);
				}
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(sr.getHits(), Hit.COMPARATOR);

			status.setResults(sr);
			logger.info("Search time:" + (System.currentTimeMillis() - init));
			status.setActualStep(SearchStep.FINISHED);
		}

		private IndexRetrievedData getIndexPositions(int[] iess, int threshould) {

			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(),
					sp);

			status.setActualStep(SearchStep.INDEX_SEARCH);
			try {
				for (int ss = 0; ss < iess.length; ss++) {
					retrieveIndexPosition(iess[ss], threshould, retrievedData, ss);
				}
			} catch (Exception e) {
				logger.fatal("Fatar error while searching at index of the datatabank "
						+ databank.getName() + ".", e);
				status.setActualStep(SearchStep.FATAL_ERROR);
				return null;
			}
			return retrievedData;
		}

		private void retrieveIndexPosition(int encodedSubSequence, int threshould,
				IndexRetrievedData retrievedData, int queryPos) throws ValueOutOfBoundsException,
				IOException, InvalidHeaderData {

			List<Integer> similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);

			for (Integer similarSubSequence: similarSubSequences) {
				long[] indexPositions = databank.getMachingSubSequence(similarSubSequence);
				for (long subSequenceIndexInfo : indexPositions) {
					retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
				}
			}
		}
	}

	private int[] getEncodedSubSequences(SymbolList querySequence) {
		int[] iess = new int[querySequence.length() - (XMLConfigurationReader.getSubSequenceLength() - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory()
				.newSymbolListWindowIterator(querySequence, XMLConfigurationReader.getSubSequenceLength());
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			SymbolList subSequence = symbolListWindowIterator.next();
			iess[pos] = DNASequenceEncoderToInteger.getDefaultEncoder().encodeSubSymbolListToInteger(
					subSequence);
		}
		return iess;
	}

	private static final class IndexRetrievedData {
		final List<RetrievedArea>[] retrievedAreasArray;
		final FuckingArrayList<RetrievedArea>[] openedAreasArray;
		private final SearchParams sp;

		@SuppressWarnings("unchecked")
		public IndexRetrievedData(int size, SearchParams sp) {
			this.sp = sp;
			retrievedAreasArray = new List[size];
			openedAreasArray = new FuckingArrayList[size];
			for (int i = 0; i < size; i++) {
				openedAreasArray[i] = new FuckingArrayList();
			}
		}

		void addSubSequenceInfoIntRepresention(int queryPos, long subSequenceInfoIntRepresention) {
			int start = EncoderSubSequenceIndexInfo.getStart(subSequenceInfoIntRepresention);
			int sequenceId = EncoderSubSequenceIndexInfo.getSequenceId(subSequenceInfoIntRepresention);

			mergeOrRemoveOrNew(queryPos, start, sequenceId);
		}

		private void mergeOrRemoveOrNew(int queryPos, int start, int sequenceId) {
			boolean merged = false;
			FuckingArrayList<RetrievedArea> list = openedAreasArray[sequenceId];
			int fromIndex = -1;
			int toIndex = -1;

			int size = list.size();
			for (int pos = 0; pos < size; pos++) {
				RetrievedArea openedArea = list.get(pos);
				// Try merge with previous area.
				if (openedArea.setTestAndSet(queryPos,
						sp.getMaxQuerySequenceSubSequencesDistance(), start,
						sp.getMaxDatabankSequenceSubSequencesDistance())) {
					merged = true;

					// Check if the area end is away from the actual sequence
					// pos.
				} else if (queryPos - openedArea.queryAreaEnd > sp.getMaxQuerySequenceSubSequencesDistance()) {
					// Mark the areas to remove.
					if (fromIndex == -1) {
						fromIndex = pos;
						toIndex = pos;
					} else {
						// TODO: See why some sequences are lost.
						toIndex = pos;
					}
					if (openedArea.length() >= sp.getMinMatchAreaLength()) {
						if (retrievedAreasArray[sequenceId] == null) {
							retrievedAreasArray[sequenceId] = Lists.newArrayList();
						}
						retrievedAreasArray[sequenceId].add(openedArea);
					}
				}
			}

			if (fromIndex != -1) {
				list.removeRange(fromIndex, toIndex + 1);
			}

			if (!merged) {
				RetrievedArea retrievedArea = new RetrievedArea(queryPos, queryPos + 7, start,
						start + 7);
				openedAreasArray[sequenceId].add(retrievedArea);
			}
		}

		public List<RetrievedArea>[] getRetrievedAreas() {
			for (int sequenceId = 0; sequenceId < openedAreasArray.length; sequenceId++) {
				List<RetrievedArea> openedAreaList = openedAreasArray[sequenceId];
				if (openedAreaList != null) {
					for (RetrievedArea openedArea : openedAreaList) {
						if (openedArea.length() >= sp.getMinMatchAreaLength()) {
							if (retrievedAreasArray[sequenceId] == null) {
								retrievedAreasArray[sequenceId] = Lists.newArrayList();
							}
							retrievedAreasArray[sequenceId].add(openedArea);
						}
					}
				}
			}
			int totalNotZero = 0;
			for (int i = 0; i < retrievedAreasArray.length; i++) {
				if (retrievedAreasArray[i] != null) {
					if (retrievedAreasArray[i].size() > 0) {
						totalNotZero++;
					}
				}
			}

			System.out.println("TotalAreas: " + totalNotZero);

			return retrievedAreasArray;
		}
	}

	private final static class RetrievedArea {
		int queryAreaBegin;
		int queryAreaEnd;
		int sequenceAreaBegin;
		int sequenceAreaEnd;
		int length;

		public RetrievedArea(int queryAreaBegin, int queryAreaEnd, int sequenceAreaBegin,
				int sequenceAreaEnd) {
			this.queryAreaBegin = queryAreaBegin;
			this.queryAreaEnd = queryAreaEnd;
			this.sequenceAreaBegin = sequenceAreaBegin;
			this.sequenceAreaEnd = sequenceAreaEnd;
			this.length = 8;
		}

		public int length() {
			return this.length;
		}

		public boolean setTestAndSet(int newQueryAreaBegin, int maxQueryAreaDistance,
				int newSequenceAreaBegin, int maxSequenceAreaDistance) {

			int queryAreaEndOffset = newQueryAreaBegin - queryAreaEnd;
			if (queryAreaEndOffset > -7 && queryAreaEndOffset <= maxQueryAreaDistance) {
				int sequenceAreaEndOffset = newSequenceAreaBegin - sequenceAreaEnd;
				if (sequenceAreaEndOffset > -7 && sequenceAreaEndOffset <= maxSequenceAreaDistance) {
					this.queryAreaEnd = newQueryAreaBegin + 7;
					this.sequenceAreaEnd = newSequenceAreaBegin + 7;
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

		@Override
		public void removeRange(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}
}
