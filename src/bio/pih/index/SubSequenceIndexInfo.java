package bio.pih.index;

import java.io.Serializable;

/**
 * Class to store in the index information on witch sequence an sub sequences is in and its position.
 * 
 * Also provides methods to encoded these informations, sequence and position, into a integer.
 * @author albrecht
 */
public class SubSequenceIndexInfo implements Serializable {
	
	private static final long serialVersionUID = -4573843446834948261L;
	
	private int sequenceId;
	private int start ;
	
	/**
	 * @param sequenceId
	 * @param sequenceEncoded 
	 * @param start
	 */
	public SubSequenceIndexInfo(int sequenceId, int start) {
		this.sequenceId = sequenceId;
		this.start = start;
	}
	
	/**
	 * @param sequenceId
	 */
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	/**
	 * @return
	 */
	public int getSequenceId() {
		return sequenceId;
	}
	
	/**
	 * @param start
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	/**
	 * @return
	 */
	public int getStart() {
		return start;
	}
		
	@Override
	public String toString() {
		return this.sequenceId + " ("+start+")";
	}
	
	/**
	 * @param sequenceId 
	 * @param getStart 
	 * @return an integer containing the sequenceId and start point
	 */
	public static int getSubSequenceInfoIntRepresention(int sequenceId, int getStart) {
		return ((sequenceId << 16) | (getStart & 0xFFFF));
	}

	/**
	 * @param subSequenceInfoIntRepresention 
	 * @return the start position
	 */
	public static int getStartFromSubSequenceInfoIntRepresentation(int subSequenceInfoIntRepresention) {
		return (subSequenceInfoIntRepresention & 0xFFFF);
	}

	/**
	 * @param subSequenceInfoIntRepresention 
	 * @return the sequence id
	 */
	public static int getSequenceIdFromSubSequenceInfoIntRepresentation(int subSequenceInfoIntRepresention) {
		return (subSequenceInfoIntRepresention >> 16) & 0xFFFF;
	}
}
