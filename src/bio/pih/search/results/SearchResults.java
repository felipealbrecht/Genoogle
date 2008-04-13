package bio.pih.search.results;

import java.util.List;

import bio.pih.search.SearchParams;

import com.google.common.collect.Lists;

public class SearchResults {
	
	private final SearchParams params;
	private final List<Hit> hits;
	
	public SearchResults(SearchParams params) {
		this.params = params;
		this.hits = Lists.newLinkedList();
	}
	
	public SearchParams getParams() {
		return params;
	}
	
	public List<Hit> getHits() {
		return hits;
	}
	
	public void addHit(Hit hit) {
		this.hits.add(hit);
	}
	
	public void addAllHits(List<Hit> hits) {
		this.hits.addAll(hits);
	}
}
