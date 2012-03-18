/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package bio.pih.genoogle.alignment;

import bio.pih.genoogle.seq.SymbolList;

/*
 * Created on 05.09.2005
 * 
 */

/**
 * Smith and Waterman developed an efficient dynamic programing algorithm to perform local sequence
 * alignments, which returns the most conserved region of two sequences (longes common substring
 * with modifications). This algorithm is performed by the method <code>pairwiseAlignment</code> of
 * this class. It uses affine gap penalties if and only if the expenses of a delete or insert
 * operation are unequal to the expenses of gap extension. This uses significantly more memory (four
 * times as much) and increases the runtime if swaping is performed.
 * 
 * @author Felipe Albrecht
 * @author Andreas Dr&auml;ger
 * @author Gero Greiner
 * @since 1.5
 */
public class GenoogleSmithWaterman extends GenoogleSequenceAlignment {

	/**
	 * Expenses for insterts.
	 */
	protected int insert;

	/**
	 * Expenses for deletes.
	 */
	protected int delete;

	/**
	 * Expenses for the extension of a gap.
	 */
	protected int gapExt;

	/**
	 * Expenses for matches.
	 */
	protected int match;

	/**
	 * Expenses for replaces.
	 */
	protected int replace;

	/*
	 * Variables needed for traceback
	 */
	int score = Integer.MIN_VALUE;
	String[] align = new String[2];
	String path = null;
	int identitySize;

	private static final long serialVersionUID = 2884980510887845616L;

	/**
	 * Constructs the new SmithWaterman alignment object. Alignments are only performed, if the
	 * alphabet of the given <code>SubstitutionMatrix</code> equals the alpabet of both the query
	 * and the target <code>Sequence</code>. The alignment parameters here are expenses and not
	 * scores as they are in the <code>NeedlemanWunsch</code> object. scores are just given by
	 * multipliing the expenses with <code>(-1)</code>. For example you could use parameters like
	 * "-2, 5, 3, 3, 0". If the expenses for gap extension are equal to the cost of starting a gap
	 * (delete or insert), no affine gap penalties are used, which saves memory.
	 * 
	 * @param match
	 *            expenses for a match
	 * @param replace
	 *            expenses for a replace operation
	 * @param insert
	 *            expenses for a gap opening in the query sequence
	 * @param delete
	 *            expenses for a gap opening in the target sequence
	 * @param gapExtend
	 *            expenses for the extension of a gap which was started earlier.
	 */
	public GenoogleSmithWaterman(int match, int replace, int insert, int delete, int gapExtend) {
		this.insert = insert;
		this.delete = delete;
		this.gapExt = gapExtend;
		this.match = match;
		this.replace = replace;
	}

	int maxI = 0, maxJ = 0, queryStart = 0, targetStart = 0;

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#pairwiseAlignment(bio.pih.genoogle.seq.SymbolList, bio.pih.genoogle.seq.SymbolList)
	 */

	public int pairwiseAlignment(SymbolList query, SymbolList subject) {
		int[][] scoreMatrix = new int[query.getLength() + 1][subject.getLength() + 1];
		int builderLength = (int) (Math.max(query.getLength(), subject.getLength()) * 1.20);

		StringBuilder pathBuilder = new StringBuilder(builderLength);
		StringBuilder[] alignBuilder = new StringBuilder[2];
		alignBuilder[0] = new StringBuilder(builderLength);
		alignBuilder[1] = new StringBuilder(builderLength);

		/*
		 * Use affine gap panalties.
		 */
		if ((gapExt != delete) || (gapExt != insert)) {
			affinedGapAlignment(query, subject, scoreMatrix, pathBuilder, alignBuilder);

			/*
			 * No affine gap penalties to save memory.
			 */
		} else {
			nonAfinedGapAlignment(query, subject, scoreMatrix, pathBuilder, alignBuilder);
		}

		this.score = scoreMatrix[maxI][maxJ];
		this.path = pathBuilder.reverse().toString();
		this.align[0] = alignBuilder[0].reverse().toString();
		this.align[1] = alignBuilder[1].reverse().toString();

		return this.score;
	}

