package bio.pih.search.results;

import java.util.Collections;
import java.util.Comparator;

import bio.pih.alignment.GenoogleSmithWaterman;

/**
 * @author albrecht
 * 
 */
public class HSP {

	private static final long serialVersionUID = -7701610542981141900L;

	private final String query;
	private final GenoogleSmithWaterman alignment;
	private final int sequenceId;
	private final String databankName;
	private final int queryOffset;
	private final int targetOffset;

	/**
	 * @param query
	 * @param alignment
	 * @param sequenceId
	 * @param databankName
	 * @param queryOffset
	 * @param targetOffset
	 * @param sequence
	 * @param pontuation
	 */
	public HSP(String query, GenoogleSmithWaterman alignment, int sequenceId, String databankName, int queryOffset, int targetOffset) {
		this.query = query;
		this.alignment = alignment;
		this.sequenceId = sequenceId;
		this.databankName = databankName;
		this.queryOffset = queryOffset;
		this.targetOffset = targetOffset;
	}

	public String getQuery() {
		return query;
	}

	public GenoogleSmithWaterman getAlignment() {
		return alignment;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public String getDatabankName() {
		return databankName;
	}

	public int getQueryOffset() {
		return queryOffset;
	}

	public int getTargetOffset() {
		return targetOffset;
	}

	public static Comparator<HSP> getScoreComparetor() {
		return new Comparator<HSP>() {
			@Override
			public int compare(HSP o1, HSP o2) {
				GenoogleSmithWaterman osw1 = o1.getAlignment();
				GenoogleSmithWaterman osw2 = o2.getAlignment();
				return Double.compare(osw2.getScore(), osw1.getScore());
			}
		};
	}
}
