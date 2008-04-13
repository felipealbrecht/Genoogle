package bio.pih.search;

import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

public class ExtendSequences {
	SymbolList querySequenceExtended;
	SymbolList targetSequenceExtended;

	int queryLeftExtended, queryRightExtended, targetLeftExtended, targetRightExtended;

	public ExtendSequences(SymbolList querySequenceExtended, SymbolList targetSequenceExtended, int queryLeftExtended, int queryRightExtended, int targetLeftExtended, int targetRightExtended) {
		this.querySequenceExtended = querySequenceExtended;
		this.targetSequenceExtended = targetSequenceExtended;

		this.queryLeftExtended = queryLeftExtended;
		this.targetLeftExtended = targetLeftExtended;
		this.queryRightExtended = queryRightExtended;
		this.targetRightExtended = targetRightExtended;
	}

	public SymbolList getQuerySequenceExtended() {
		return querySequenceExtended;
	}

	public SymbolList getTargetSequenceExtended() {
		return targetSequenceExtended;
	}

	public int getQueryLeftExtended() {
		return queryLeftExtended;
	}

	public int getTargetLeftExtender() {
		return targetLeftExtended;
	}

	public int getQueryRightExtended() {
		return queryRightExtended;
	}

	public int getTargetRightExtender() {
		return targetRightExtended;
	}
	
	public static ExtendSequences doExtension(SymbolList querySequence, int beginQuerySegment, int endQuerySegment, SymbolList databankSequence, int beginDatabankSequenceSegment, int endDatabankSequenceSegment, int dropoff) {
		int score = 0;
		int bestScore = 0;
		int bestQueryPos, bestDatabankPos;
		int queryPos, databankPos;

		// Atention: biojava sequence symbols is from 1 to sequenceLength. It means that the first position is one and not zero!

		// right extend
		bestQueryPos = endQuerySegment;
		bestDatabankPos = endDatabankSequenceSegment;

		queryPos = endQuerySegment;
		databankPos = endDatabankSequenceSegment;

		while (queryPos < querySequence.length() && databankPos < databankSequence.length()) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos);
			if (symbolAtQuery == symbolAtDatabank) {
				score++;
				if (score > bestScore) {
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

		while (queryPos > 0 && databankPos > 0) {
			Symbol symbolAtQuery = querySequence.symbolAt(queryPos + 1);
			Symbol symbolAtDatabank = databankSequence.symbolAt(databankPos + 1);
			if (symbolAtQuery == symbolAtDatabank) {
				score++;
				if (score > bestScore) {
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

		return new ExtendSequences(queryExtended, targetExtended, queryLeftExtended, queryRightExtend, targetLeftExtended, targetRightExtended);
	}
}