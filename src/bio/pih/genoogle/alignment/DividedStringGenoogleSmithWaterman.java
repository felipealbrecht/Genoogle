package bio.pih.genoogle.alignment;

// TODO: Ver o pattern que isto corresponde.
// TODO: Ver/refazer o alinhamento para as questoes do interior serem todos alinhados.

public class DividedStringGenoogleSmithWaterman {

	private final int match;
	private final int replace;
	private final int insert;
	private final int delete;
	private final int gapExtend;
	private final int lengthThreshould;

	StringBuilder queryAlignedBuilder = new StringBuilder();
	StringBuilder targetAlignedBuilder = new StringBuilder();
	StringBuilder pathAlignedBuilder = new StringBuilder();

	String queryAligned = null;
	String targetAligned = null;
	String pathAligned = null;

	int score = 0;
	private int queryStart;
	private int queryEnd;
	private int targetStart;
	private int targetEnd;
	private int identitySize;

	public DividedStringGenoogleSmithWaterman(int match, int replace, int insert, int delete, int gapExtend,
			int lengthThreshould) {
		this.match = match;
		this.replace = replace;
		this.insert = insert;
		this.delete = delete;
		this.gapExtend = gapExtend;
		this.lengthThreshould = lengthThreshould;
	}

	public int pairwiseAlignment(String query, String target) {
		if (query.length() <= lengthThreshould || target.length() <= lengthThreshould) {
			StringGenoogleSmithWaterman aligner = new StringGenoogleSmithWaterman(match, replace, insert, delete, gapExtend);
			aligner.pairwiseAlignment(query, target);
			this.queryAligned = aligner.getQueryAligned();
			this.targetAligned = aligner.getTargetAligned();
			this.pathAligned = aligner.getPath();
			this.score = aligner.getScore();
			this.identitySize = aligner.getIdentitySize();
			this.queryStart = aligner.getQueryStart();
			this.queryEnd = aligner.getQueryEnd();
			this.targetStart = aligner.getTargetStart();
			this.targetEnd = aligner.getTargetEnd();

			return this.score;
		}

		int m = query.length() / lengthThreshould;
		int n = target.length() / lengthThreshould;
		
		int mRest = query.length() % lengthThreshould;
		if (mRest != 0)  {
			m++;
		}
		
		int nRest = query.length() % lengthThreshould;
		if (nRest != 0) {
			n++;
		}
		
		int c = Math.min(m, n);
		int queryLength = query.length() / c;
		int targetLength = target.length() / c;

		int queryPos = 0;
		int targetPos = 0;
		int queryDiff = 0;
		int targetDiff = 0;

		queryAlignedBuilder = new StringBuilder();
		targetAlignedBuilder = new StringBuilder();
		pathAlignedBuilder = new StringBuilder();
		score = 0;

		for (int s = 0; s < c; s++) {
			int endQueryPiece; 
			int endTargetPiece;
			
			if (s == c - 1) {
				endQueryPiece = query.length();
				endTargetPiece = target.length();
			} else {
				endQueryPiece = queryPos + queryLength + queryDiff;
				endTargetPiece  = targetPos + targetLength + targetDiff;
			}
			
			String queryPiece = query.substring(queryPos, endQueryPiece);
			String targetPiece = target.substring(targetPos, endTargetPiece);

			StringGenoogleSmithWaterman aligner = new StringGenoogleSmithWaterman(match, replace, insert, delete, gapExtend);
			aligner.pairwiseAlignment(queryPiece, targetPiece);
			score += aligner.getScore();
			identitySize += aligner.getIdentitySize();

			if (s == 0) {
				this.queryStart = aligner.getQueryStart();
				this.targetStart = aligner.getTargetStart();
			} else {
				setBeginSubAlignment(queryPiece, targetPiece, aligner);
			}

			queryAlignedBuilder.append(aligner.getQueryAligned());
			targetAlignedBuilder.append(aligner.getTargetAligned());
			pathAlignedBuilder.append(aligner.getPath());

			queryPos = queryPos + aligner.getQueryEnd();
			targetPos = targetPos + aligner.getTargetEnd();

			queryDiff = queryLength - aligner.getQueryEnd();
			targetDiff = targetLength - aligner.getTargetEnd();

		}

		this.queryEnd = query.length();
		this.targetEnd = target.length();

		return this.score;
	}

	private void setBeginSubAlignment(String queryPiece, String targetPiece, StringGenoogleSmithWaterman aligner) {
		int i;
		for (i = 1; i < aligner.getQueryStart() && i < aligner.getTargetStart(); i++) {
			char queryChar = queryPiece.charAt(i);
			char targetChar = targetPiece.charAt(i);

			queryAlignedBuilder.append(queryChar);
			targetAlignedBuilder.append(targetChar);
			if (queryChar == targetChar) {
				pathAlignedBuilder.append('|');
				score += match;
				identitySize++;
			} else {
				pathAlignedBuilder.append(' ');
				score += replace;
			}

		}
		while (i < aligner.getQueryStart() || i < aligner.getTargetStart()) {
			if (i < aligner.getQueryStart()) {
				queryAlignedBuilder.append(queryPiece.charAt(i-1));
				targetAlignedBuilder.append('-');
				pathAlignedBuilder.append(' ');
				score += insert;
			} else {
				queryAlignedBuilder.append('-');
				targetAlignedBuilder.append(targetPiece.charAt(i-1));
				pathAlignedBuilder.append(' ');
				score += insert;
			}
			i++;
		}
	}

	public String getQueryAligned() {
		if (queryAligned == null) {
			queryAligned = queryAlignedBuilder.toString();
		}
		return queryAligned;
	}

	public String getTargetAligned() {
		if (targetAligned == null) {
			targetAligned = targetAlignedBuilder.toString();
		}
		return targetAligned;
	}

	public String getPath() {
		if (pathAligned == null) {
			pathAligned = pathAlignedBuilder.toString();
		}
		return pathAligned;
	}

	public int getScore() {
		return score;
	}

	public int getQueryStart() {
		return queryStart;
	}

	public int getQueryEnd() {
		return queryEnd;
	}

	public int getTargetStart() {
		return targetStart;
	}

	public int getTargetEnd() {
		return targetEnd;
	}

	public int getIdentitySize() {
		return identitySize;
	}
}