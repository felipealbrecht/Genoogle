package bio.pih.genoogle.search;

import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;

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
	private final DNASequenceEncoderToInteger encoder;

	public ExtendSequences(int[] encodedQuery, int[] encodedTarget, int beginQuerySegment, int endQuerySegment,
			int beginTargetSegment, int endTargetSegment, DNASequenceEncoderToInteger encoder) {
		this.encodedQuery = encodedQuery;
		this.encodedTarget = encodedTarget;

		this.beginQuerySegment = beginQuerySegment;
		this.endQuerySegment = endQuerySegment;
		this.beginTargetSegment = beginTargetSegment;
		this.endTargetSegment = endTargetSegment;
		this.encoder = encoder;
	}

	String queryExtendedString = null;

	/**
	 * @return extended query.
	 * @throws IllegalSymbolException
	 */
	public String getQuerySequenceExtended() {
		if (queryExtendedString == null) {
			queryExtendedString = encoder.decodeIntegerArrayToString(encodedQuery, beginQuerySegment, endQuerySegment);
		}
		return queryExtendedString;
	}

	String targetExtendedString = null;

	/**
	 * @return extended target.
	 * @throws IllegalSymbolException
	 */
	public String getTargetSequenceExtended() {
		if (targetExtendedString == null) {
			targetExtendedString = encoder.decodeIntegerArrayToString(encodedTarget, beginTargetSegment,
					endTargetSegment);
		}
		return targetExtendedString;
	}

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

	public DNASequenceEncoderToInteger getEncoder() {
		return encoder;
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
	 * TODO: Remove the param subSequenceLength and get from the encoder.
	 * 
	 * @return {@link ExtendSequences} of the extended sequences.
	 */
	public static ExtendSequences doExtension(int[] encodedQuerySequence, int beginQuerySegment, int endQuerySegment,
			int[] encodedDatabankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment,
			int dropoff, int subSequenceLength, DNASequenceEncoderToInteger encoder) {
		int score = 0;
		int bestScore = 0;
		int bestQueryPos, bestDatabankPos;
		int queryPos, databankPos;

		// right extend
		bestQueryPos = endQuerySegment;
		bestDatabankPos = endDatabankSequenceSegment;

		queryPos = endQuerySegment + 1;
		databankPos = endDatabankSequenceSegment + 1;

		int queryLength = encodedQuerySequence[0];
		int databankLength = encodedDatabankSequence[0];

		// http://2.bp.blogspot.com/_a7jkcMVp5Vg/SMMSwfT7jXI/AAAAAAAAF5Q/vrtrqwk-z1c/s1600-h/usetheforce.jpg
		while (queryPos < queryLength && databankPos < databankLength) {
			int queryValue = DNASequenceEncoderToInteger.getValueAtPos(encodedQuerySequence, queryPos,
					subSequenceLength);
			int databankValue = DNASequenceEncoderToInteger.getValueAtPos(encodedDatabankSequence, databankPos,
					subSequenceLength);
			if (queryValue == databankValue) {
				score++;
				if (score >= bestScore) {
					bestScore = score;
					bestQueryPos = queryPos;
					bestDatabankPos = databankPos;
				}
			} else {
				score--;
				if (bestScore - score > dropoff) {
					break;
				}
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
			int queryValue = DNASequenceEncoderToInteger.getValueAtPos(encodedQuerySequence, queryPos,
					subSequenceLength);
			int databankValue = DNASequenceEncoderToInteger.getValueAtPos(encodedDatabankSequence, databankPos,
					subSequenceLength);
			if (queryValue == databankValue) {
				score++;
				if (score >= bestScore) {
					bestScore = score;
					bestQueryPos = queryPos;
					bestDatabankPos = databankPos;
				}
			} else {
				score--;
				if (bestScore - score > dropoff) {
					break;
				}
			}
			queryPos--;
			databankPos--;
		}

		return new ExtendSequences(encodedQuerySequence, encodedDatabankSequence, bestQueryPos, rightBestQueryPos, bestDatabankPos, rightBestDatabankPos, encoder);
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