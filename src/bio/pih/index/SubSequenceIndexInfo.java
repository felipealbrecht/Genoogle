package bio.pih.index;


/**
 * Class to store in the index information on witch sequence an sub sequences is in and its position.
 * 
 * Also provides methods to encoded these informations, sequence and position, into a integer.
 * @author albrecht
 */
public class SubSequenceIndexInfo {
	
	/**
	 * @param sequenceId 
	 * @param pos 
	 * @return an integer containing the sequenceId and start point
	 */
	public static int getSubSequenceInfoIntRepresention(int sequenceId, int pos) {
		//assert sequenceId <= 65535; //((long)1 << 16) -1; 
		//assert pos <= 65535; //((long)1 << 16) -1;
		return ((sequenceId << 16) | (pos & 0xFFFF));
	}

	/**
	 * @param subSequenceInfoIntRepresention 
	 * @return the start position
	 */
	public static int getStartFromSubSequenceInfoIntRepresentation(long subSequenceInfoIntRepresention) {
		return (int) (subSequenceInfoIntRepresention & 0xFFFF);
	}

	/**
	 * @param subSequenceInfoIntRepresention
	 * @return the sequence id 
	 */
	public static int getSequenceIdFromSubSequenceInfoIntRepresentation(long subSequenceInfoIntRepresention) {
		return (int) (subSequenceInfoIntRepresention >> 16) & 0xFFFF;
	}
}
