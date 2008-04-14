package bio.pih.search;

import java.io.Serializable;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.io.ConfigurationXMLReader;

/**
 * A class to hold parameters for a search. Now it is a bit useless, but when more parameters will be added, they should be stored here.
 * 
 * @author albrecht
 */
public class SearchParams implements Serializable {

	private static final long serialVersionUID = 6773155953856917786L;

	private final SymbolList query;
	private final String databankName;
	private final int minSimilarity;
	private final int maxDatabankSubSequencesDistance;
	private final int minMatchAreaLength;
	private final int maxQuerySubSequencesDistance;
	private final int minQuerySubSequence;
	private final int sequencesExtendDropoff;

	public static final int DEFAULT_MIN_SIMILARITY = ConfigurationXMLReader.getSubSequenceMinSimilarity();
	public static final int DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE = ConfigurationXMLReader.getDataBankMaxSubSequenceDistance();
	public static final int DEFAULT_MIN_MATCH_AREA_LENGTH = ConfigurationXMLReader.getDataBankMinMatchAreaLength();
	public static final int DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE = ConfigurationXMLReader.getQueryMaxSubSequenceDistance();
	public static final int DEFAULT_MIN_QUERY_SUB_SEQUENCE = ConfigurationXMLReader.getQueryMinSubSequenceLength();
	public static final int SEQUENCES_EXTEND_DROPOFF = ConfigurationXMLReader.getExtendDropoff();

	/**
	 * @param query
	 * @param databankName
	 */
	public SearchParams(SymbolList query, String databankName) {
		this(query, databankName, DEFAULT_MIN_SIMILARITY, DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE, DEFAULT_MIN_MATCH_AREA_LENGTH, DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE, DEFAULT_MIN_QUERY_SUB_SEQUENCE, SEQUENCES_EXTEND_DROPOFF);
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
	public SearchParams(SymbolList query, String databankName, int minSimilarity, int maxDatabankSequenceSubSequencesDistance, int minMatchAreaLength, int maxQuerySequenceSubSequencesDistance, int minQuerySequenceSubSequence, int sequencesExtendDropoff) {
		this.query = query;
		this.databankName = databankName;
		this.minSimilarity = minSimilarity;
		this.maxDatabankSubSequencesDistance = maxDatabankSequenceSubSequencesDistance;
		this.minMatchAreaLength = minMatchAreaLength;
		this.maxQuerySubSequencesDistance = maxQuerySequenceSubSequencesDistance;
		this.minQuerySubSequence = minQuerySequenceSubSequence;
		this.sequencesExtendDropoff = sequencesExtendDropoff;
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

	public int getSequencesExtendDropoff() {
		return sequencesExtendDropoff;
	}
}
