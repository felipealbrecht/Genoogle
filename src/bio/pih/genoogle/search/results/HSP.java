/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search.results;

import java.util.Comparator;

import bio.pih.genoogle.alignment.GenoogleSequenceAlignment;
import bio.pih.genoogle.alignment.GenoogleSmithWaterman;

/**
 * The class stores a HSP from a search.
 * 
 * @author albrecht
 */
public class HSP {

	private static final long serialVersionUID = -7701610542981141900L;

	private final GenoogleSequenceAlignment alignment;
	private final double normalizedScore;
	private final double eValue;

	private final int queryFrom;
	private final int queryTo;
	private final int hitFrom;
	private final int hitTo;

	public HSP(GenoogleSequenceAlignment alignment, int queryFrom, int queryTo, int hitFrom, int hitTo,
			double normalizedScore, double eValue) {
		this.alignment = alignment;
		this.queryFrom = queryFrom;
		this.queryTo = queryTo;
		this.hitFrom = hitFrom;
		this.hitTo = hitTo;
		this.normalizedScore = normalizedScore;
		this.eValue = eValue;
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
		return queryFrom;
	}

	/**
	 * @return where the query ends at this HSP.
	 */
	public int getQueryTo() {
		return queryTo;
	}

	/**
	 * @return where the target begins at this HSP.
	 */
	public int getHitFrom() {
		return hitFrom;
	}

	/**
	 * @return where the target ends at this HSP.
	 */
	public int getHitTo() {
		return hitTo;
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
	public GenoogleSequenceAlignment getAlignment() {
		return alignment;
	}

	/**
	 * @return normalize score of this HSP.
	 */
	public double getNormalizedScore() {
		return normalizedScore;
	}

	/**
	 * @return E-Value of this HSP.
	 */
	public double getEValue() {
		return eValue;
	}

	/**
	 * Comparator of two HSP using them scores.
	 */
	public static final Comparator<HSP> COMPARATOR = new Comparator<HSP>() {
		@Override
		public int compare(HSP o1, HSP o2) {
			GenoogleSequenceAlignment osw1 = o1.getAlignment();
			GenoogleSequenceAlignment osw2 = o2.getAlignment();
			return Double.compare(osw2.getScore(), osw1.getScore());
		}
	};

}
