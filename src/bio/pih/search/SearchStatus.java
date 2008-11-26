package bio.pih.search;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.results.SearchResults;

/**
 * Class that hold informations about one search process.
 * 
 * @author albrecht
 */
public class SearchStatus {

	/**
	 * The step where the search is.
	 * 
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
		 * Extending the matchs found.
		 */
		EXTENDING,

		/**
		 * Doing the alignments.
		 */
		ALIGNMENT,

		/**
		 * Sorting the alignments.
		 */
		SORTING,

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

	protected final long code;
	protected final long timeBegin;
	protected long timeEnd;
	protected final SequenceDataBank db;
	protected final SearchParams sp;
	protected SearchResults results;
	protected SearchStep actualStep;

	/**
	 * @param code
	 * @param sp
	 * @param db
	 * @param sm
	 * @param parent
	 */
	public SearchStatus(long code, SearchParams sp, SequenceDataBank db) {
		this.code = code;
		this.timeBegin = System.currentTimeMillis();
		this.sp = sp;
		this.db = db;
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
		if ((step == SearchStep.FINISHED) || (step == SearchStep.FATAL_ERROR)) {
			this.timeEnd = System.currentTimeMillis();
		}
	}

	@Override
	public String toString() {
		return "Query '" + sp.getQuery().seqString() + "' against " + db.getName() + " ("
				+ actualStep + ")";
	}

	/**
	 * @param results
	 *            of this Search.
	 */
	public void setResults(SearchResults results) {
		this.results = results;
	}

	/**
	 * @return {@link SearchResults} of this Search.
	 */
	public SearchResults getResults() {
		return results;
	}
}
