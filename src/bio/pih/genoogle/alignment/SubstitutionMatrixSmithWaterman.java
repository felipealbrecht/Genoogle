/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009,2010,2011,2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.alignment;


public class SubstitutionMatrixSmithWaterman extends GenoogleSequenceAlignment {

	private final SubstitutionTable substitutionTable;
	
	/*
	 * Variables needed for traceback
	 */
	int score = Integer.MIN_VALUE;
	String[] align = new String[2];
	String path = null;
	int identitySize;

	private final int delete;

	private final int insert;


	public SubstitutionMatrixSmithWaterman(SubstitutionTable substitutionTable, int delete, int insert) {		
		this.substitutionTable = substitutionTable;		
		this.delete = delete;
		this.insert = insert;		
	}

	int maxI = 0, maxJ = 0, queryStart = 0, targetStart = 0;

	/**
	 * @param query
	 * @param subject
	 * @return the score of the alignment
	 */
	public int pairwiseAlignment(String query, String subject) {
		int[][] scoreMatrix = new int[query.length() + 1][subject.length() + 1];
		int builderLength = (int) (Math.max(query.length(), subject.length()) * 1.20);

		StringBuilder pathBuilder = new StringBuilder(builderLength);
		StringBuilder[] alignBuilder = new StringBuilder[2];
		alignBuilder[0] = new StringBuilder(builderLength);
		alignBuilder[1] = new StringBuilder(builderLength);


			nonAfinedGapAlignment(query, subject, scoreMatrix, pathBuilder, alignBuilder);

		this.score = scoreMatrix[maxI][maxJ];
		this.path = pathBuilder.reverse().toString();
		this.align[0] = alignBuilder[0].reverse().toString();
		this.align[1] = alignBuilder[1].reverse().toString();

		return this.score;
	}

	private void nonAfinedGapAlignment(String query, String subject, int[][] scoreMatrix,
			StringBuilder pathBuilder, StringBuilder[] alignBuilder) {
		int i;
		int j;
		int queryLength = query.length();
		int subjectLength = subject.length();
		int k = 3;
		
		for (i = 0; i <= queryLength; i++)
			scoreMatrix[i][0] = 0;
		for (j = 0; j <= subjectLength; j++)
			scoreMatrix[0][j] = 0;
		for (i = 1; i <= queryLength; i++) {
			int from = Math.max(1, i - k);
			int to = Math.min(subjectLength, i + k);
			for (j = from; j <= to; j++) {
				scoreMatrix[i][j] = max(
						0, 
						scoreMatrix[i - 1][j] + delete, scoreMatrix[i][j - 1] + insert, 
						scoreMatrix[i - 1][j - 1] + substitutionTable.getValue(query.charAt(i-1), subject.charAt(j-1))
						);
						

				if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
					maxI = i;
					maxJ = j;
				}
			}
		}

		/*
		 * Here starts the traceback for non-affine gap penalities
		 */
		backtrace(query, subject, scoreMatrix, pathBuilder, alignBuilder);
	}

	private void backtrace(String query, String subject, int[][] scoreMatrix,
			StringBuilder pathBuilder, StringBuilder[] alignBuilder) {
		int i;
		int j;
		j = maxJ;
		for (i = maxI; i > 0;) {
			do {
				// only Deletes or Inserts or Replaces possible.
				// That's not what
				// we want to have.
				if (scoreMatrix[i][j] == 0) {
					queryStart = i;
					targetStart = j;
					i = j = 0;

					// Match/Replace
				} else {
					char queryChar = query.charAt(i-1);
					char subjectChar = subject.charAt(j-1);
					if (scoreMatrix[i][j]
							- (scoreMatrix[i - 1][j - 1] + substitutionTable.getValue(queryChar, subjectChar) ) == 0) {
						if (queryChar == subjectChar) {
							pathBuilder.append('|');
							identitySize++;
						} else {
							if (substitutionTable.getValue(queryChar, subjectChar) > 0) {
								pathBuilder.append('+');	
							} else {
								pathBuilder.append(' ');
							}
							
						}

						alignBuilder[0].append(queryChar);
						alignBuilder[1].append(subjectChar);
						i--;
						j--;

						// Insert
					} else if (scoreMatrix[i][j] - (scoreMatrix[i][j - 1] + insert) == 0) {
						alignBuilder[0].append('-');
						alignBuilder[1].append(subjectChar);
						pathBuilder.append(' ');
						j--;

						// Delete
					} else {
						alignBuilder[0].append(queryChar);
						alignBuilder[1].append('-');
						pathBuilder.append(' ');
						i--;
					}
				}
			} while (j > 0);
		}
	}

	@Override
	public String getQueryAligned() {
		return align[0];
	}

	@Override
	public String getTargetAligned() {
		return align[1];
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public int getQueryStart() {
		return queryStart + 1;
	}

	@Override
	public int getQueryEnd() {
		return maxI;
	}

	@Override
	public int getTargetStart() {
		return targetStart + 1;
	}

	@Override
	public int getTargetEnd() {
		return maxJ;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public int getIdentitySize() {
		return identitySize;
	}
}
