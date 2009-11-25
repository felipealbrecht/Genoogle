package bio.pih.genoogle.io;

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
	 * @return data bank collection where have two data banks with the same name. 
	 */
	public String getDatabankCollection() {
		return databankCollection;
	}
	
	
	/**
	 * @return name of the duplicate data bank.
	 */
	public String getDatabankName() {
		return databankName;
	}
	

}
