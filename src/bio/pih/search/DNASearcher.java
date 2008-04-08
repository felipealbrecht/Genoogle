package bio.pih.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.io.MultipleSequencesFoundException;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.SequenceInformation;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.IntArray;
import bio.pih.util.LongArray;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the searchers.
 * 
 * 
 * 40164
 * 
 * bio.pih.searchDNASearcher
 * 
 * @author albrecht
 */
public class DNASearcher extends AbstractSearcher {

	@Override
	public SearchStatus doSearch(SearchParams sp, SequenceDataBank bank) {
		status = super.doSearch(sp, bank);
		IndexedDatabankSimilarSearcher ss = new IndexedDatabankSimilarSearcher(sp, (IndexedSequenceDataBank) bank);
		ss.start();

		return status;
	}

	protected class IndexedDatabankSimilarSearcher extends Thread {
		private final IndexedSequenceDataBank databank;
		private final SearchParams sp;

		/**
		 * Constructor for the inner class that construct a searcher to find sequences that are similar with the sequence into databank.
		 * 
		 * @param sp
		 * @param databank
		 * @param status
		 */
		public IndexedDatabankSimilarSearcher(SearchParams sp, IndexedSequenceDataBank databank) {
			this.sp = sp;
			this.databank = databank;
		}

		@Override
		public void run() {
			SymbolList querySequence = sp.getQuery();

			status.setActualStep(SearchStep.INITIALIZED);
			Logger logger = Logger.getLogger("pih.bio.search.DNASearcher.SimilarSearcher");
			// logger.setLevel(Level.ERROR);
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
			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(), 40, lookup);

			int eco = 0;

			int[] similarSubSequences;
			int[] indexPositions;
			int threshould = 8;

			long init = System.currentTimeMillis();
			status.setActualStep(SearchStep.INDEX_SEARCH);
			try {
				for (int ss = 0; ss < iess.length; ss++) {
					short encodedSubSequence = iess[ss];

					similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);

					for (int i = 0; i < similarSubSequences.length; i++) {
						if (SubSequencesComparer.getScoreFromIntRepresentation(similarSubSequences[i]) < threshould) {
							break;
						}

						int similarSubSequence = SubSequencesComparer.getSequenceFromIntRepresentation(similarSubSequences[i]) & 0xFFFF;
						lookup.addPosition(similarSubSequence & 0xFFFF, ss);
						if (!subSequencesSearched.get(similarSubSequence)) {
							indexPositions = databank.getMachingSubSequence((short) similarSubSequence);
							for (long subSequenceIndexInfo : indexPositions) {
								retrievedData.addSubSequenceInfoIntRepresention(similarSubSequence, subSequenceIndexInfo);
							}
							subSequencesSearched.set(similarSubSequence);
						} else {
							eco++;
						}
					}

				}
			} catch (ValueOutOfBoundsException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidHeaderData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lookup.end();

			logger.info("Search total time:" + (System.currentTimeMillis() - init) + " and found " + retrievedData.getTotal() + " possible seeds");
			System.out.println("eco = " + eco);
			status.setActualStep(SearchStep.COMPUTING_MATCHS);
			List<List<MatchArea>> sequencesMatchAreas = retrievedData.getMatchAreas();
			System.out.println("sequencesHits: " + sequencesMatchAreas.size());

			SearchResults sr = new SearchResults(sp);

			int hitNum = 0;
			List<Hit> hits = Lists.newLinkedList();
			for (List<MatchArea> matchAreas : sequencesMatchAreas) {
				Hit hit = null;
				SymbolList hitSequence = null;
				int hspNum = 0;

				for (MatchArea matchZone : matchAreas) {
					if (hit == null) {
						int sequenceId = matchZone.getSequenceId();
						SequenceInformation sequenceInformation;
						try {
							sequenceInformation = databank.getSequenceInformationFromId(sequenceId);
							hitSequence = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToSymbolList(sequenceInformation.getEncodedSequence());
							hit = new Hit(hitNum, sequenceInformation.getName(), sequenceInformation.getAccession(), sequenceInformation.getDescription(), hitSequence.length());
						} catch (IllegalSymbolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MultipleSequencesFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BioException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					int sequenceAreaBegin = matchZone.getBegin();
					int sequenceAreaLength = matchZone.getLength();
					IntArray querySubSequences = matchZone.getQuerySubSequences();

					status.setActualStep(SearchStep.SEEDS);
					for (int[] querySegments : connectQuerySubSequences(querySubSequences, lookup)) {

						int beginQuerySegment = querySegments[0];
						int lastPosQuerySegment = querySegments[1];
						int querySegmentLength = (lastPosQuerySegment - beginQuerySegment) + 8;

						if (querySegmentLength < 24) { // if query segments is too short
							continue;
						}

						status.setActualStep(SearchStep.ALIGNMENT);

						ExtensionResult extensionResult = doExtension(querySequence, beginQuerySegment, beginQuerySegment + querySegmentLength, hitSequence, sequenceAreaBegin, sequenceAreaBegin + sequenceAreaLength, 5);

						SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(databank.getAlphabet(), 1, -1); // values
						GenoogleSmithWaterman smithWaterman = new GenoogleSmithWaterman(-1, 3, 3, 3, 2, substitutionMatrix);
						smithWaterman.pairwiseAlignment(extensionResult.getQuerySequenceExtended(), extensionResult.getTargetSequenceExtended());

						int queryOffset = beginQuerySegment - extensionResult.getQueryLeftExtended();
						int targetOffset = sequenceAreaBegin - extensionResult.getTargetLeftExtender();

						hit.addHSP(new HSP(hspNum++, smithWaterman, queryOffset, targetOffset));
					}
					sr.addHit(hit);
				}

			}

			status.setActualStep(SearchStep.SELECTING);

			// Collections.sort(hits, HSP.getScoreComparetor());

			status.setResults(sr);
			status.setActualStep(SearchStep.FINISHED);
		}
	}

	private static class ExtensionResult {
		SymbolList querySequenceExtended;
		SymbolList targetSequenceExtended;

		int queryLeftExtended, queryRightExtended, targetLeftExtended, targetRightExtended;

		public ExtensionResult(SymbolList querySequenceExtended, SymbolList targetSequenceExtended, int queryLeftExtended, int queryRightExtended, int targetLeftExtended, int targetRightExtended) {
			this.querySequenceExtended = querySequenceExtended;
			this.targetSequenceExtended = targetSequenceExtended;

			this.queryLeftExtended = queryLeftExtended;
			this.targetLeftExtended = targetLeftExtended;
			this.queryRightExtended = queryRightExtended;
			this.targetRightExtended = targetRightExtended;
		}

		public SymbolList getQuerySequenceExtended() {
			return querySequenceExtended;
		}

		public SymbolList getTargetSequenceExtended() {
			return targetSequenceExtended;
		}

		public int getQueryLeftExtended() {
			return queryLeftExtended;
		}

		public int getTargetLeftExtender() {
			return targetLeftExtended;
		}

		public int getQueryRightExtended() {
			return queryRightExtended;
		}

		public int getTargetRightExtender() {
			return targetRightExtended;
		}
	}

	private ExtensionResult doExtension(SymbolList querySequence, int beginQuerySegment, int endQuerySegment, SymbolList databankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment, int dropoff) {
		int score = 0;
		int bestScore = 0;
		int bestQueryPos, bestDatabankPos;
		int queryPos, databankPos;

		// Atention: biojava sequence symbols is from 1 to sequenceLength. It means that the first position is one and not zero!

		// right extend
		bestQueryPos = endQuerySegment;
		bestDatabankPos = endDatabankSequenceSegment;

		queryPos = endQuerySegment;
		databankPos = endDatabankSequenceSegment;

		while (queryPos < querySequence.length() && databankPos < databankSequence.length()) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos);
			if (symbolAtQuery == symbolAtDatabank) {
				score++;
				if (score > bestScore) {
					bestScore = score;
					bestQueryPos = queryPos;
					bestDatabankPos = databankPos;
				}
			} else {
				score--;
				if (bestScore - score > dropoff) {
					break;
				}
			}
			queryPos++;
			databankPos++;
		}

