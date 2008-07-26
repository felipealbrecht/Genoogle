package bio.pih.search.results;

import java.util.List;

import bio.pih.search.SearchParams;

import com.google.common.collect.Lists;

/**
 * Class that stores the results of a search
 * 
 * @author albrecht
 */
public class SearchResults {
	
	private final SearchParams params;
	private final List<Hit> hits;
	
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
	 * @param hit
	 */
	public void addHit(Hit hit) {
		this.hits.add(hit);
	}
	
	/**
	 * Add a {@link List} of {@link Hit} to this search result. 
	 * @param hits
	 */
	public void addAllHits(List<Hit> hits) {
		this.hits.addAll(hits);
	}
}