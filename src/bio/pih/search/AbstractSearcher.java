package bio.pih.search;

import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;

/**
 * This interface defines the methods that are presents in a similar sequence
 * searcher.
 * 
 * The doSearch method works asynchronous, returning an unique identifier for
 * the solicited search. The verifySearch return the current status of the
 * search. Others methods are for
 * 
 * @author albrecht
 */
public abstract class AbstractSearcher {

	protected SearchStatus status = null;
	protected AbstractSearcher parent;
	protected Thread ss;

	/**
	 * @param id
	 * @param sp
	 *            Parameter of the search
	 * @param bank
	 *            Sequence data bank where the search will be performed.
	 * @param sm
	 * @param parent
	 *            The parent of this search.
	 */
	public AbstractSearcher(long id, SearchParams sp, SequenceDataBank bank, SearchManager sm,
			AbstractSearcher parent) {
		status = new SearchStatus(id, sp, bank, sm, parent);
		status.setActualStep(SearchStep.NOT_INITIALIZED);

	}

	/**
	 * Start an asynchronous search for similar sequences against the sequence data bank.
	 */
	public void doSearch() {
		ss.start();
	}

	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus() {
		return status;
	}

	/**
	 * Alert this search that its soon was finished.
	 * 
	 * @param searchStatus
	 * @return <code>true</code> if the search was found and marked as finished.
	 */
	public boolean setFinished(SearchStatus searchStatus) {
		throw new UnsupportedOperationException("This Searcher do not support sons.");
	}

}