	private void nonAfinedGapAlignment(SymbolList query, SymbolList subject, int[][] scoreMatrix,
			StringBuilder pathBuilder, StringBuilder[] alignBuilder) {
		int i;
		int j;
		int k = 3;
		for (i = 0; i <= query.getLength(); i++)
			scoreMatrix[i][0] = 0;
		for (j = 0; j <= subject.getLength(); j++)
			scoreMatrix[0][j] = 0;
		for (i = 1; i <= query.getLength(); i++) {
			int from = Math.max(1, i - k);
			int to = Math.min(subject.getLength(), i + k);
			for (j = from; j <= to; j++) {
				scoreMatrix[i][j] = max(0, scoreMatrix[i - 1][j] + delete, scoreMatrix[i][j - 1] + insert,
						scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j));

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

	private void affinedGapAlignment(SymbolList query, SymbolList subject, int[][] scoreMatrix,
			StringBuilder pathBuilder, StringBuilder[] alignBuilder) {
		int i;
		int j;
		int[][] E = new int[query.getLength() + 1][subject.getLength() + 1]; // Inserts
		int[][] F = new int[query.getLength() + 1][subject.getLength() + 1]; // Deletes

		scoreMatrix[0][0] = 0;
		E[0][0] = F[0][0] = Integer.MIN_VALUE;
		for (i = 1; i <= query.getLength(); i++) {
			scoreMatrix[i][0] = F[i][0] = 0;
			E[i][0] = Integer.MIN_VALUE;
		}
		for (j = 1; j <= subject.getLength(); j++) {
			scoreMatrix[0][j] = E[0][j] = 0;
			F[0][j] = Integer.MIN_VALUE;
		}
		for (i = 1; i <= query.getLength(); i++)
			for (j = 1; j <= subject.getLength(); j++) {
				E[i][j] = Math.max(E[i][j - 1], scoreMatrix[i][j - 1] + insert) + gapExt;
				F[i][j] = Math.max(F[i - 1][j], scoreMatrix[i - 1][j] + delete) + gapExt;
				scoreMatrix[i][j] = max(0, E[i][j], F[i][j], scoreMatrix[i - 1][j - 1]
						+ matchReplace(query, subject, i, j));

				if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
					maxI = i;
					maxJ = j;
				}
			}

		/*
		 * Here starts the traceback for affine gap penalities
		 */
		boolean[] gap_extend = { false, false };
		j = maxJ;
		for (i = maxI; i > 0;) {
			do {
				// only Deletes or Inserts or Replaces possible.
				// That's not what we want to have.
				if (scoreMatrix[i][j] == 0) {
					queryStart = i;
					targetStart = j;
					i = j = 0;

					// Match/Replace
				} else if ((Math.abs(scoreMatrix[i][j]
						- (scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j))) < 0.0001)
						&& !(gap_extend[0] || gap_extend[1])) {
					if (query.symbolAt(i) == subject.symbolAt(j)) {
						pathBuilder.append('|');
						identitySize++;
					} else {
						pathBuilder.append(' ');
					}

					alignBuilder[0].append(query.symbolAt(i--));
					alignBuilder[1].append(subject.symbolAt(j--));

					// Insert || finish gap if extended gap is
					// opened
				} else if (scoreMatrix[i][j] == E[i][j] || gap_extend[0]) {
					// check if gap has been extended or freshly
					// opened
					gap_extend[0] = Math.abs(E[i][j] - (scoreMatrix[i][j - 1] + insert + gapExt)) > 0.0001;

					alignBuilder[0].append('-');
					alignBuilder[1].append(subject.symbolAt(j--));
					pathBuilder.append(' ');
					// Delete || finish gap if extended gap is
					// opened
				} else {
					// check if gap has been extended or freshly
					// opened
					gap_extend[1] = Math.abs(F[i][j] - (scoreMatrix[i - 1][j] + delete + gapExt)) > 0.0001;

					alignBuilder[0].append(query.symbolAt(i--));
					alignBuilder[1].append('-');
					pathBuilder.append(' ');
				}
			} while (j > 0);
		}
	}

	/**
	 * Backtrace phase for the Smith-Waterman algorithm.
	 */
	private void backtrace(SymbolList query, SymbolList subject, int[][] scoreMatrix, StringBuilder pathBuilder,
			StringBuilder[] alignBuilder) {
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
				} else if (Math.abs(scoreMatrix[i][j]
						- (scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j))) < 0.0001) {
					if (query.symbolAt(i) == subject.symbolAt(j)) {
						pathBuilder.append('|');
						identitySize++;
					} else {
						pathBuilder.append(' ');
					}

					alignBuilder[0].append(query.symbolAt(i--));
					alignBuilder[1].append(subject.symbolAt(j--));

					// Insert
				} else if (Math.abs(scoreMatrix[i][j] - (scoreMatrix[i][j - 1] + insert)) < 0.0001) {
					alignBuilder[0].append('-');
					alignBuilder[1].append(subject.symbolAt(j--));
					pathBuilder.append(' ');

					// Delete
				} else {
					alignBuilder[0].append(query.symbolAt(i--));
					alignBuilder[1].append('-');
					pathBuilder.append(' ');
				}
			} while (j > 0);
		}
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getQueryAligned()
	 */
	@Override
	public String getQueryAligned() {
		return align[0];
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getTargetAligned()
	 */
	@Override
	public String getTargetAligned() {
		return align[1];
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getQueryStart()
	 */
	@Override
	public int getQueryStart() {
		return queryStart + 1;
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getQueryEnd()
	 */
	@Override
	public int getQueryEnd() {
		return maxI;
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getTargetStart()
	 */
	@Override
	public int getTargetStart() {
		return targetStart + 1;
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getTargetEnd()
	 */
	@Override
	public int getTargetEnd() {
		return maxJ;
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getScore()
	 */
	@Override
	public int getScore() {
		return score;
	}

	/**
	 * This method computes the scores for the substution of the i-th symbol of query by the j-th
	 * symbol of subject.
	 * 
	 * @param query
	 *            The query sequence
	 * @param subject
	 *            The target sequence
	 * @param i
	 *            The position of the symbol under consideration within the query sequence (starting
	 *            from one)
	 * @param j
	 *            The position of the symbol under consideration within the target sequence
	 * @return The score for the given substitution.
	 */
	private int matchReplace(SymbolList query, SymbolList subject, int i, int j) {
		// Symbols are singletons, so it is possible to use '=='.
		if (query.symbolAt(i) == subject.symbolAt(j)) {
			return match;
		} else {
			return replace;
		}
	}

	/* (non-Javadoc)
	 * @see bio.pih.genoogle.alignment.X#getIdentitySize()
	 */
	@Override
	public int getIdentitySize() {
		return identitySize;
	}

}
