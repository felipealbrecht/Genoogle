package bio.pih.search;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.SequenceInformation;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
import bio.pih.util.IntArray;
import bio.pih.util.LongArray;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the searchers.
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
	public DNASearcher(long id, SearchParams sp, SequenceDataBank bank, SearchManager sm, Searcher parent) {
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
		 * Constructor for the inner class that construct a searcher to find sequences that are similar with the sequence into databank.
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
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory().newSymbolListWindowIterator(querySequence, 8);

			BitSet subSequencesSearched = new BitSet(65536);

			logger.info("Begining the search of sequence with " + querySequence.length() + "bases " + querySequence.seqString());

			SymbolList subSequence;
			short[] iess = new short[querySequence.length() - (8 - 1)];
			int pos = -1;
			while (symbolListWindowIterator.hasNext()) {
				pos++;
				subSequence = symbolListWindowIterator.next();
				// for (int i = 0; i < pos; i++) {
				// System.out.print(" ");
				// }
				// System.out.println(pos + " " + subSequence.seqString());
				iess[pos] = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(subSequence);
			}

			LookupTable lookup = new LookupTable();
			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(), 50, lookup, sp);

			int[] similarSubSequences;
			int[] indexPositions;
			int threshould = sp.getMinSimilarity();

			long init = System.currentTimeMillis();
			status.setActualStep(SearchStep.INDEX_SEARCH);
			try {
				for (int ss = 0; ss < iess.length; ss++) {
					short encodedSubSequence = iess[ss];

					similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);

					for (int i = 0; i < similarSubSequences.length; i++) {
						if (SimilarSubSequencesIndex.getScoreFromIntRepresentation(similarSubSequences[i]) < threshould) {
							break;
						}

						int similarSubSequence = SimilarSubSequencesIndex.getSequenceFromIntRepresentation(similarSubSequences[i]) & 0xFFFF;
						lookup.addPosition(similarSubSequence & 0xFFFF, ss);
						if (!subSequencesSearched.get(similarSubSequence)) {
							indexPositions = databank.getMachingSubSequence((short) similarSubSequence);
							for (long subSequenceIndexInfo : indexPositions) {
								retrievedData.addSubSequenceInfoIntRepresention(similarSubSequence, subSequenceIndexInfo);
							}
							subSequencesSearched.set(similarSubSequence);
						}
					}

				}
			} catch (Exception e) {
				logger.fatal("Fatar error while searching at index of the datatabank " + databank.getName() + ".", e);
				status.setActualStep(SearchStep.FATAL_ERROR);
				return;
			}

			lookup.end();

			logger.info("Search total time:" + (System.currentTimeMillis() - init));
			status.setActualStep(SearchStep.COMPUTING_MATCHS);
			List<List<MatchArea>> sequencesMatchAreas = retrievedData.getMatchAreas();
			logger.info("sequencesHits: " + sequencesMatchAreas.size());

			SearchResults sr = new SearchResults(sp);

			int hitNum = 0;
			for (List<MatchArea> matchAreas : sequencesMatchAreas) {
				SymbolList hitSequence = null;
				int hspNum = 0;

				List<ExtendSequences> extendedSequencesList = Lists.newLinkedList();
				SequenceInformation sequenceInformation = null;
				
				for (MatchArea matchZone : matchAreas) {
					if (sequenceInformation == null) {
						int sequenceId = matchZone.getSequenceId();
						try {
							sequenceInformation = databank.getSequenceInformationFromId(sequenceId);
							hitSequence = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToSymbolList(sequenceInformation.getEncodedSequence());
						} catch (Exception e) {
							logger.fatal("Fatar error while loading sequence " + sequenceId + " from datatabank " + databank.getName() + ".", e);
							status.setActualStep(SearchStep.FATAL_ERROR);
							return;
						}
					}

					int sequenceAreaBegin = matchZone.getBegin();
					int sequenceAreaLength = matchZone.getLength();
					IntArray querySubSequences = matchZone.getQuerySubSequences();

					status.setActualStep(SearchStep.SEEDS);

					for (int[] querySegments : connectQuerySubSequences(querySubSequences, lookup, sp)) {

						int beginQuerySegment = querySegments[0];
						int lastPosQuerySegment = querySegments[1];
						int querySegmentLength = (lastPosQuerySegment - beginQuerySegment) + 8;

						status.setActualStep(SearchStep.ALIGNMENT);

						if (querySegmentLength <= sp.getMinQuerySequenceSubSequence()) {
							continue;
						}

						ExtendSequences extensionResult = ExtendSequences.doExtension(querySequence, beginQuerySegment, beginQuerySegment + querySegmentLength, hitSequence, sequenceAreaBegin, sequenceAreaBegin + sequenceAreaLength, sp.getSequencesExtendDropoff(), beginQuerySegment, sequenceAreaBegin);

						if (extendedSequencesList.contains(extensionResult)) {
							continue;
						}

						if (extensionResult.getQuerySequenceExtended().length() > sp.getMinQuerySequenceSubSequence() && extensionResult.getTargetSequenceExtended().length() > sp.getMinMatchAreaLength()) {
							extendedSequencesList.add(extensionResult);
						}

					}
				}

				if (sequenceInformation != null && hitSequence != null && extendedSequencesList.size() > 0) {
					Hit hit = new Hit(hitNum++, sequenceInformation.getName(), sequenceInformation.getAccession(), sequenceInformation.getDescription(), hitSequence.length(), databank.getName());
					for (ExtendSequences extensionResult : extendedSequencesList) {
						SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(databank.getAlphabet(), 1, -1);
						GenoogleSmithWaterman smithWaterman = new GenoogleSmithWaterman(-1, 3, 2.5, 2.5, 2, substitutionMatrix);
						smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(), extensionResult.getTargetSequenceExtended());
						hit.addHSP(new HSP(hspNum++, smithWaterman, extensionResult.getQueryOffset(), extensionResult.getTargetOffset()));
					}
					sr.addHit(hit);
				}
			}

			status.setActualStep(SearchStep.SELECTING);

			Collections.sort(sr.getHits(), Hit.COMPARATOR);

			status.setResults(sr);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

	private List<int[]> connectQuerySubSequences(IntArray querySubSequences, LookupTable lookup, SearchParams sp) {
		List<int[]> areas = Lists.newLinkedList();

		// para cada subSequence, comparar todas as posicoes que ela ocorre com as da sub-sequencia seguinte e criar pares.
		int[] subSequences = querySubSequences.getArray();
		if (subSequences.length >= 2) {
			for (int i : lookup.getPos(subSequences[0])) {
				int[] area = null;

				boolean isIn = false;

				// The initial position is between existent areas?
				for (int[] eArea : areas) {
					if (i >= eArea[0] && i <= eArea[1]) {
						isIn = true;
						break;
					}
				}

				if (!isIn) {
					for (int j : lookup.getPos(subSequences[1])) {
						boolean merged = false;

						for (int[] eArea : areas) {
							int offset = j - eArea[1];
							if (offset != 0 && offset <= sp.getMaxQuerySequenceSubSequencesDistance()) {
								eArea[1] = j;
								merged = true;
							}
						}

						if (!merged) {
							int offset = j - i;
							if (offset > 0 && offset <= sp.getMaxQuerySequenceSubSequencesDistance()) {
								if (area == null) {
									area = new int[] { i, j };
									areas.add(area);
								}
							}
						}
					}
				}
			}

			for (int pos = 2; pos < subSequences.length; pos++) {
				List<int[]> newAreas = Lists.newLinkedList();
				ListIterator<int[]> areasIterator = areas.listIterator();

				while (areasIterator.hasNext()) {
					int[] area = areasIterator.next();

					boolean added = false;
					int lastPos = area[1];
					for (int j : lookup.getPos(subSequences[pos])) {
						if (j > lastPos && j - lastPos <= sp.getMaxQuerySequenceSubSequencesDistance() && j - lastPos != 0) {
							lastPos = j;
							added = true;
						}
					}
					if (added) {
						newAreas.add(new int[] { area[0], lastPos });
					}
				}
				if (newAreas.size() != 0) {
					areas = newAreas;
				}
			}
		}

		return areas;
	}

	private static long encodeSubSequenceAndPos(int subSequence, int pos) {
		long encodedPosAndSubSequence = ((long) pos << 32) | (subSequence & 0xFFFF);
		// assert decodePosFromEncoded(encodedPosAndSubSequence) == pos;
		// assert decodeSubSequenceFromEncoded(encodedPosAndSubSequence) == subSequence;
		return encodedPosAndSubSequence;
	}

	private static int decodePosFromEncoded(long encodedPosAndSubSequence) {
		return (int) (encodedPosAndSubSequence >> 32);
	}

	private static int decodeSubSequenceFromEncoded(long encodedPosAndSubSequence) {
		return (int) (encodedPosAndSubSequence & 0xFFFF);
	}

	private static class IndexRetrievedData {
		LongArray[] sequencesResultArrays;
		private final LookupTable lookup;
		private final SearchParams sp;

		/**
		 * @param size
		 *            the qtd of lists
		 * @param initialSize
		 * @param lookup 
		 * @param sp 
		 */
		public IndexRetrievedData(int size, int initialSize, LookupTable lookup, SearchParams sp) {
			this.lookup = lookup;
			this.sp = sp;
			sequencesResultArrays = new LongArray[size];
			for (int i = 0; i < size; i++) {
				sequencesResultArrays[i] = new LongArray(initialSize);
			}
		}

		void addSubSequenceInfoIntRepresention(int subSequence, long subSequenceInfoIntRepresention) {
			int start = EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);
			int sequenceId = EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);

			long subSequenceAndPos = encodeSubSequenceAndPos(subSequence, start);

			sequencesResultArrays[sequenceId].add(subSequenceAndPos);
		}

		/**
		 * @return the match areas of each sequence.
		 */
		public List<List<MatchArea>> getMatchAreas() {
			List<List<MatchArea>> matchAreas = Lists.newLinkedList();
			long[] sequenceMatchs;
			for (int sequenceNumber = 0; sequenceNumber < sequencesResultArrays.length; sequenceNumber++) {
				// lazy creation for memory economy propose.
				List<MatchArea> sequenceMatchAreas = null;
				sequenceMatchs = sequencesResultArrays[sequenceNumber].getArray();
				//sequencesResultArrays[sequenceNumber] = null; // no more this data.
				
				if (sequenceMatchs.length >= sp.getMinMatchAreaLength() / 8) {
					Arrays.sort(sequenceMatchs);

					// Hardly a same sequence will have 20 hits
					IntArray querySubSequences = new IntArray(20);

					// The first position
					int previousMatch = decodePosFromEncoded(sequenceMatchs[0]);
					int beginArea = previousMatch;
					int match = previousMatch;

					int previousQuerySubSequence = decodeSubSequenceFromEncoded(sequenceMatchs[0]);
					int querySubSequence = previousQuerySubSequence;

					querySubSequences.add(querySubSequence);

					// the inner positions
					for (int i = 1; i < sequenceMatchs.length; i++) {
						previousMatch = match;
						match = decodePosFromEncoded(sequenceMatchs[i]);
						previousQuerySubSequence = querySubSequence;
						querySubSequence = decodeSubSequenceFromEncoded(sequenceMatchs[i]);

						assert match > previousMatch;

						// End of area. Check if it's continuous in the data bank sequence and too in query sequence
						if ((match - previousMatch >= sp.getMaxDatabankSequenceSubSequencesDistance()) || !checkContinuousInSequence(previousQuerySubSequence, querySubSequence, sp.getMaxQuerySequenceSubSequencesDistance(), lookup)) {
							// Add? at least 3 subsequences
							if ((previousMatch - beginArea >= sp.getMinMatchAreaLength()) && (querySubSequences.length() >= sp.getMinMatchAreaLength() / 8)) {
								if (sequenceMatchAreas == null) {
									sequenceMatchAreas = Lists.newLinkedList();
								}
								sequenceMatchAreas.add(new MatchArea(sequenceNumber, beginArea, (previousMatch - beginArea) + 8, querySubSequences));
								querySubSequences = new IntArray(20);
							}
							querySubSequences.reset();
							beginArea = match;
						}

						querySubSequences.add(querySubSequence);
					}

					if ((match - previousMatch <= sp.getMaxDatabankSequenceSubSequencesDistance()) && (match - beginArea >= sp.getMinMatchAreaLength() && checkContinuousInSequence(previousQuerySubSequence, querySubSequence, sp.getMaxQuerySequenceSubSequencesDistance(), lookup))) {
						if (sequenceMatchAreas == null) {
							sequenceMatchAreas = Lists.newLinkedList();
						}
						sequenceMatchAreas.add(new MatchArea(sequenceNumber, beginArea, (match - beginArea) + 8, querySubSequences));
					}
				}
				if (sequenceMatchAreas != null) {
					matchAreas.add(sequenceMatchAreas);
				}
			}
			return matchAreas;
		}

		boolean checkContinuousInSequence(int previousQuerySubSequence, int querySubSequence, int threshould, LookupTable lookup) {
			for (int pPos : lookup.getPos(previousQuerySubSequence)) {
				for (int pos : lookup.getPos(querySubSequence)) {
					int offset = pos - pPos;
					if ((offset > 0) && (offset <= threshould)) {
						return true;
					}
				}
			}
			return false;
		}
	}

}
