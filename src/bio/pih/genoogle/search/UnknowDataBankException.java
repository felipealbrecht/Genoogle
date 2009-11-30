package bio.pih.genoogle.search;

/**
 * Exception that is throw when is tried to access an invalid data bank.
 * 
 * @author albrecht
 */
public class UnknowDataBankException extends Exception {

	private static final long serialVersionUID = -5092176329117667144L;
	private final String databank;

	/**
	 * @param the invalid data bank name 
	 */
	public UnknowDataBankException(String databank) {
		this.databank = databank;
	}

	@Override
	public String toString() {
		return "Unknow databank " + databank + ".";
	}

}
