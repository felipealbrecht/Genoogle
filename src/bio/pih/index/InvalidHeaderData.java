package bio.pih.index;

/**
 * @author albrecht
 *
 * Throw when some data as an invalid header. 
 */
public class InvalidHeaderData extends Exception {
	
	/**
	 * @param the message
	 */
	public InvalidHeaderData(String string) {
		super(string);
	}

	private static final long serialVersionUID = -1149058987449988029L;	
}
