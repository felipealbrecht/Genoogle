package bio.pih.search;

import java.io.Serializable;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.XMLConfigurationReader;

/**
 * A class to hold parameters for a search. Now it is a bit useless, but when more parameters will be added, they should be stored here.
 * 
 * @author albrecht
 */
public final class SearchParams implements Serializable {

	private static final long serialVersionUID = 6773155953856917786L;

	private final SymbolList query;
	private final String databankName;
	private final int maxSubSequencesDistance;
	private final int sequencesExtendDropoff;
	private final double minEvalue;
	private final int maxHitsResults;

	private int maxThreadsIndexSearch;

	private int minQuerySliceLength;
	
	/**
	 * Default maximum distance between two sub-sequences of a sequence to be considered at same area.
	 */
	public static final int DEFAULT_MAX_SUB_SEQUENCE_DISTANCE = XMLConfigurationReader.getMaxSubSequenceDistance();
	
	/**
	 * Drop off for sequences extension.
	 */
	public static final int SEQUENCES_EXTEND_DROPOFF = XMLConfigurationReader.getExtendDropoff();
	
	/**
	 * Minimum value for E-Value
	 */
	public static final double MIN_EVALUE = XMLConfigurationReader.getMinEvalue();
	
	/**
	 * Quantity of hits that will be processed and shown.
	 */
	public static final int MAX_HITS_RESULTS = XMLConfigurationReader.getMaxResults();
	
	public static final int MAX_THREADS_INDEX_SEARCH = XMLConfigurationReader.getMaxThreadsIndexSearch();
	
	public static final int MIN_QUERY_SLICE_LENGTH = XMLConfigurationReader.getMinQuerySliceLength();

	public SearchParams(SymbolList query, String databankName) {
		this(query, databankName, DEFAULT_MAX_SUB_SEQUENCE_DISTANCE, SEQUENCES_EXTEND_DROPOFF, MIN_EVALUE, 
				MAX_HITS_RESULTS, MAX_THREADS_INDEX_SEARCH, MIN_QUERY_SLICE_LENGTH);
	}

	public SearchParams(SymbolList query, String databankName, int maxSubSequencesDistance, int sequencesExtendDropoff, double minEvalue, int maxHitsResults, int maxThreadsIndexSearch, int minQuerySliceLength) {
		this.query = query;
		this.databankName = databankName;
		this.maxSubSequencesDistance = maxSubSequencesDistance;
		this.sequencesExtendDropoff = sequencesExtendDropoff;
		this.minEvalue = minEvalue;
		this.maxHitsResults = maxHitsResults;
		this.maxThreadsIndexSearch = maxThreadsIndexSearch;
		this.minQuerySliceLength = minQuerySliceLength;
	}

	/**
	 * @return the query of the search
	 */
	public SymbolList getQuery() {
		return query;
	}

	/**
	 * @return the data bank where the search will be performed
	 */
	public String getDatabank() {
		return databankName;
	}

	/**
	 * @return maximum distance between two sub-sequences to be considered at same area.
	 */
	public int getMaxSubSequencesDistance() {
		return maxSubSequencesDistance;
	}
	
	/**
	 * @return drop off for sequences extension.
	 */
	public int getSequencesExtendDropoff() {
		return sequencesExtendDropoff;
	}
	
	/**
	 * @return the minimum value of the E-value
	 */
	public double getMinEvalue() {
		return minEvalue;
	}
	
	/**
	 * @return the quantity of hits that will be processed and shown.
	 */
	public int getMaxHitsResults() {
		return maxHitsResults;
	}

	public int getMaxThreadsIndexSearch() {
		return maxThreadsIndexSearch;
	}

	public int getMinQuerySliceLength() {
		return minQuerySliceLength;
	}
}
