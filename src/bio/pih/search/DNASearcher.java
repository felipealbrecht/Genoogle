package bio.pih.search;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.search.SearchInformation.SearchStep;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.IntArray;
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
			//logger.setLevel(Level.ERROR);
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory().newSymbolListWindowIterator(sequence, 8);

			int totalSubsequences = 0;

			IndexRetrievedData indexRetrievedData = new IndexRetrievedData(databank.getTotalSequences()/20);

			logger.info("Begining the search of sequence with " + sequence.length() + "bases " + sequence.getString());

			long init = System.currentTimeMillis();
			try {
				while (symbolListWindowIterator.hasNext()) {
					//long iterationBegin = System.currentTimeMillis();
					totalSubsequences++;
					LightweightSymbolList subSequence = (LightweightSymbolList) symbolListWindowIterator.next();
					short encodedSubSequence = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(subSequence);

					Map<Short, int[]> similarSubSequencesMap = databank.getSimilarSubSequence(encodedSubSequence, 7);

					for (int[] similarSubSequences: similarSubSequencesMap.values()) {				
						for (int subSequenceInfoIntRepresention : similarSubSequences) {
							indexRetrievedData.addSubSequenceInfoIntRepresention(subSequenceInfoIntRepresention);
						}
					}
					//System.out.println("Iteracao " + totalSubsequences + " demorou: " + (System.currentTimeMillis() - iterationBegin));
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
			logger.info("Search total time:" + (System.currentTimeMillis() - init) + " and found " + indexRetrievedData.getTotal() + " possible seeds");
			indexRetrievedData = null;
			System.gc();
			

		}

		void searchSeeds() {

		}
	}

	private static class IndexRetrievedData {
		IntArray[] arrays;
		int size;
		
		long total;

		/**
		 * @param size the qtd of lists
		 */
		@SuppressWarnings("unchecked")
		public IndexRetrievedData(int size) {
			this.size = size;
			this.total = 0;

			arrays = new IntArray[size];
			for (int i = 0; i < size; i++) {
				arrays[i] = new IntArray(500);
			}
		}

		void addSubSequenceInfoIntRepresention(int subSequenceInfoIntRepresention) {
			total++;
			arrays[SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention) % size].add(subSequenceInfoIntRepresention);
		}
		
		public long getTotal() {
			return total;
		}

	}

}
