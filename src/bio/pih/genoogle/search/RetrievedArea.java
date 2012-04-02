package bio.pih.genoogle.search;

import bio.pih.genoogle.io.Utils;


/**
 * HSP
 * 
 * @author albrecht
 */
public final class RetrievedArea {
	private int queryAreaBegin;
	private int queryAreaEnd;
	private int sequenceAreaBegin;
	private int sequenceAreaEnd;
	private int length;

	public RetrievedArea(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
		reset(queryAreaBegin, sequenceAreaBegin, subSequenceLength);
	}

	private RetrievedArea(int queryAreaBegin, int queryAreaEnd, int sequenceAreaBegin, int sequenceAreaEnd, int length) {
		this.queryAreaBegin = queryAreaBegin;
		this.queryAreaEnd = queryAreaEnd;
		this.sequenceAreaBegin = sequenceAreaBegin;
		this.sequenceAreaEnd = sequenceAreaEnd;
		this.length = length;
	}

	public void reset(int queryAreaBegin, int sequenceAreaBegin, int subSequenceLength) {
		this.queryAreaBegin = queryAreaBegin;
		this.queryAreaEnd = queryAreaBegin + subSequenceLength;
		this.sequenceAreaBegin = sequenceAreaBegin;
		this.sequenceAreaEnd = sequenceAreaBegin + subSequenceLength;
		this.length = subSequenceLength;
	}

	public RetrievedArea copy() {
		return new RetrievedArea(queryAreaBegin, queryAreaEnd, sequenceAreaBegin, sequenceAreaEnd, length);
	}

	public int length() {
		return this.length;
	}

	public boolean testAndSet(final int newQueryPos, final int newSequencePos, final int maxSubSequenceDistance, final int subSequenceLength) {

		if (Utils.isIn(queryAreaBegin, queryAreaEnd + maxSubSequenceDistance, newQueryPos)) {
			if (Utils.isIn(sequenceAreaBegin, sequenceAreaEnd + maxSubSequenceDistance, newSequencePos)) {

				int newQueryPosEnd = newQueryPos + subSequenceLength;
				if (newQueryPosEnd > this.queryAreaEnd) {
					this.queryAreaEnd = newQueryPosEnd;
				}

				int newSequencePosEnd = newSequencePos + subSequenceLength;
				if (newSequencePosEnd > this.sequenceAreaEnd) {
					this.sequenceAreaEnd = newSequencePosEnd;
				}

				this.length = Math.min(queryAreaEnd - queryAreaBegin, sequenceAreaEnd - sequenceAreaBegin);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("([");
		sb.append(queryAreaBegin);
		sb.append(",");
		sb.append(queryAreaEnd);
		sb.append("]");
		sb.append("[");
		sb.append(sequenceAreaBegin);
		sb.append(",");
		sb.append(sequenceAreaEnd);
		sb.append("]:");
		sb.append(length);
		sb.append(")");

		return sb.toString();
	}

	public int getQueryAreaBegin() {
		return queryAreaBegin;
	}

	public int getQueryAreaEnd() {
		return queryAreaEnd;
	}

	public int getSequenceAreaBegin() {
		return sequenceAreaBegin;
	}

	public int getSequenceAreaEnd() {
		return sequenceAreaEnd;
	}

}
