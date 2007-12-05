package bio.pih.search;

/**
 * Informations about the search process
 * @author albrecht
 */
public interface SearchInformation {

	/**
	 * The step where the search is. 
	 * @author albrecht
	 *
	 */
	public enum Step {
		/**
		 * Not initialized yet
		 */
		NOT_INITIALIZED,
		/**
		 * Searching the seeds
		 */
		SEEDS,
		
		/**
		 * Doing the alignments
		 */
		ALIGNMENT,
		
		/**
		 * Selecting the best alignments 
		 */
		SELECTING,
		
		/**
		 * The search finalized
		 */
		FINISHED;
	}
	
	/**
	 * @return the actual search step.
	 */
	public Step getActualStep();
	
}
