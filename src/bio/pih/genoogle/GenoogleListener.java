/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

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
