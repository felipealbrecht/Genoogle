/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009,2010,2011,2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

public class IndexSearchResults {
	
	private static Comparator<RetrievedSequenceAreas> AREAS_LENGTH_COMPARATOR = new Comparator<RetrievedSequenceAreas>() {
		@Override
		public int compare(final RetrievedSequenceAreas o1, final RetrievedSequenceAreas o2) {
			return o2.getBiggestLength() - o1.getBiggestLength();
		}
	};
		
	private final IndexSearcher[] searchers;
	List<RetrievedSequenceAreas> results = Lists.newLinkedList();
	boolean sorted = false;
		
	public IndexSearchResults(IndexSearcher ... searcher) {
		searchers = searcher;			
	}
	
	public void add(RetrievedSequenceAreas areas) {
		results.add(areas);
	}
	
	public RetrievedSequenceAreas get(int pos) {
		if (!sorted) {
			Collections.sort(results, AREAS_LENGTH_COMPARATOR);
			sorted = true;
		}
		return results.get(pos);
	}
		
	public IndexSearcher[] getIndexSearchers() {
		return searchers;
	}
		
	public int size() {
		return results.size();
	}
	
	public void merge(IndexSearchResults indexSearchResults) {
		this.results.addAll(indexSearchResults.results);
		sorted = false;
	}
}
