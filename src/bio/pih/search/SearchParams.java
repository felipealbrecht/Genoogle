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
	private final int minSimilarity;
	private final int maxDatabankSubSequencesDistance;
	private final int maxQuerySubSequencesDistance;
	private final int sequencesExtendDropoff;
	private final double minEvalue;

	/**
	 * Default similarity threshold at the similar sub-sequences index. 
	 */
	public static final int DEFAULT_MIN_SIMILARITY = XMLConfigurationReader.getSubSequenceMinSimilarity();
	
	/**
	 * Default maximum distance between two sub-sequences of a sequence at data bank to be considered at same area.
	 */
	public static final int DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE = XMLConfigurationReader.getDataBankMaxSubSequenceDistance();
	
	/**
	 * Default maximum distance between two sub-sequences of a query sequence to be considered at same area.
	 */
	public static final int DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE = XMLConfigurationReader.getQueryMaxSubSequenceDistance();
	
	
	/**
	 * Drop off for sequences extension.
	 */
	public static final int SEQUENCES_EXTEND_DROPOFF = XMLConfigurationReader.getExtendDropoff();
	
	/**
	 * Minimum value for E-Value
	 */
	public static final double MIN_EVALUE = XMLConfigurationReader.getMinEvalue();

	/**
	 * @param query
	 * @param databankName
	 */
	public SearchParams(SymbolList query, String databankName) {
		this(query, databankName, DEFAULT_MIN_SIMILARITY, DEFAULT_MAX_DATABANK_SUB_SEQUENCE_DISTANCE, DEFAULT_MAX_QUERY_SUB_SEQUENCE_DISTANCE, SEQUENCES_EXTEND_DROPOFF, MIN_EVALUE);
	}

	/**
	 * @param query
	 * @param databankName
	 * @param minSimilarity
	 * @param maxDatabankSequenceSubSequencesDistance
	 * @param minMatchAreaLength
	 * @param maxQuerySequenceSubSequencesDistance
	 * @param minQuerySequenceSubSequence
	 * @param sequencesExtendDropoff 
	 * @param minEvalue 
	 */
	public SearchParams(SymbolList query, String databankName, int minSimilarity, int maxDatabankSequenceSubSequencesDistance, int maxQuerySequenceSubSequencesDistance, int sequencesExtendDropoff, double minEvalue) {
		this.query = query;
		this.databankName = databankName;
		this.minSimilarity = minSimilarity;
		this.maxDatabankSubSequencesDistance = maxDatabankSequenceSubSequencesDistance;
		this.maxQuerySubSequencesDistance = maxQuerySequenceSubSequencesDistance;
		this.sequencesExtendDropoff = sequencesExtendDropoff;
		this.minEvalue = minEvalue;
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
	 * @return similarity threshold at the similar sub-sequences index. 
	 */
	public int getMinSimilarity() {
		return minSimilarity;
	}

	/**
	 * @return maximum distance between two sub-sequences of a sequence at data bank to be considered at same area.
	 */
	public int getMaxDatabankSequenceSubSequencesDistance() {
		return maxDatabankSubSequencesDistance;
	}

	/**
	 * @return maximum distance between two sub-sequences of a query sequence to be considered at same area.
	 */
	public int getMaxQuerySequenceSubSequencesDistance() {
		return maxQuerySubSequencesDistance;
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
}
