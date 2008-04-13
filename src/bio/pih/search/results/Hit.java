package bio.pih.search.results;

import java.util.Comparator;
import java.util.List;

import bio.pih.alignment.GenoogleSmithWaterman;

import com.google.common.collect.Lists;

public class Hit {

	private final int hitNum;
	private final String id;
	private final String accession;
	private final String description;
	private final int length;
	private final String databankName;
	List<HSP> hsps;
	
	public Hit(int hitNum, String id, String description, String accession, int hitLength, String databankName) {
		this.id = id;
		this.hitNum = hitNum;
		this.accession = accession;
		this.description = description;
		this.length = hitLength;
		this.databankName = databankName;
		this.hsps = Lists.newLinkedList();
	}
	
	public int getHitNum() {
		return hitNum;
	}

	public String getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getAccession() {
		return accession;
	}
	
	public int getLength() {
		return length;
	}
	
	public List<HSP> getHSPs() {
		return this.hsps;
	}
	
	public void addHSP(HSP hsp) {
		this.hsps.add(hsp);
	}
	
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

	public static final Comparator<Hit> COMPARATOR = new Comparator<Hit>() {
		@Override
		public int compare(Hit o1, Hit o2) {
			return Double.compare(o2.getTotalScore(), o1.getTotalScore());
		}
	};
}
