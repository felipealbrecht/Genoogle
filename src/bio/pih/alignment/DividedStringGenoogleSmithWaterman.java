package bio.pih.alignment;

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

		int querySplit = query.length() / lengthThreshould;
		int subjectSplit = target.length() / lengthThreshould;

		// int pieces = (querySplit + subjectSplit) / 2;
		int pieces = Math.min(querySplit, subjectSplit);

		int queryLength = query.length() / pieces;
		int subjectLenth = target.length() / pieces;

		int queryRest = query.length() % pieces;
		int subjectRest = target.length() % pieces;

		// TODO: arrumar o resto da divisao e por no ultimo alinhamento.

		int queryBegin = 0;
		int subjectBegin = 0;

		queryAlignedBuilder = new StringBuilder();
		targetAlignedBuilder = new StringBuilder();
		pathAlignedBuilder = new StringBuilder();
		score = 0;

		for (int i = 0; i < pieces; i++) {
			if (i == pieces - 1) {
				queryLength += queryRest;
				subjectLenth += subjectRest;
			}
			String queryPiece = query.substring(queryBegin, queryBegin + queryLength);
			String targetPiece = target.substring(subjectBegin, subjectBegin + subjectLenth);
			queryBegin += queryLength;
			subjectBegin += subjectLenth;

			StringGenoogleSmithWaterman aligner = new StringGenoogleSmithWaterman(match, replace, insert, delete, gapExtend);
			aligner.pairwiseAlignment(queryPiece, targetPiece);
			score += aligner.getScore();

			int initGap = Math.max(aligner.getQueryStart(), aligner.getTargetStart());
			int maxLength = Math.max(queryPiece.length(), targetPiece.length());

			for (int p = 0; p < initGap; p++) {
				if (p < aligner.getQueryStart()) {
					queryAlignedBuilder.append(queryPiece.charAt(p));
				} else {
					queryAlignedBuilder.append('-');
				}
			}
			queryAlignedBuilder.append(aligner.getQueryAligned());
			for (int p = aligner.getQueryEnd(); p < maxLength; p++) {
				if (p < queryPiece.length()) {
					queryAlignedBuilder.append(queryPiece.charAt(p));
				} else {
					queryAlignedBuilder.append('-');
				}
			}

			for (int p = 0; p < initGap; p++) {
				if (p < aligner.getTargetStart()) {
					targetAlignedBuilder.append(targetPiece.charAt(p));
				} else {
					targetAlignedBuilder.append('-');
				}
			}
			targetAlignedBuilder.append(aligner.getTargetAligned());
			for (int p = aligner.getTargetEnd(); p < maxLength; p++) {
				if (p < targetPiece.length()) {
					targetAlignedBuilder.append(targetPiece.charAt(p));					
				} else {
					targetAlignedBuilder.append('-');
				}
			}

			int min = Math.min(aligner.getQueryStart(), aligner.getTargetEnd());
			for (int p = 1; p < min; p++) {
				pathAlignedBuilder.append(' ');
			}
			pathAlignedBuilder.append(aligner.getPath());
			for (int p = aligner.getPath().length(); p < maxLength; p++) {
				pathAlignedBuilder.append(' ');
			}
		}

		this.queryStart = 0;
		this.queryEnd = query.length();
		this.targetStart = 0;
		this.targetEnd = target.length();

		return this.score;

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
