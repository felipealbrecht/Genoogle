package bio.pih.search.results;

import java.util.Comparator;

import bio.pih.alignment.GenoogleSmithWaterman;

/**
 * @author albrecht
 * 
 */
public class HSP {

	private static final long serialVersionUID = -7701610542981141900L;

	private final GenoogleSmithWaterman alignment;
	private final int queryOffset;
	private final int targetOffset;
	private final int num;

	public HSP(int num, GenoogleSmithWaterman alignment, int queryOffset, int targetOffset) {
		this.num = num;
		this.alignment = alignment;
		this.queryOffset = queryOffset;
		this.targetOffset = targetOffset;
	}

	public int getNum() {
		return num;
	}
	
	public double getScore() {
		return alignment.getScore();
	}
	
	public int getQueryFrom() {
		return queryOffset + alignment.getQueryStart();
	}
	
	public int getQueryTo() {
		return queryOffset + alignment.getQueryEnd();
	}
	
	public int getHitFrom() {
		return targetOffset + alignment.getTargetStart();
	}
	
	public int getHitTo() {
		return targetOffset + alignment.getTargetEnd();
	}
	
	public int getIdentityLength() {
		return alignment.getIdentitySize();
	}
	
	public int getAlignLength() {
		return alignment.getPath().length();
	}
	
	public String getQuerySeq() {
		return alignment.getQueryAligned();
	}
	
	public String getTargetSeq() {
		return alignment.getTargetAligned();
	}
	
	public String getPathSeq() {
		return alignment.getPath();
	}
	
	public GenoogleSmithWaterman getAlignment() {
		return alignment; 
	}
	
		
//    92               <Hsp_score>83</Hsp_score>
//    94               <Hsp_query-from>555</Hsp_query-from>
//    95               <Hsp_query-to>641</Hsp_query-to>
//    96               <Hsp_hit-from>475</Hsp_hit-from>
//    97               <Hsp_hit-to>561</Hsp_hit-to>
//   100               <Hsp_identity>86</Hsp_identity>
//   102               <Hsp_align-len>87</Hsp_align-len>
//   103               <Hsp_qseq>CAGAATCTTTCTGGAACTCTGCCGCAGGATGAGCTCAAGGAATTGAAGAAGAAGGTCACTGCCAAAATTGATTATGGAAACAGAATC</Hsp_qseq>
//   104               <Hsp_hseq>CAGATTCTTTCTGGAACTCTGCCGCAGGATGAGCTCAAGGAATTGAAGAAGAAGGTCACTGCCAAAATTGATTATGGAAACAGAATC</Hsp_hseq>
//   105               <Hsp_midline>|||| ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||</Hsp_midline>


	public static final Comparator<HSP> COMPARATOR = new Comparator<HSP>() {
		@Override
		public int compare(HSP o1, HSP o2) {
			GenoogleSmithWaterman osw1 = o1.getAlignment();
			GenoogleSmithWaterman osw2 = o2.getAlignment();
			return Double.compare(osw2.getScore(), osw1.getScore());
		}
	};
	
}
