package bio.pih.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SmithWaterman;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.search.SearchInformation.SearchStep;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.IntArray;
import bio.pih.util.LongArray;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
public class DNASearcher implements Searcher {

	Map<Long, SearchInformation> idToSearch = Maps.newHashMap();
	long searchId;

	/**
	 * Construction of the searcher.
	 */
	public DNASearcher() {
		searchId = 0;
	}

	public void cancelSearch(long searchCode) {
		// TODO Auto-generated method stub

	}

	public long doSearch(LightweightSymbolList input, IndexedSequenceDataBank bank) {
		long id = getNextSearchId();

		SearchInformation si = new SearchInformation(bank.getName(), input.toString(), id);
		idToSearch.put(id, si);
		si.setActualStep(SearchStep.NOT_INITIALIZED);

		SimilarSearcher ss = new SimilarSearcher(input, bank);
		ss.run();

		return id;
	}

	public SearchResult getSearchResult(long searchCode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeSearch(long searchCode) {
		// TODO Auto-generated method stub

	}

	public SearchStep verifySearch(long searchCode) {
		// TODO Auto-generated method stub
		return null;
	}

	protected synchronized long getNextSearchId() {
		long id = searchId;
		searchId++;
		return id;
	}

	private static class SimilarSearcher implements Runnable {
		final LightweightSymbolList querySequence;
		final IndexedSequenceDataBank databank;

		/**
		 * Constructor for the inner class that construct a searcher to find sequences that are similar with the sequence into databank.
		 * 
		 * @param querySequence
		 * @param databank
		 */
		public SimilarSearcher(LightweightSymbolList querySequence, IndexedSequenceDataBank databank) {
			this.querySequence = querySequence;
			this.databank = databank;
		}

		public void run() {
			Logger logger = Logger.getLogger("pih.bio.search.DNASearcher.SimilarSearcher");
			// logger.setLevel(Level.ERROR);
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory().newSymbolListWindowIterator(querySequence, 8);

			BitSet subSequencesSearched = new BitSet(65536);

			logger.info("Begining the search of sequence with " + querySequence.length() + "bases " + querySequence.getString());

			SymbolList subSequence;
			short[] iess = new short[querySequence.length() - (8 - 1)];
			int pos = -1;
			while (symbolListWindowIterator.hasNext()) {
				pos++;
				subSequence = symbolListWindowIterator.next();
				for (int i = 0; i < pos; i++) {
					System.out.print(" ");
				}
				System.out.println(pos + " " + subSequence.seqString());
				iess[pos] = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(subSequence);
			}

			LookupTable lookup = new LookupTable();
			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(), 40, lookup);

			// Arrays.sort(iess);
			// int k = 1;
			// for (int i = 1; i < pos; i++) {
			// if (iess[i] != iess[i - 1]) {
			// iess[k++] = iess[i];
			// }
			// }
			// short[] inputSubSequences = new short[k];
			// System.arraycopy(iess, 0, inputSubSequences, 0, k);
			// logger.info("from " + pos + " subSequences to " + k);

			int eco = 0;

			int[] similarSubSequences;
			int[] indexPositions;
			int threshould = 8;

			long init = System.currentTimeMillis();
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
			List<MatchArea> matchAreas = retrievedData.getMatchAreas();
			System.out.println("matches: " + matchAreas.size());

			for (MatchArea matchZone : matchAreas) {
				int sequenceId = matchZone.getSequenceId();
				int sequenceAreaBegin = matchZone.getBegin();
				int sequenceAreaLength = matchZone.getLength();
				IntArray querySubSequences = matchZone.getQuerySubSequences();

				System.out.println(sequenceId + " begin: " + sequenceAreaBegin + " subsequence db length: " + sequenceAreaLength);
				for (int[] querySegments : connectQuerySubSequences(querySubSequences, lookup)) {

					int beginQuerySegment = querySegments[0];
					int lastPosQuerySegment = querySegments[1];
					int querySegmentLength = (lastPosQuerySegment - beginQuerySegment) + 8;
					System.out.println("      " + beginQuerySegment + " - " + lastPosQuerySegment + " subsequence query:" + querySegmentLength);

					try {
						LightweightSymbolList databankSequence = (LightweightSymbolList) databank.getSymbolListFromSequenceId((int) sequenceId);

						SymbolList[] extendedSequences = doExtension(querySequence, beginQuerySegment, beginQuerySegment+querySegmentLength, databankSequence, sequenceAreaBegin, sequenceAreaBegin+sequenceAreaLength, 3);

						System.out.println(querySequence.seqString());
						System.out.println(extendedSequences[0].seqString());

						System.out.println(databankSequence.seqString());
						System.out.println(extendedSequences[1].seqString());

						// SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(databank.getAlphabet(), 1, -1); // values
						// SmithWaterman smithWaterman = new SmithWaterman(-1, 3, 3, 3, 2, substitutionMatrix);
						//
						// smithWaterman.pairwiseAlignment(querySubSequence, subDatabankSequence);
						// System.out.println("Sequence " + sequenceId + " from :" + begin + " to " + (begin + length));
						// System.out.println(smithWaterman.getAlignmentString());

					} catch (IllegalSymbolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BioException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}

		private SymbolList[] doExtension(LightweightSymbolList querySequence, int beginQuerySegment, int endQuerySegment, LightweightSymbolList databankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment, int dropoff) {
			int score = 0;
			int bestScore = 0;
			int bestQueryPos, bestDatabankPos;
			int queryPos, databankPos;

			bestQueryPos = endQuerySegment;
			bestDatabankPos = endDatabankSequenceSegment;
			
			queryPos = endQuerySegment + 1;
			databankPos = endDatabankSequenceSegment + 1;

			// extender a direita
			while (queryPos < querySequence.length() && databankPos < databankSequence.length()) {
				if (querySequence.symbolAt(queryPos+1) == databankSequence.symbolAt(databankPos+1)) {
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
			}

			return new SymbolList[] { querySequence.subList(beginQuerySegment + 1, bestQueryPos), databankSequence.subList(beginDatabankSequenceSegment + 1, bestDatabankPos) };
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
		 * @return the match areas.
		 */
		public List<MatchArea> getMatchAreas() {
			int distanceThreshould = 16;
			int lengthThreadshould = 24;
			List<MatchArea> matchAreas = new LinkedList<MatchArea>();
			long[] sequenceMatchs;
			for (int sequenceNumber = 0; sequenceNumber < sequencesResultArrays.length; sequenceNumber++) {
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
								matchAreas.add(new MatchArea(sequenceNumber, beginArea, (previousMatch - beginArea), querySubSequences));
							}
							beginArea = match;
							querySubSequences = new IntArray();
						}
						// add the
						querySubSequences.add(querySubSequence);
					}

					if ((match - previousMatch < distanceThreshould) && checkContinuousInSequence(previousQuerySubSequence, querySubSequence, distanceThreshould, lookup) && (match - beginArea >= lengthThreadshould)) {
						matchAreas.add(new MatchArea(sequenceNumber, beginArea, (match - beginArea), querySubSequences));
					}
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
