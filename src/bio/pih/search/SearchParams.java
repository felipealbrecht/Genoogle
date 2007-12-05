package bio.pih.search;

/**
 * @author albrecht
 *
 */
public interface SearchParams {

	/**
	 * @return the database where the search will be performed
	 */
	String getDatabase();
	/**
	 * @return the query of the search
	 */
	String getQuery();
	
}
