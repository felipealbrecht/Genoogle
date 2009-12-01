/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search.results;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The class stores a Hit from a search. 
 * 
 * @author albrecht
 *
 */
public class Hit {
	private final String id;
	private final String gi;
	private final String accession;
	private final String description;
	private final int length;
	private final String databankName;
	List<HSP> hsps;
	
	/**
	 * @param id
	 * @param gi 
	 * @param description
	 * @param accession
	 * @param hitLength
	 * @param databankName
	 */
	public Hit(String id, String gi, String description, String accession, int hitLength, String databankName) {
		this.id = id;
		this.gi = gi;
		this.accession = accession;
		this.description = description;
		this.length = hitLength;
		this.databankName = databankName;
		this.hsps = Lists.newLinkedList();
	}

	/**
	 * @return id of the sequence target.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return gi of the sequence target.
	 */
	public String getGi() {
		return gi;
	}
	
	/**
	 * @return description of the sequence target.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return accession of the sequence target.
	 */
	public String getAccession() {
		return accession;
	}
	
	/**
	 * @return length of the sequence target.
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return {@link List} of HSP related with this Hit.
	 */
	public List<HSP> getHSPs() {
		return this.hsps;
	}
	
	/**
	 * Add a new HSP to this Hit.
	 * @param hsp
	 */
	public void addHSP(HSP hsp) {
		this.hsps.add(hsp);
	}

	/**
	 * Add a list of HSPs to this Hit.
	 * @param hsps : {@link List} of {@link HSP}. 
	 */
	public void addAllHSP(List<HSP> hsps) {
		this.hsps.addAll(hsps);
	}
	
	/**
	 * @return data bank name where the Hit happened.
	 */
	public String getDatabankName() {
		return databankName;
	}
	
	private int totalScore = 0;
	private int getTotalScore() {
		if (totalScore == 0) {
			for (HSP hsp: hsps) {
				totalScore += hsp.getScore();
			}
		}
		return totalScore;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getId());
		sb.append(" ");
		sb.append(getDescription());
		sb.append(" ");
		sb.append(getDatabankName());
		return sb.toString();
	}
	
	/**
	 * Verify if the other Hit object is from the same Gi. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) == true) {
			return true;
		}
		if (!(obj instanceof Hit)) {
			return false;
		}
		
		Hit other = (Hit) obj;
		return other.gi.equals(gi);
	}
	
	@Override
	public int hashCode() {
		int hashCode = id.hashCode();
		hashCode *= gi.hashCode();
		hashCode *= accession.hashCode();
		hashCode *= description.hashCode();
		hashCode *= length;
		hashCode *= databankName.hashCode();
		hashCode *= hsps.hashCode();
		return hashCode;	
	}

	/**
	 * Comparator of two hits by them total score.
	 */
	public static final Comparator<Hit> COMPARATOR = new Comparator<Hit>() {
		@Override
		public int compare(Hit o1, Hit o2) {
			return Double.compare(o2.getTotalScore(), o1.getTotalScore());
		}
	};
}
