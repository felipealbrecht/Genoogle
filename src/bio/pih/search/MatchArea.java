package bio.pih.search;

import bio.pih.util.IntArray;

/**
 * A class that represent a found similar sub-sequence
 * 
 * @author albrecht
 */
public class MatchArea {
	
	private final int sequenceId;
	private final int begin;
	private final int length;
	private final IntArray querySubSequences;
	
	/**
	 * @param sequenceId
	 * @param begin
	 * @param length
	 * @param querySubSequences
	 */
	public MatchArea(int sequenceId, int begin, int length, IntArray querySubSequences) {
		this.sequenceId = sequenceId;
		this.begin = begin;
		this.length = length;
		this.querySubSequences = querySubSequences;
	}
	
	/**
	 * @return sequenceId
	 */
	public int getSequenceId() {
		return sequenceId;
	}
	
	/**
	 * @return begin in the sequence
	 */
	public int getBegin() {
		return begin;
	}
	
	/**
	 * @return length of the match area
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return query subsequences.
	 */
	public IntArray getQuerySubSequences() {
		return querySubSequences;
	}
	
}
