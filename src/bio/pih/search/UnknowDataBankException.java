package bio.pih.search;


/**
 * Exception that is throw when is tried to access an unknown data bank.
 * @author albrecht
 *
 */
public class UnknowDataBankException extends Exception {

	private static final long serialVersionUID = -5092176329117667144L;
	private final SearchManager searchManager;
	private final String databank;

	/**
	 * @param searchManager
	 * @param databank
	 */
	public UnknowDataBankException(SearchManager searchManager, String databank) {
		this.searchManager = searchManager;
		this.databank = databank;
	}
	
	@Override
	public String toString() {
		return "Unknow databank " + databank + " at " + searchManager;
	}

}
