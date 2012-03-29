/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import bio.pih.genoogle.alignment.SubstitutionMatrix;
import bio.pih.genoogle.encoder.SequenceEncoder;

/**
 * Extend sequences by its similarity to the right and to the left.
 * 
 * @author albrecht
 */
public class ExtendSequences {

	private final int[] encodedQuery;
	private final int[] encodedTarget;
	private final int beginQuerySegment;
	private final int endQuerySegment;
	private final int beginTargetSegment;
	private final int endTargetSegment;

	public ExtendSequences(int[] encodedQuery, int[] encodedTarget, int beginQuerySegment, int endQuerySegment,
			int beginTargetSegment, int endTargetSegment) {
		this.encodedQuery = encodedQuery;
		this.encodedTarget = encodedTarget;

		this.beginQuerySegment = beginQuerySegment;
		this.endQuerySegment = endQuerySegment;
		this.beginTargetSegment = beginTargetSegment;
		this.endTargetSegment = endTargetSegment;
	}

	String queryExtendedString = null;

	public int getBeginQuerySegment() {
		return beginQuerySegment;
	}

	public int getEndQuerySegment() {
		return endQuerySegment;
	}

	public int getBeginTargetSegment() {
		return beginTargetSegment;
	}

	public int getEndTargetSegment() {
		return endTargetSegment;
	}

	public int[] getEncodedQuery() {
		return encodedQuery;
	}

	public int[] getEncodedTarget() {
		return encodedTarget;
	}

	/**
	 * @param encodedQuerySequence
	 * @param beginQuerySegment
	 * @param endQuerySegment
	 * @param encodedDatabankSequence
	 * @param beginDatabankSequenceSegment
	 * @param endDatabankSequenceSegment
	 * @param dropoff
	 * @param subSequenceLength
	 * @param encoder
	 * 
	 * @return {@link ExtendSequences} of the extended sequences.
	 */
	public static ExtendSequences doExtension(int[] encodedQuerySequence, int beginQuerySegment, int endQuerySegment,
			int[] encodedDatabankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment,
			int dropoff, SequenceEncoder extensionEncoder, final SubstitutionMatrix substitutionTable) {
		int score = 0;
		int bestScore = 0;
		int bestQueryPos, bestDatabankPos;
		int queryPos, databankPos;
		
		final int subSequenceLength = extensionEncoder.getSubSequenceLength();

		// right extend
		bestQueryPos = endQuerySegment;
		bestDatabankPos = endDatabankSequenceSegment;

		queryPos = endQuerySegment + 1;
		databankPos = endDatabankSequenceSegment + 1;

		int queryLength = SequenceEncoder.getSequenceLength(encodedQuerySequence);
		int databankLength = SequenceEncoder.getSequenceLength(encodedDatabankSequence);

		// http://2.bp.blogspot.com/_a7jkcMVp5Vg/SMMSwfT7jXI/AAAAAAAAF5Q/vrtrqwk-z1c/s1600-h/usetheforce.jpg
		while (queryPos < queryLength && databankPos < databankLength) {
			int queryValue = extensionEncoder.getValueAtPos(encodedQuerySequence, queryPos, subSequenceLength);
			int databankValue = extensionEncoder.getValueAtPos(encodedDatabankSequence, databankPos, subSequenceLength);

			if (substitutionTable == null) {
				if (queryValue == databankValue) {
					score++;
				}
			} else {
				char a = extensionEncoder.getSymbolFromBits(queryValue);
				char b = extensionEncoder.getSymbolFromBits(databankValue);				
				score += substitutionTable.getValue(a, b);				
			}
			
			if (score >= bestScore) {
				bestScore = score;
				bestQueryPos = queryPos;
				bestDatabankPos = databankPos;
			} 
			if (bestScore - score > dropoff) {
				break;
			}
			queryPos++;
			databankPos++;
		}

		int rightBestQueryPos = bestQueryPos;
		int rightBestDatabankPos = bestDatabankPos;

		// left extend
		score = 0;
		bestScore = 0;

		bestQueryPos = beginQuerySegment;
		bestDatabankPos = beginDatabankSequenceSegment;

		queryPos = beginQuerySegment - 1;
		databankPos = beginDatabankSequenceSegment - 1;

		while (queryPos >= 0 && databankPos >= 0) {
			int queryValue = extensionEncoder.getValueAtPos(encodedQuerySequence, queryPos, subSequenceLength);
			int databankValue = extensionEncoder.getValueAtPos(encodedDatabankSequence, databankPos, subSequenceLength);
			if (substitutionTable == null) {
				if (queryValue == databankValue) {
					score++;
				}
			} else {
				char a = extensionEncoder.getSymbolFromBits(queryValue);
				char b = extensionEncoder.getSymbolFromBits(databankValue);				
				score += substitutionTable.getValue(a, b);				
			}
			
			if (score >= bestScore) {
				bestScore = score;
				bestQueryPos = queryPos;
				bestDatabankPos = databankPos;
			} 
			if (bestScore - score > dropoff) {
				break;
			}
			queryPos--;
			databankPos--;
		}

		return new ExtendSequences(encodedQuerySequence, encodedDatabankSequence, bestQueryPos, rightBestQueryPos, bestDatabankPos, rightBestDatabankPos);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object anObject) {
		if (super.equals(anObject)) {
			return true;
		}

		if (!(anObject instanceof ExtendSequences)) {
			return false;
		}

		ExtendSequences other = (ExtendSequences) anObject;

		if (this.getBeginQuerySegment() != other.getBeginQuerySegment()) {
			return false;
		}

		if (this.getBeginTargetSegment() != other.getBeginTargetSegment()) {
			return false;
		}

		if (this.getEndQuerySegment() != other.getEndQuerySegment()) {
			return false;
		}

		if (this.getEndTargetSegment() != other.getEndTargetSegment()) {
			return false;
		}

		if (!(this.encodedQuery == other.getEncodedQuery())) {
			return false;
		}

		if (!(this.encodedTarget == other.getEncodedTarget())) {
			return false;
		}

		return true;
	}
}