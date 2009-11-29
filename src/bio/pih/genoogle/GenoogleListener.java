package bio.pih.genoogle;

/**
 * Interface for the classes which should be notified by {@link Genoogle} about changes.
 * 
 * @author albrecht
 */
public interface GenoogleListener {

	/**
	 * Notify the listener to finish its instance.
	 */
	public void finish();
}
