package bio.pih.search;

import bio.pih.io.SequenceDataBank;

public class UnknowDataBankException extends Exception {

	private final SearchManager searchManager;
	private final String databank;

	public UnknowDataBankException(SearchManager searchManager, String databank) {
		this.searchManager = searchManager;
		this.databank = databank;
	}
	
	@Override
	public String toString() {
		return "Unknow databank " + databank + " at " + searchManager;
	}

}
