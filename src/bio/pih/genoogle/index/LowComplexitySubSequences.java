/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

import java.util.Arrays;
import java.util.List;

import org.biojava.bio.seq.DNATools;

import bio.pih.genoogle.encoder.DNASequenceEncoder;

import com.google.common.collect.Lists;

public class LowComplexitySubSequences {

	private final int bitsByAlphabetSize;
	private final int subSequenceLength;
	private final int bitsMask;
	private final int derivationLimit;

	private static final int alphabetSize = DNATools.getDNA().size();

	public LowComplexitySubSequences(int subSequenceLength, int derivationLimit) {
		this.subSequenceLength = subSequenceLength;
		this.derivationLimit = derivationLimit;
		this.bitsByAlphabetSize = DNASequenceEncoder.bitsByAlphabetSize(alphabetSize);
		this.bitsMask = ((1 << bitsByAlphabetSize) - 1);				
	}
	
	public int[] getSubSequences()  {		
		double total = 0.0;		
		
		final int maxSize = (int) Math.pow(alphabetSize, subSequenceLength);
		for (int i = 0; i < maxSize; i++) {
			double sequenceStandartDerivation = sequenceStandartDerivation(i);		
			total += sequenceStandartDerivation;
		}
		
		double varianceSumming = 0.0;
		final double m = total / maxSize;
		for (int i = 0; i < maxSize; i++) {
			double sequenceStandartDerivation = sequenceStandartDerivation(i);
			double d = sequenceStandartDerivation - m;
			varianceSumming += (d * d);
		}
		double variance = varianceSumming / maxSize;
		double standartDerivation = Math.sqrt(variance);
		
		List<Integer> lowComplexitySubSequences = Lists.newArrayList();
		double limit = m + (standartDerivation * this.derivationLimit);
		
		for (int i = 0; i < maxSize; i++) {
			double sequenceStandartDerivation = sequenceStandartDerivation(i);
			if (sequenceStandartDerivation > limit) {
				lowComplexitySubSequences.add(i);
			}
		}
		
		int[] lowIndex = new int[lowComplexitySubSequences.size()];
		for (int i = 0; i < lowComplexitySubSequences.size(); i++) {
			lowIndex[i] = lowComplexitySubSequences.get(i).intValue();
		}
		
		Arrays.sort(lowIndex);
		
		return lowIndex;
	}

	private double sequenceStandartDerivation(int encoded) {
		int ac = 0;
		int cc = 0;
		int gc = 0;
		int tc = 0;
		for (int pos = 0; pos < subSequenceLength; pos++) {
			int posInInt = subSequenceLength - pos;
			int shift = posInInt * bitsByAlphabetSize;
			int value = encoded >> (shift - bitsByAlphabetSize);
			value &= bitsMask;
			if (value == 0) {
				ac++;
			} else if (value == 1) {
				cc++;
			} else if (value == 2) {
				gc++;
			} else if (value == 3) {
				tc++;
			}
		}
		
		final int total = subSequenceLength;
		final double m = total / 4.0;

		double dma = ac - m;
		double dmc = cc - m;
		double dmg = gc - m;
		double dmt = tc - m;

		double variance = ((dma * dma) + (dmc * dmc) + (dmg * dmg) + (dmt * dmt)) / 4.0;
		return Math.sqrt(variance);

	}
}
