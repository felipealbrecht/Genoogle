package bio.pih.search;

import java.util.List;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.SequenceDataBank;

/**
 * Class that hold informations about one search process.
 * @author albrecht
 */
public class SearchStatus {
	
	/**
	 * The step where the search is. 
	 * @author albrecht
	 *
	 */
	public enum SearchStep {
		/**
		 * Not initialized yet
		 */
		NOT_INITIALIZED,
		
		/**
		 * Initialized
		 */
		INITIALIZED,
		
		
		/**
		 * Searching the data in the Index.
		 */
		INDEX_SEARCH,
		
		/**
		 * Computing the match areas found.
		 */
		COMPUTING_MATCHS,
				
		/**
		 * Searching and creating the seeds.
		 */
		SEEDS,
		
		/**
		 * Doing the alignments.
		 */
		ALIGNMENT,
		
		/**
		 * Selecting the best alignments. 
		 */
		SELECTING,
		
		
		/**
		 * Waiting the search result of its inner data banks.
		 */
		SEARCHING_INNER,
		
		/**
		 * The search finalized.
		 */
		FINISHED,
		
			
		/**
		 * The search was canceled and stopped.
		 */
		CANCELED,
		
		
		/**
		 * 
		 */
		ERASED;
	}
	
	SequenceDataBank db;
	SymbolList query;
	SearchStep actualStep;
	List<AlignmentResult> results;
	long code;
		
	/**
	 * @param db
	 * @param query
	 * @param code 
	 */
	public SearchStatus(SymbolList query, SequenceDataBank db, long code) {
		this.db = db;
		this.query = query;
		this.code = code;
		this.actualStep = SearchStep.NOT_INITIALIZED;
		this.results = null;
	}
	
	/**
	 * @param db
	 */
	public void setDb(SequenceDataBank db) {
		this.db = db;
	}
	
	
	/**
	 * @return db
	 */
	public SequenceDataBank getDb() {
		return db;
	}
	
	/**
	 * @param query
	 */
	public void setQuery(SymbolList query) {
		this.query = query;
	}
	
	/**
	 * @return query
	 */
	public SymbolList getQuery() {
		return query;
	}
	
	/**
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * @return
	 */
	public long getCode() {
		return code;
	}
	
	/**
	 * @return the actual search step.
	 */
	public SearchStep getActualStep() {
		return actualStep;
	}
	
	/**
	 * @param step
	 */
	public void setActualStep(SearchStep step) {
		this.actualStep = step;		
	}
	
	@Override
	public String toString() {
		return "Query '"+ query + "' against " + db + " ("+actualStep+")";
	}
	
	public void setResults(List<AlignmentResult> results) {
		this.results = results;
	}
	
	public List<AlignmentResult> getResults() {
		return results;
	}
	
	
	public boolean isDone() {
		if (this.getActualStep() == SearchStep.FINISHED) {
			return true;
		}		
		return false;
	}
	
}
