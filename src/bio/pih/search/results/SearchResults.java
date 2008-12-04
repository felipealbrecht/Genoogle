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
	private int minSubSequenceLength;

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
	 * @param id
	 * @param hit
	 */
	public void addHit(Hit hit) {
		this.hits.add(hit);
	}

	/**
	 * Merge another Hash of hits into this search results.
	 * 
	 * @param newHits
	 */
	public void addAllHits(List<Hit> newHits) {
		for (Hit h: newHits) {			
			int i = this.hits.indexOf(h);
			if (i >= 0) {
				this.hits.get(i).addAllHSP(h.getHSPs());
			} else {
				this.addHit(h);
			}			
		}
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
			fails = Lists.newArrayList();
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
			fails = Lists.newArrayList();
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
	
	public void setMinSubSequenceLength(int minSubSequenceLength) {
		this.minSubSequenceLength = minSubSequenceLength;
	}
	
	public int getMinSubSequenceLength() {
		return minSubSequenceLength;
	}
}
