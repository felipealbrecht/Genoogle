package bio.pih.search;

import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

/**
 * Extend sequences by its similarity to the right and to the left.
 * 
 * @author albrecht
 */
public class ExtendSequences {
	private SymbolList querySequenceExtended;
	private SymbolList targetSequenceExtended;

	private int queryLeftExtended, queryRightExtended, targetLeftExtended, targetRightExtended;

	private final int beginTargetSegment;
	private final int beginQuerySegment;

	private ExtendSequences(SymbolList querySequenceExtended, SymbolList targetSequenceExtended, int queryLeftExtended, int queryRightExtended, int targetLeftExtended, int targetRightExtended, int beginTargetSegment, int beginQuerySegment) {
		this.querySequenceExtended = querySequenceExtended;
		this.targetSequenceExtended = targetSequenceExtended;

		this.queryLeftExtended = queryLeftExtended;
		this.targetLeftExtended = targetLeftExtended;
		this.queryRightExtended = queryRightExtended;
		this.targetRightExtended = targetRightExtended;
		this.beginTargetSegment = beginTargetSegment;
		this.beginQuerySegment = beginQuerySegment;
		
	}

	/**
	 * @return extended query.
	 */
	public SymbolList getQuerySequenceExtended() {
		return querySequenceExtended;
	}

	/**
	 * @return extended target.
	 */
	public SymbolList getTargetSequenceExtended() {
		return targetSequenceExtended;
	}

	/**
	 * @return how many bases the query was extended to left.
	 */
	public int getQueryLeftExtended() {
		return queryLeftExtended;
	}

	/**
	 * @return how many bases the target was extended to left.
	 */
	public int getTargetLeftExtended() {
		return targetLeftExtended;
	}

	/**
	 * @return how many bases the query was extended to right.
	 */
	public int getQueryRightExtended() {
		return queryRightExtended;
	}

	/**
	 * @return how many bases the target was extended to right.
	 */
	public int getTargetRightExtended() {
		return targetRightExtended;
	}
	
	/**
	 * @return difference between the query segment and the extended.
	 */
	public int getQueryOffset() {
		return beginQuerySegment - this.getQueryLeftExtended();
	}

	/**
	 * @return difference between the target segment and the extended.
	 */
	public int getTargetOffset() {
		return beginTargetSegment - this.getTargetLeftExtended();
	}
	
	/**
	 * Do the extension of the query and target.
	 * 
	 * @param querySequence
	 * @param beginQuerySegment
	 * @param endQuerySegment
	 * @param databankSequence
	 * @param beginDatabankSequenceSegment
	 * @param endDatabankSequenceSegment
	 * @param dropoff
	 * @param beginQuerySequence
	 * @param beginTargetSequence
	 * @return
	 */
	public static ExtendSequences doExtension(SymbolList querySequence, int beginQuerySegment, int endQuerySegment, SymbolList databankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment, int dropoff, int beginQuerySequence, int beginTargetSequence) {
		int score = 0;
		int bestScore = 0;
		int bestQueryPos, bestDatabankPos;
		int queryPos, databankPos;

		// Attention: biojava sequence symbols is from 1 to sequenceLength. It means that the first position is one and not zero!

		// right extend
		bestQueryPos = endQuerySegment;
		bestDatabankPos = endDatabankSequenceSegment;

		queryPos = endQuerySegment;
		databankPos = endDatabankSequenceSegment;

		while (queryPos <= querySequence.length() && databankPos <= databankSequence.length()) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos);
			if (symbolAtQuery == symbolAtDatabank) {
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

		queryPos = beginQuerySegment;
		databankPos = beginDatabankSequenceSegment;

		while (queryPos >= 0 && databankPos >= 0) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos + 1);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos + 1);
			if (symbolAtQuery == symbolAtDatabank) {
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

		SymbolList queryExtended = querySequence.subList(bestQueryPos + 1, rightBestQueryPos);
		SymbolList targetExtended = databankSequence.subList(bestDatabankPos + 1, rightBestDatabankPos);

		int queryLeftExtended = beginQuerySegment - bestQueryPos;
		int queryRightExtend = rightBestQueryPos - endQuerySegment;
		int targetLeftExtended = beginDatabankSequenceSegment - bestDatabankPos;
		int targetRightExtended = rightBestDatabankPos - endDatabankSequenceSegment;

		return new ExtendSequences(queryExtended, targetExtended, queryLeftExtended, queryRightExtend, targetLeftExtended, targetRightExtended, beginQuerySegment, beginTargetSequence);
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
		
		if (this.getQueryOffset() != other.getQueryOffset()) {
			return false;
		}
		
		if (this.getTargetOffset() != other.getTargetOffset()) {
			return false;
		}
		
		if (!(this.querySequenceExtended.seqString().equals(other.querySequenceExtended.seqString()))) {
			return false;
		}
		
		if (!(this.targetSequenceExtended.seqString().equals(other.targetSequenceExtended.seqString()))) {
			return false;
		}
				
		return true;
	}
	
	
}