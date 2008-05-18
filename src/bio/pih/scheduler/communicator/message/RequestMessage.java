package bio.pih.scheduler.communicator.message;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.search.SearchParams;

/**
 * A message that is send by the Server to the Worker for him to execute a query.
 * 
 * @author albrecht
 */
public class RequestMessage extends Message {
	private static final long serialVersionUID = 7129366659656051697L;

	SearchParams sp;
	long code;

	/**
	 * A message representing a request
	 * 
	 * @param sp
	 * @param code            
	 */
	public RequestMessage(SearchParams sp, long code) {
		this.sp = sp;
		this.code = code;
	}

	@Override
	public MessageKind getKind() {
		return MessageKind.REQUEST;
	}

	/**
	 * Get the <code>query</code>, that is, the sequence that will be searched againds the <code>database</code>.
	 * 
	 * @return the <code>query</code>
	 */
	public SymbolList getQuery() {
		return sp.getQuery();
	}

	/**
	 * Get the database where will be performed the search.
	 * 
	 * @return data bank name.
	 */
	public String getDatabase() {
		return sp.getDatabank();
	}

	/**
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return code of this request.
	 */
	public long getCode() {
		return code;
	}
	
	/**
	 * @return {@link SearchParams} of this request.
	 */
	public SearchParams getSearchParams() {
		return sp;
	}

	@Override
	public String toString() {
		return "Searching " + getQuery().seqString() + " at " + getDatabase();
	}
}
