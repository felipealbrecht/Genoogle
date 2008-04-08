package bio.pih.search;

import org.biojava.bio.symbol.SymbolList;

/**
 * A class to hold parameters for a search. Now it is a bit useless, but when more parameters will be added, they should be stored here.
 * 
 * @author albrecht
 */
public class SearchParams {

	private final SymbolList query;
	private final String databankName;
	private final int minSimilarity;
	private final int maxDatabankSubSequencesDistance;
	private final int minMatchAreaLength;
	private final int maxQuerySubSequencesDistance;
	private final int minQuerySubSequence;

	public static final int DEFAULT_MIN_SIMILARITY = 4;
	public static final int DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE = 23;
	public static final int DEFAULT_MIN_MATCH_AREA_LENGTH = 24;
	public static final int DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE = 23;
	public static final int DEFAULT_MIN_QUERY_SUB_SEQUENCE = 24;

	/**
	 * @param query
	 * @param databankName
	 */
	public SearchParams(SymbolList query, String databankName) {
		this(query, databankName, DEFAULT_MIN_SIMILARITY, DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE, DEFAULT_MIN_MATCH_AREA_LENGTH, DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE, DEFAULT_MIN_QUERY_SUB_SEQUENCE);
	}

	/**
	 * @param query
	 * @param databankName
	 * @param minSimilarity
	 * @param maxDatabankSequenceSubSequencesDistance
	 * @param minMatchAreaLength
	 * @param maxQuerySequenceSubSequencesDistance
	 * @param minQuerySequenceSubSequence
	 */
	public SearchParams(SymbolList query, String databankName, int minSimilarity, int maxDatabankSequenceSubSequencesDistance, int minMatchAreaLength, int maxQuerySequenceSubSequencesDistance, int minQuerySequenceSubSequence) {
		this.query = query;
		this.databankName = databankName;
		this.minSimilarity = minSimilarity;
		this.maxDatabankSubSequencesDistance = maxDatabankSequenceSubSequencesDistance;
		this.minMatchAreaLength = minMatchAreaLength;
		this.maxQuerySubSequencesDistance = maxQuerySequenceSubSequencesDistance;
		this.minQuerySubSequence = minQuerySequenceSubSequence;
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

	public int getMinSimilarity() {
		return minSimilarity;
	}

	public int getMaxDatabankSequenceSubSequencesDistance() {
		return maxDatabankSubSequencesDistance;
	}

	public int getMinMatchAreaLength() {
		return minMatchAreaLength;
	}

	public int getMaxQuerySequenceSubSequencesDistance() {
		return maxQuerySubSequencesDistance;
	}

	public int getMinQuerySequenceSubSequence() {
		return minQuerySubSequence;
	}
}
