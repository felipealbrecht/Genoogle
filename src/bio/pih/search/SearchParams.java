package bio.pih.search;

/**
 * @author albrecht
 *
 */
public class SearchParams {

	String database;
	String query;
	
	
	/**
	 * @param database
	 * @param query
	 */
	public SearchParams(String database, String query) {
		this.database = database;
		this.query = query;
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
	
}
