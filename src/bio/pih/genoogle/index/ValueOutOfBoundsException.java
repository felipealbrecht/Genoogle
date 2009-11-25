/**
 * 
 */
package bio.pih.genoogle.index;

/**
 * Exception when the parameter value is lower or higher than a pre set limit.
 * <p>@author Albrecht
 */
public class ValueOutOfBoundsException extends RuntimeException {

	private static final long serialVersionUID = -526404286506434331L;
	
	/**
	 * @param message
	 */
	public ValueOutOfBoundsException(String message) {
		super(message);
	}
	

}
