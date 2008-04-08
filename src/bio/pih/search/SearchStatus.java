package bio.pih.search;

import java.util.List;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

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
	
	protected final long timeBegin;
	protected long timeEnd;
	protected final SequenceDataBank db;
	protected final SearchParams sp;
	protected final long code;
	protected SearchResults results;
	protected SearchStep actualStep;
		
	/**
	 * @param sp
	 * @param query 
	 * @param db
	 * @param code
	 */
	public SearchStatus(SearchParams sp, SequenceDataBank db, long code) {
		this.timeBegin = System.currentTimeMillis();
		this.sp = sp;
		this.db = db;
		this.code = code;
		this.actualStep = SearchStep.NOT_INITIALIZED;
		this.results = null;
	}
		
	/**
	 * @return db
	 */
	public SequenceDataBank getDb() {
		return db;
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
		if (step == SearchStep.FINISHED) {
			this.timeEnd = System.currentTimeMillis();
		}
	}
	
	@Override
	public String toString() {
		return "Query '"+ sp.getQuery().seqString() + "' against " + db.getName() + " ("+actualStep+")";
	}
	
	public void setResults(SearchResults  results) {
		this.results = results;
	}
	
	public SearchResults getResults() {
		return results;
	}
	
	
	public boolean isDone() {
		if (this.getActualStep() == SearchStep.FINISHED) {
			return true;
		}		
		return false;
	}
	
}
