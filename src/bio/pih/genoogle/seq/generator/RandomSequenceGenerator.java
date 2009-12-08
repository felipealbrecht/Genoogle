/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq.generator;

import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.Sequence;

/**
 * A random sequence generator
 * @author Albrecht
 *
 */
public class RandomSequenceGenerator {

	private UniformDistribution dist;
	private final int lengthFrom;
	private final int lengthTo;
	int count;
	private final Alphabet alphabet;
	
	
	public RandomSequenceGenerator(Alphabet alphabet, int length) {
		this.alphabet = alphabet;
		this.lengthFrom = length;
		this.lengthTo = -1;
	}
	/**
	 * @param alphabet
	 * @param lengthFrom 
	 * @param lengthTo 
	 */
	public RandomSequenceGenerator(Alphabet alphabet, int lengthFrom, int lengthTo) {
		this.alphabet = alphabet;
		this.lengthFrom = lengthFrom;
		this.lengthTo   = lengthTo;
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
	public Sequence generateSequence() throws IllegalSymbolException {
		String name = "Generate Sequence " + getNext();
		String symbolList = dist.generateSymbolList(lengthFrom, lengthTo);		
		return new Sequence(name, alphabet, symbolList);
	}	
}
