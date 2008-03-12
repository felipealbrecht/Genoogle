package bio.pih.search;

public class MatchArea {

	
	public MatchArea(long encodedMatchZone) {
		
	}
	
	
	
	public static long getSequenceIdFromEncodedMatchArea(long encodedMatchZone) {
		return encodedMatchZone >> 32;
	}
	
	public static long getBeginFromEncodedMatchArea(long encodedMatchArea) {
		return (encodedMatchArea >> 12) & 0xFFFFF; 
	}
	
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
	 * @return
	 */
	public static long encodeMatchZone(long sequence, long begin, long length) {
		long value = ((sequence << 32) | ((begin & 0xFFFFF) << 12) | (length & 0xFFF ));
		assert getSequenceIdFromEncodedMatchArea(value) == sequence;
		assert getBeginFromEncodedMatchArea(value) == begin;
		assert getLengthFromEncodedMatchArea(value) == length;
		return value;
	}
}
