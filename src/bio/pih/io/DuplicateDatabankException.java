package bio.pih.io;

/**
 * @author albrecht
 */
public class DuplicateDatabankException extends Exception {

	private static final long serialVersionUID = 1237811559244598848L;
	private final String databankName;
	private final String databankCollection;

	/**
	 * @param databankName
	 * @param databankCollection
	 */
	public DuplicateDatabankException(String databankName, String databankCollection) {
		this.databankName = databankName;
		this.databankCollection = databankCollection;
	}
	
	/**
	 * @return
	 */
	public String getDatabankCollection() {
		return databankCollection;
	}
	
	
	/**
	 * @return
	 */
	public String getDatabankName() {
		return databankName;
	}
	

}
