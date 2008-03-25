package bio.pih.search;

/**
 * A class that represent a found similar sub-sequence
 * 
 * @author albrecht
 */
public class MatchArea {
	
	/**
	 * @param encodedMatchZone
	 * @return the sequence id of the match area.
	 */
	public static long getSequenceIdFromEncodedMatchArea(long encodedMatchZone) {
		return encodedMatchZone >> 32;
	}
	
	/**
	 * @param encodedMatchArea
	 * @return the begin of the  match area,
	 */
	public static long getBeginFromEncodedMatchArea(long encodedMatchArea) {
		return (encodedMatchArea >> 12) & 0xFFFFF; 
	}
	
	/**
	 * @param encodedMatchArea
	 * @return the length of the  match area,
	 */
	public static long getLengthFromEncodedMatchArea(long encodedMatchArea) {
		return encodedMatchArea & 0xFFF;
	}
	
	/**
	 * 32 bits for sequence
	 * 20 for begin
	 * 12 for length
	 * @param sequence
	 * @param begin
	 * @param length
	 * @return encoded match area
	 */
	public static long encodeMatchArea(long sequence, long begin, long length) {
		long value = ((sequence << 32) | ((begin & 0xFFFFF) << 12) | (length & 0xFFF ));
		assert getSequenceIdFromEncodedMatchArea(value) == sequence;
		assert getBeginFromEncodedMatchArea(value) == begin;
		assert getLengthFromEncodedMatchArea(value) == length;
		return value;
	}
}
