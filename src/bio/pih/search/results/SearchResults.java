package bio.pih.search.results;

import java.util.Collections;
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
		List<Hit> l = Lists.newLinkedList();
		this.hits = Collections.synchronizedList(l);
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


	public void addHit(Hit hit) {
		hits.add(hit);		
	}
	
	/**
	 * Merge another hits {@link List} into this search results.
	 * 
	 * @param newHits
	 */
	public void addAllHits(List<Hit> newHits) {
		this.hits.addAll(newHits);
	}

	/**
	 * Check if the search had problems.
	 * 
	 * @return <code>true</code> if happened a exception during the search process.
	 */
	public boolean hasFail() {
		return fails != null;
	}

	/**
	 * Add a {@link List} of {@link Exception} that happened during the execution.
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
	public synchronized void addFail(Exception fail) {
		if (fails == null) { 
			List<Exception> f =  Lists.newArrayList();
			fails = Collections.synchronizedList(f);
		}
		fails.add(fail);
	}

	/**
	 * @return {@link List} of {@link Exception} that happened during the execution.
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
