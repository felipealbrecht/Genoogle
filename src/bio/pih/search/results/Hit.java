package bio.pih.search.results;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The class stores the Hit from a search. 
 * 
 * @author albrecht
 *
 */
public class Hit {
	private final int hitNum;
	private final String id;
	private final String accession;
	private final String description;
	private final int length;
	private final String databankName;
	List<HSP> hsps;
	
	/**
	 * @param hitNum
	 * @param id
	 * @param description
	 * @param accession
	 * @param hitLength
	 * @param databankName
	 */
	public Hit(int hitNum, String id, String description, String accession, int hitLength, String databankName) {
		this.id = id;
		this.hitNum = hitNum;
		this.accession = accession;
		this.description = description;
		this.length = hitLength;
		this.databankName = databankName;
		this.hsps = Lists.newLinkedList();
	}
	
	/**
	 * @return number of the.
	 */
	public int getHitNum() {
		return hitNum;
	}

	/**
	 * @return id of the sequence target.
	 */
	public String getId() {
		return id;
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
	 * Comparator of two hits by them total score.
	 */
	public static final Comparator<Hit> COMPARATOR = new Comparator<Hit>() {
		@Override
		public int compare(Hit o1, Hit o2) {
			return Double.compare(o2.getTotalScore(), o1.getTotalScore());
		}
	};
}
