package bio.pih.search;

/**
 * A class to hold parameters for a search.
 * Now it is a bit useless, but when more parameters will be added, they should be stored here.
 *  
 * @author albrecht
 */
public class SearchParams {

	String database;
	String query;
	long code;
	
	
	/**
	 * @param database
	 * @param query
	 * @param code 
	 */
	public SearchParams(String database, String query, long code) {
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
	 * @return the code of the associate search or -1 if is not associate with anyone Search
	 */
	public long getCode() {
		return code;
	}
	
}
