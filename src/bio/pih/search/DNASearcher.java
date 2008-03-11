package bio.pih.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import org.apache.log4j.Logger;
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

import com.google.common.collect.Maps;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the searchers.
 * 
 * @author albrecht
 */
public class DNASearcher implements Searcher {

	Map<Long, SearchInformation> idToSearch = Maps.newHashMap();
	long searchId;

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
		final LightweightSymbolList sequence;
		final IndexedSequenceDataBank databank;

		short[] encodedSequence;

		public SimilarSearcher(LightweightSymbolList sequence, IndexedSequenceDataBank databank) {
			this.sequence = sequence;
			this.databank = databank;
		}

		public void run() {
			Logger logger = Logger.getLogger("pih.bio.search.DNASearcher.SimilarSearcher");
			// logger.setLevel(Level.ERROR);
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory().newSymbolListWindowIterator(sequence, 8);

			BitSet subSequencesSearched = new BitSet(65536);

			IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getTotalSequences(), 40);

			logger.info("Begining the search of sequence with " + sequence.length() + "bases " + sequence.getString());

			SymbolList subSequence;
			short[] iess = new short[sequence.length() - (8 - 1)];
			int pos = -1;
			while (symbolListWindowIterator.hasNext()) {
				pos++;
				subSequence = symbolListWindowIterator.next();
				iess[pos] = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(subSequence);
			}
			Arrays.sort(iess);
			int k = 1;
			for (int i = 1; i < pos; i++) {
				if (iess[i] != iess[i - 1]) {
					iess[k++] = iess[i];
				}
			}
			short[] inputSubSequences = new short[k];
			System.arraycopy(iess, 0, inputSubSequences, 0, k);
			logger.info("from " + pos + " subSequences to " + k);

			int eco = 0;

			int[] similarSubSequences;
			long[] indexPositions;
			int threshould = 1;

			long init = System.currentTimeMillis();
			try {
				for (short encodedSubSequence : inputSubSequences) {
					similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);

					for (int i = 0; i < similarSubSequences.length; i++) {
						if (SubSequencesComparer.getScoreFromIntRepresentation(similarSubSequences[i]) < threshould) {
							break;
						}

						int sequenceFromIntRepresentation = SubSequencesComparer.getSequenceFromIntRepresentation(similarSubSequences[i]) & 0xFFFF;
						if (!subSequencesSearched.get(sequenceFromIntRepresentation)) {
							indexPositions = databank.getMachingSubSequence((short) sequenceFromIntRepresentation);
							if (indexPositions != null) {
								for (long subSequenceIndexInfo : indexPositions) {
									retrievedData.addSubSequenceInfoIntRepresention(subSequenceIndexInfo);
								}
							}
							subSequencesSearched.set(sequenceFromIntRepresentation);
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
			logger.info("Search total time:" + (System.currentTimeMillis() - init) + " and found " + retrievedData.getTotal() + " possible seeds");
			System.out.println("eco = " + eco);
			//retrievedData.compress();
			//retrievedData.filterData();
			// logger.info("Search total time:" + (System.currentTimeMillis() - init) + " and found " + retrievedData.getTotal() + " possible seeds");
			retrievedData = null;
			System.gc();

		}
	}

	private static class IndexRetrievedData {
		IntArray[] arrays;
		int size;

		long total;

		/**
		 * @param size
		 *            the qtd of lists
		 * @param initialSize
		 */
		@SuppressWarnings("unchecked")
		public IndexRetrievedData(int size, int initialSize) {
			this.size = size;

			arrays = new IntArray[size];
			for (int i = 0; i < size; i++) {
				arrays[i] = new IntArray(initialSize);
			}
		}

		void addSubSequenceInfoIntRepresention(long subSequenceInfoIntRepresention) {
			arrays[SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention)].add(SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention));
		}

		public long getTotal() {
			int total = 0;
			for (IntArray array : arrays) {
				if (array.getArray() != null){
				total += array.getArray().length;
			}
			}
			return total;
		}

		public void compress() {
			for (int a = 0; a < arrays.length; a++) {
				IntArray array = arrays[a];
//				System.out.print(a + ":[");
//				System.out.print(array.pos());
//				System.out.print("] ");
				if (array.getArray() != null) {
				Arrays.sort(array.getArray());
				}

//				for (int i = 0; i < array.pos(); i++) {
//					System.out.print(array.get(i));
//					System.out.print(",");
//				}
//				System.out.println();
			}
		}

		public void filterData() {
			int[] c = new int[999999];
			int previousPos;
			int offset;
			int score;
			int maxScore;
			int consecutives;
			int cccc = 0;
			int array[];
			for (IntArray results : arrays) {
				array = results.getArray();
				score = 0;
				maxScore = 0;
				consecutives = 0;
				if (array != null) {
					previousPos = array[0];
					for (int i = 1; i < array.length; i++) {
						offset = ((array[i] - previousPos) / 8) - 1;
						if (offset == 0) {
							consecutives++;
						} else {
							consecutives -= offset;
						}
						previousPos = array[i];
					}
					if (consecutives > 0) {
						c[cccc] = consecutives;
						//System.out.println(consecutives + " consecutivos.");
					}
					cccc++;
				}
			}
			Arrays.sort(c);
			for (int i = 999998; c[i] > 0; i--) {
				System.out.println(c[i]);
			}
		}

	}

}
