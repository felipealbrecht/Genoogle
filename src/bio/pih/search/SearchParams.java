package bio.pih.search;

/**
 * @author albrecht
 *
 */
public class SearchParams {

	String database;
	String query;
	int code;
	
	
	/**
	 * @param database
	 * @param query
	 * @param code 
	 */
	public SearchParams(String database, String query, int code) {
		this.database = database;
		this.query = query;
		this.code = code;
	}
	
	/**
	 * @return the database where the search will be performed
	 */
	public String getDatabase() {
		return database;
	}
	/**
	 * @return the query of the search
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * @return
	 */
	public int getCode() {
		return code;
	}
	
}
