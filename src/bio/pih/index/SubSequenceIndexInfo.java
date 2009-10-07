package bio.pih.index;


/**
 * Class to store in the index information on witch sequence an sub sequences is in and its position.
 * 
 * Also provides methods to encoded these informations, sequence and position, into a integer.
 * @author albrecht
 */
public class SubSequenceIndexInfo {
	
	private static final int DATA_32_BITS_MASK = 0xFFFFFFFF;

	/**
	 * Create an index info representation for the given sequenceId and position.
	 * @param sequenceId - Id of the sequence.
	 * @param pos - Position in the sequence.
	 * @return the encoded value containing the sequenceId and start point
	 */
	public static long newIndexInfo(long sequenceId, long pos) {
		if (sequenceId > ((long) 1 << 32) - 1) {
			throw new RuntimeException(sequenceId + "is too high. Should be fill into a integer.");
		}
		if (pos > ((long) 1 << 32) - 1) {
			throw new RuntimeException(pos + "is too high. Should be fill into a integer.");
		}
		return (((sequenceId) << 32) | (pos & DATA_32_BITS_MASK));
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
