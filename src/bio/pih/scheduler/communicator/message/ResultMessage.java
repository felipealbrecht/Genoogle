package bio.pih.scheduler.communicator.message;

import bio.pih.search.results.HSP;


/**
 * @author albrecht
 *
 */
public class ResultMessage extends Message {

	private static final long serialVersionUID = -5762601490806905960L;
	String db;
	String query;
	long code;
	HSP[] alignments;
	
	/**
	 * @param db 
	 * @param query 
	 * @param code 
	 * @param alignments 
	 */
	public ResultMessage(String db, String query, long code, HSP[] alignments) {
		this.db = db;
		this.query = query;
		this.code = code;
		this.alignments = alignments;
	}
	
	
	/**
	 * @param db
	 */
	public void setDb(String db) {
		this.db = db;
	}
	
	/**
	 * @return
	 */
	public String getDb() {
		return db;
	}
	
	/**
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}
	
	/**
	 * @return
	 */
	public String getQuery() {
		return query;
	}
	
	
	/**
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * @return
	 */
	public long getCode() {
		return code;
	}
	
	/**
	 * @param alignments
	 */
	public void setAlignments(HSP[] alignments) {
		this.alignments = alignments.clone();
	}
	
	/**
	 * @return
	 */
	public HSP[] getAlignments() {
		return alignments.clone();
	}
	
	
	@Override
	public String toString() {
		return "ResultMessage (code: "+code+") from " + query + " agains " + db;
	}
		
	@Override
	public MessageKind getKind() {
		return MessageKind.RESULT;
	}	
}
