package bio.pih.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.search.SearchInformation.SearchStep;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

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

	public long doSearch(SymbolList input, IndexedSequenceDataBank bank) {
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

	private class SimilarSearcher implements Runnable {
		final SymbolList sequence;
		final IndexedSequenceDataBank databank;

		short[] encodedSequence;

		public SimilarSearcher(SymbolList sequence, IndexedSequenceDataBank databank) {
			this.sequence = sequence;
			this.databank = databank;
		}

		public void run() {
			SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory.getOverlappedFactory().newSymbolListWindowIterator(sequence, 8);			

			// SequenceId, Positions			
			TreeMultimap<Integer,Integer> sequenceIdPositions = Multimaps.newTreeMultimap();
			int sequenceId;
			int position;
			
			try {
				while (symbolListWindowIterator.hasNext()) {
					SymbolList subSequence = symbolListWindowIterator.next();
					short encodedSubSequence = DNASequenceEncoderToShort.getDefaultEncoder().encodeSubSymbolListToShort(subSequence);
					Map<Short, List<Integer>> similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence, 1);
					for (Short similarSubSequence: similarSubSequences.keySet()) {
						for (Integer subSequenceInfoIntRepresention: similarSubSequences.get(similarSubSequence)) {
							sequenceId = SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);
							position = SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(subSequenceInfoIntRepresention);
							
							sequenceIdPositions.put(sequenceId, position);							 
						}
					}
				}
				
			} catch (ValueOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidHeaderData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		

		void searchSeeds() {

		}
	}

}