		int rightBestQueryPos = bestQueryPos;
		int rightBestDatabankPos = bestDatabankPos;

		// left extend
		score = 0;
		bestScore = 0;

		bestQueryPos = beginQuerySegment;
		bestDatabankPos = beginDatabankSequenceSegment;

		queryPos = beginQuerySegment;
		databankPos = beginDatabankSequenceSegment;

		while (queryPos > 0 && databankPos > 0) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos + 1);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos + 1);
			if (symbolAtQuery == symbolAtDatabank) {
				score++;
				if (score > bestScore) {
					bestScore = score;
					bestQueryPos = queryPos;
					bestDatabankPos = databankPos;
				}
			} else {
				score--;
				if (bestScore - score > dropoff) {
					break;
				}
			}
			queryPos--;
			databankPos--;
		}

		SymbolList queryExtended = querySequence.subList(bestQueryPos + 1, rightBestQueryPos);
		SymbolList targetExtended = databankSequence.subList(bestDatabankPos + 1, rightBestDatabankPos);

		int queryLeftExtended = beginQuerySegment - bestQueryPos;
		int queryRightExtend = rightBestQueryPos - endQuerySegment;
		int targetLeftExtended = beginDatabankSequenceSegment - bestDatabankPos;
		int targetRightExtended = rightBestDatabankPos - endDatabankSequenceSegment;

		return new ExtensionResult(queryExtended, targetExtended, queryLeftExtended, queryRightExtend, targetLeftExtended, targetRightExtended);
	}

	private List<int[]> connectQuerySubSequences(IntArray querySubSequences, LookupTable lookup) {
		List<int[]> sequenceSegments = Lists.newLinkedList();
		boolean merged;
		int maxPos = -1;

		for (int subSequence : querySubSequences.getArray()) {
			for (int queryPos : lookup.getPos(subSequence)) {
				merged = false;
				if (queryPos > maxPos) {
					for (int[] segment : sequenceSegments) {
						if ((queryPos - segment[1] != 0) && (queryPos - segment[1] <= 16)) {
							segment[1] = queryPos;
							merged = true;
							maxPos = queryPos;
						}
					}
					if (merged == false) {
						sequenceSegments.add(new int[] { queryPos, queryPos });
						maxPos = queryPos;
					}
				}
			}
		}

		assert sequenceSegments.size() > 0;
		return sequenceSegments;
	}

	private static long encodeSubSequenceAndPos(int subSequence, int pos) {
		long encodedPosAndSubSequence = ((long) pos << 32) | (subSequence & 0xFFFF);
		assert decodePosFromEncoded(encodedPosAndSubSequence) == pos;
		assert decodeSubSequenceFromEncoded(encodedPosAndSubSequence) == subSequence;
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

		/**
		 * @param size
		 *            the qtd of lists
		 * @param initialSize
		 */
		@SuppressWarnings("unchecked")
		public IndexRetrievedData(int size, int initialSize, LookupTable lookup) {
			this.lookup = lookup;
			sequencesResultArrays = new LongArray[size];
			for (int i = 0; i < size; i++) {
				sequencesResultArrays[i] = new LongArray(initialSize);
			}
		}

		void addSubSequenceInfoIntRepresention(int subSequence, long subSequenceInfoIntRepresention) {
			int start = SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);
			int sequenceId = SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);

			long subSequenceAndPos = encodeSubSequenceAndPos(subSequence, start);

			sequencesResultArrays[sequenceId].add(subSequenceAndPos);
		}

		/**
		 * @return possible seeds total.
		 */
		public long getTotal() {
			int total = 0;
			for (LongArray array : sequencesResultArrays) {
				if (array.getArray() != null) {
					total += array.getArray().length;
				}
			}
			return total;
		}

		/**
		 * @return the match areas of each sequence.
		 */
		public List<List<MatchArea>> getMatchAreas() {
			int distanceThreshould = 16;
			int lengthThreadshould = 24;
			List<List<MatchArea>> matchAreas = Lists.newLinkedList();
			long[] sequenceMatchs;
			for (int sequenceNumber = 0; sequenceNumber < sequencesResultArrays.length; sequenceNumber++) {
				// lazy creation for memory economy propose.
				List<MatchArea> sequenceMatchAreas = null;
				sequenceMatchs = sequencesResultArrays[sequenceNumber].getArray();

				if (sequenceMatchs.length > 1) {
					Arrays.sort(sequenceMatchs);

					IntArray querySubSequences = new IntArray();

					// The first position
					int previousMatch = decodePosFromEncoded(sequenceMatchs[0]);
					int beginArea = decodePosFromEncoded(sequenceMatchs[0]);
					int match = decodePosFromEncoded(sequenceMatchs[0]);
					int previousQuerySubSequence = decodeSubSequenceFromEncoded(sequenceMatchs[0]);
					int querySubSequence = decodeSubSequenceFromEncoded(sequenceMatchs[0]);

					querySubSequences.add(querySubSequence);

					// the inner positions
					for (int i = 1; i < sequenceMatchs.length; i++) {
						previousMatch = match;
						match = decodePosFromEncoded(sequenceMatchs[i]);
						previousQuerySubSequence = querySubSequence;
						querySubSequence = decodeSubSequenceFromEncoded(sequenceMatchs[i]);

						// End of area. the if it's continuous in the databank sequence and too in query sequence
						if ((match - previousMatch > distanceThreshould) || !checkContinuousInSequence(previousQuerySubSequence, querySubSequence, distanceThreshould, lookup)) {
							// Add? at least 3 subsequences
							if ((previousMatch - beginArea >= lengthThreadshould) && (querySubSequences.length() >= 3)) {
								if (sequenceMatchAreas == null) {
									sequenceMatchAreas = Lists.newLinkedList();
								}
								sequenceMatchAreas.add(new MatchArea(sequenceNumber, beginArea, (previousMatch - beginArea) + 8, querySubSequences));
							}
							querySubSequences = new IntArray();
							beginArea = match;
						}
						// add the
						querySubSequences.add(querySubSequence);
					}

					if ((match - previousMatch < distanceThreshould) && checkContinuousInSequence(previousQuerySubSequence, querySubSequence, distanceThreshould, lookup) && (match - beginArea >= lengthThreadshould)) {
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
					int offset = Math.abs(pos - pPos);
					if ((offset != 0) && (offset <= threshould)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private static class LookupTable {
		IntArray[] positions;
		int[] EMPTY = new int[0];

		public LookupTable() {
			positions = new IntArray[(int) Math.pow(4, 8)];
		}

		public void addPosition(int subSequence, int pos) {
			if (positions[subSequence] == null) {
				positions[subSequence] = new IntArray(20);
			}
			positions[subSequence].add(pos);
		}

		public int[] getPos(int subSequence) {
			if (positions[subSequence] != null) {
				return positions[subSequence].getArray();
			}
			return EMPTY;
		}

		public void end() {
			for (IntArray intArray : positions) {
				if (intArray != null) {
					intArray.sort();
				}
			}
		}
	}

}
