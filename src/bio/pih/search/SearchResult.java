package bio.pih.search;

import java.util.List;

import org.biojava.bio.seq.Sequence;

/**
 * @author albrecht
 * <b>DUMMY AND INCOMPLETE INTERFACE!</b>
 */
public interface SearchResult {
	
	/**
	 * @return a list with the similar sequences.
	 */
	List<Sequence> getSequences();

}
