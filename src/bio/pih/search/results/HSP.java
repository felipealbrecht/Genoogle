package bio.pih.search.results;

import java.io.Serializable;
import java.util.Comparator;

import bio.pih.alignment.GenoogleSmithWaterman;

/**
 * @author albrecht
 * 
 */
public class HSP implements Serializable {

	private static final long serialVersionUID = -7701610542981141900L;

	private final GenoogleSmithWaterman alignment;
	private final int queryOffset;
	private final int targetOffset;
	private final int num;

	/**
	 * @param num
	 * @param alignment
	 * @param queryOffset
	 * @param targetOffset
	 */
	public HSP(int num, GenoogleSmithWaterman alignment, int queryOffset, int targetOffset) {
		this.num = num;
		this.alignment = alignment;
		this.queryOffset = queryOffset;
		this.targetOffset = targetOffset;
	}

	/**
	 * @return number of this HSP.
	 */
	public int getNum() {
		return num;
	}
	
	/**
	 * @return score of this HSP.
	 */
	public double getScore() {
		return alignment.getScore();
	}
	
	/**
	 * @return where the query begins at this HSP.
	 */
	public int getQueryFrom() {
		return queryOffset + alignment.getQueryStart();
	}
	
	/**
	 * @return where the query ends at this HSP.
	 */	
	public int getQueryTo() {
		return queryOffset + alignment.getQueryEnd();
	}
	
	/**
	 * @return where the target begins at this HSP.
	 */
	public int getHitFrom() {
		return targetOffset + alignment.getTargetStart();
	}
	
	/**
	 * @return where the target ends at this HSP.
	 */
	public int getHitTo() {
		return targetOffset + alignment.getTargetEnd();
	}
	
	/**
	 * @return identity length of this HSP.
	 */
	public int getIdentityLength() {
		return alignment.getIdentitySize();
	}
	
	/**
	 * @return alignment length of this HSP.
	 */
	public int getAlignLength() {
		return alignment.getPath().length();
	}
	
	/**
	 * @return representation of the aligned query.
	 */
	public String getQuerySeq() {
		return alignment.getQueryAligned();
	}

	/**
	 * @return representation of the aligned target.
	 */
	public String getTargetSeq() {
		return alignment.getTargetAligned();
	}
	
	/**
	 * @return representation of the aligned path.
	 */
	public String getPathSeq() {
		return alignment.getPath();
	}
	
	/**
	 * @return {@link GenoogleSmithWaterman} containing the alignment informations.
	 */
	public GenoogleSmithWaterman getAlignment() {
		return alignment; 
	}
	
		
	/**
	 * Comparator of two HSP using them scores.
	 */
	public static final Comparator<HSP> COMPARATOR = new Comparator<HSP>() {
		@Override
		public int compare(HSP o1, HSP o2) {
			GenoogleSmithWaterman osw1 = o1.getAlignment();
			GenoogleSmithWaterman osw2 = o2.getAlignment();
			return Double.compare(osw2.getScore(), osw1.getScore());
		}
	};
	
}
