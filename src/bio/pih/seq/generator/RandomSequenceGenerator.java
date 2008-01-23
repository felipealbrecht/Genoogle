package bio.pih.seq.generator;

import java.util.Random;

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

	private Random random; 
	private Distribution dist;
	private final int lengthFrom;
	private final int lengthTo;
	int count;
	
	/**
	 * @param alphabet
	 * @param lengthFrom 
	 * @param lengthTo 
	 * @param length
	 */
	public RandomSequenceGenerator(FiniteAlphabet alphabet, int lengthFrom, int lengthTo) {
		this.lengthFrom = lengthFrom;
		this.lengthTo   = lengthTo;
		random = new Random();
		dist = new UniformDistribution(alphabet);
		count = -1;
	}
	
	/**
	 * @param alphabet
	 * @param lengthFrom
	 */
	public RandomSequenceGenerator(FiniteAlphabet alphabet, int lengthFrom) {
		this.lengthFrom = lengthFrom;
		this.lengthTo   = -1;
		random = new Random();
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
		if (lengthTo == -1) {
			return DistributionTools.generateSequence("Generate Sequence " + getNext(), dist, lengthFrom);
		}
		int value = random.nextInt(lengthTo - lengthFrom) + lengthFrom;
		return DistributionTools.generateSequence("RandomSequence_" + getNext(), dist, value);
	}	
}
