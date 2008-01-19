package bio.pih.seq.generator;

import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.DistributionTools;
import org.biojava.bio.dist.UniformDistribution;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;

/**
 * A random sequence generator
 * @author Albrecht
 *
 */
public class RandomSequenceGenerator {

	private Distribution dist;
	private final int length;
	int count;
	
	/**
	 * @param alphabet
	 * @param length
	 */
	public RandomSequenceGenerator(FiniteAlphabet alphabet, int length) {
		this.length = length;
		dist = new UniformDistribution(alphabet);
		count = -1;
	}
	
	/**
	 * Reset the number that is append at the end of the generate sequences.
	 */
	public void resetCount() {
		count = -1;
	}
	
	private int getNext() {
		return ++count;
	}
	
	/**
	 * @return a new generate sequence;
	 */
	public Sequence generateSequence() {
			return DistributionTools.generateSequence("Generate Sequence " + getNext(), dist, length);
	}	
}
