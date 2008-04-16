package bio.pih.search;

import bio.pih.io.SequenceDataBank;
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
		 * When occurs a fatal error and the search process was not completed.
		 */
		FATAL_ERROR;
	}
	
	protected final long timeBegin;
	protected long timeEnd;
	protected final SequenceDataBank db;
	protected final SearchParams sp;
	protected SearchResults results;
	protected SearchStep actualStep;
	private final Searcher parent;
		
	/**
	 * @param searcher 
	 * @param sp
	 * @param query 
	 * @param db
	 * @param parent 
	 * @param code
	 */
	public SearchStatus(SearchParams sp, SequenceDataBank db, Searcher parent) {
		this.timeBegin = System.currentTimeMillis();
		this.sp = sp;
		this.db = db;
		this.parent = parent;
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
			if (parent != null) {				
				parent.setFinished(this);
			}
		}
		
		if (step == SearchStep.FATAL_ERROR) {
			this.timeEnd = System.currentTimeMillis();
			if (parent != null) {
				// TODO: Set fatal error at the parent.
				parent.setFinished(this);
			}
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
}
