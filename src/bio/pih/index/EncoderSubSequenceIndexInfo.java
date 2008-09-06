package bio.pih.index;


/**
 * Class to store in the index information on witch sequence an sub sequences is in and its position.
 * 
 * Also provides methods to encoded these informations, sequence and position, into a integer.
 * @author albrecht
 */
public class EncoderSubSequenceIndexInfo {
	
	private static final int DATA_32_BITS_MASK = 0xFFFFFFFF;

	/**
	 * @param sequenceId 
	 * @param pos 
	 * @return an integer containing the sequenceId and start point
	 */
	public static long getSubSequenceInfoIntRepresention(int sequenceId, int pos) {
		assert sequenceId <= ((long) 1 << 32) - 1;
		return ((((long)sequenceId) << 32) | (pos & DATA_32_BITS_MASK));
	}

	/**
	 * @param subSequenceInfoIntRepresention 
	 * @return the start position
	 */
	public static int getStart(long subSequenceInfoIntRepresention) {
		return (int) (subSequenceInfoIntRepresention & DATA_32_BITS_MASK);
	}

	/**
	 * @param subSequenceInfoIntRepresention
	 * @return the sequence id 
	 */
	public static int getSequenceId(long subSequenceInfoIntRepresention) {
		return (int) (subSequenceInfoIntRepresention >> 32) & DATA_32_BITS_MASK;
	}
}
