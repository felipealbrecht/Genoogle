package bio.pih.search.results;

import java.util.List;

import bio.pih.search.SearchParams;

import com.google.common.collect.Lists;

/**
 * Class that stores the results and the fails of a search
 * 
 * @author albrecht
 */
public class SearchResults {

	private final SearchParams params;
	private final List<Hit> hits;
	private List<Exception> fails = null;

	/**
	 * @param params
	 */
	public SearchResults(SearchParams params) {
		this.params = params;
		this.hits = Lists.newLinkedList();
	}

	/**
	 * @return parameters of this search.
	 */
	public SearchParams getParams() {
		return params;
	}

	/**
	 * @return {@link List} of {@link Hit} of this search.
	 */
	public List<Hit> getHits() {
		return hits;
	}

	/**
	 * Add a new {@link Hit} to this search result.
	 * 
	 * @param hit
	 */
	public void addHit(Hit hit) {
		this.hits.add(hit);
	}

	/**
	 * Add a {@link List} of {@link Hit} to this search result.
	 * 
	 * @param hits
	 */
	public void addAllHits(List<Hit> hits) {
		this.hits.addAll(hits);
	}

	/**
	 * Check if the search had problems.
	 * 
	 * @return <code>true</code> if happened a exception during the search
	 *         process.
	 */
	public boolean hasFail() {
		return fails != null;
	}

	/**
	 * Add a {@link List} of {@link Exception} that happened during the
	 * execution.
	 * 
	 * @param fail
	 */
	public void addAllFails(List<Exception> fail) {
		if (fails == null) {
			fails = Lists.newLinkedList();
		}
		fails.addAll(fail);
	}

	/**
	 * Add a {@link Exception} that happened during the execution.
	 * 
	 * @param fail
	 */
	public void addFail(Exception fail) {
		if (fails == null) {
			fails = Lists.newLinkedList();
		}
		fails.add(fail);
	}

	/**
	 * @return {@link List} of {@link Exception} that happened during the
	 *         execution.
	 */
	public List<Exception> getFails() {
		return fails;
	}

	public void mergeInverted(SearchResults invertedSearchResults) {
		if (invertedSearchResults.hasFail()) {
			addAllFails(invertedSearchResults.getFails());
			return;
		}
		
		addAllHits(invertedSearchResults.getHits());				
	}
}
