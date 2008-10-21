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
package bio.pih.alignment;

import org.biojava.bio.BioException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.SymbolList;

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
 * Some changes by Felipe Albrecht for faster alignment methods
 * 
 * @author Andreas Dr&auml;ger
 * @author Gero Greiner
 * @since 1.5
 */
public class GenoogleSmithWaterman extends GenoogleNeedlemanWunsch {

	private static final long serialVersionUID = 2884980510887845616L;

	private int match;
	private int replace;
	private int insert;
	private int delete;
	private int gapExt;

	/**
	 * Constructs the new SmithWaterman alignment object. Alignments are only performed, if the
	 * alphabet of the given <code>SubstitutionMatrix</code> equals the alpabet of both the query and
	 * the target <code>Sequence</code>. The alignment parameters here are expenses and not scores as
	 * they are in the <code>NeedlemanWunsch</code> object. scores are just given by multipliing the
	 * expenses with <code>(-1)</code>. For example you could use parameters like "-2, 5, 3, 3, 0". If
	 * the expenses for gap extension are equal to the cost of starting a gap (delete or insert), no
	 * affine gap penalties are used, which saves memory.
	 * 
	 * @param match
	 *          expenses for a match
	 * @param replace
	 *          expenses for a replace operation
	 * @param insert
	 *          expenses for a gap opening in the query sequence
	 * @param delete
	 *          expenses for a gap opening in the target sequence
	 * @param gapExtend
	 *          expenses for the extension of a gap which was started earlier.
	 * @param matrix
	 *          the <code>SubstitutionMatrix</code> object to use.
	 */
	public GenoogleSmithWaterman(int match, int replace, int insert, int delete,
			int gapExtend, SubstitutionMatrix matrix) {
		super(insert, delete, gapExtend, match, replace, matrix);
		this.alignment = "";
	}

	/**
	 * Overrides the method inherited from the NeedlemanWunsch and sets the penalty for an insert
	 * operation to the specified value. Reason: internaly scores are used instead of penalties so
	 * that the value is muliplied with -1.
	 * 
	 * @param ins
	 *          costs for a single insert operation
	 */
	@Override
	public void setInsert(int ins) {
		this.insert = -ins;
	}

	/**
	 * Overrides the method inherited from the NeedlemanWunsch and sets the penalty for a delete
	 * operation to the specified value. Reason: internaly scores are used instead of penalties so
	 * that the value is muliplied with -1.
	 * 
	 * @param del
	 *          costs for a single deletion operation
	 */
	@Override
	public void setDelete(int del) {
		this.delete = -del;
	}

	/**
	 * Overrides the method inherited from the NeedlemanWunsch and sets the penalty for an extension
	 * of any gap (insert or delete) to the specified value. Reason: internaly scores are used instead
	 * of penalties so that the value is muliplied with -1.
	 * 
	 * @param ge
	 *          costs for any gap extension
	 */
	@Override
	public void setGapExt(int ge) {
		this.gapExt = -ge;
	}

	/**
	 * Overrides the method inherited from the NeedlemanWunsch and sets the penalty for a match
	 * operation to the specified value. Reason: internaly scores are used instead of penalties so
	 * that the value is muliplied with -1.
	 * 
	 * @param ma
	 *          costs for a single match operation
	 */
	@Override
	public void setMatch(int ma) {
		this.match = -ma;
	}

	/**
	 * Overrides the method inherited from the NeedlemanWunsch and sets the penalty for a replace
	 * operation to the specified value. Reason: internaly scores are used instead of penalties so
	 * that the value is muliplied with -1.
	 * 
	 * @param rep
	 *          costs for a single replace operation
	 */
	@Override
	public void setReplace(int rep) {
		this.replace = -rep;
	}

	/**
	 * A simpler version of pairwiseAlignment that align SymbolList and return its score.
	 */
	@Override
	public int fastPairwiseAlignment(SymbolList query, SymbolList subject)
			throws BioRuntimeException {
		if (query.getAlphabet().equals(subject.getAlphabet())
				&& query.getAlphabet().equals(subMatrix.getAlphabet())) {

			int i, j, maxI = 0, maxJ = 0;
			int[][] scoreMatrix = new int[query.length() + 1][subject.length() + 1];

			/*
			 * Use affine gap panalties.
			 */
			if ((gapExt != delete) || (gapExt != insert)) {
				System.out.println("**slow** alignment");

				int[][] E = new int[query.length() + 1][subject.length() + 1]; // Inserts
				int[][] F = new int[query.length() + 1][subject.length() + 1]; // Deletes

				scoreMatrix[0][0] = 0;
				E[0][0] = F[0][0] = Integer.MIN_VALUE;
				for (i = 1; i <= query.length(); i++) {
					scoreMatrix[i][0] = F[i][0] = 0;
					E[i][0] = Integer.MIN_VALUE;
				}
				for (j = 1; j <= subject.length(); j++) {
					scoreMatrix[0][j] = E[0][j] = 0;
					F[0][j] = Integer.MIN_VALUE;
				}
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {
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
				 * No affine gap penalties to save memory.
				 */
			} else {
				System.out.println("**fast** alignment");
				for (i = 0; i <= query.length(); i++)
					scoreMatrix[i][0] = 0;
				for (j = 0; j <= subject.length(); j++)
					scoreMatrix[0][j] = 0;
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {

						scoreMatrix[i][j] = max(0, scoreMatrix[i - 1][j] + delete, scoreMatrix[i][j - 1]
								+ insert, scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j));

						if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
							maxI = i;
							maxJ = j;
						}
					}
			}

			return scoreMatrix[maxI][maxJ];

		}
		throw new BioRuntimeException(
				"The alphabets of the sequences and the substitution matrix have to be equal.");

	}

	int maxI = 0, maxJ = 0, queryStart = 0, targetStart = 0;

	/**
	 * @param query
	 * @param subject
	 * @return the score of the alignment
	 * @throws BioRuntimeException
	 */
	public int pairwiseAlignment(SymbolList query, SymbolList subject) throws BioRuntimeException {
		if (query.getAlphabet().equals(subject.getAlphabet())
				&& query.getAlphabet().equals(subMatrix.getAlphabet())) {

			long beginTime = System.currentTimeMillis();
			int i, j;
			int[][] scoreMatrix = new int[query.length() + 1][subject.length() + 1];

			SymbolTokenization st;
			try {
				st = query.getAlphabet().getTokenization("default");
			} catch (BioException exc) {
				throw new BioRuntimeException(exc);
			}

			/*
			 * Use affine gap panalties.
			 */
			if ((gapExt != delete) || (gapExt != insert)) {
				int[][] E = new int[query.length() + 1][subject.length() + 1]; // Inserts
				int[][] F = new int[query.length() + 1][subject.length() + 1]; // Deletes

				scoreMatrix[0][0] = 0;
				E[0][0] = F[0][0] = Integer.MIN_VALUE;
				for (i = 1; i <= query.length(); i++) {
					scoreMatrix[i][0] = F[i][0] = 0;
					E[i][0] = Integer.MIN_VALUE;
				}
				for (j = 1; j <= subject.length(); j++) {
					scoreMatrix[0][j] = E[0][j] = 0;
					F[0][j] = Integer.MIN_VALUE;
				}
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {
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
				try {
					boolean[] gap_extend = { false, false };
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
							} else if ((Math.abs(scoreMatrix[i][j]
									- (scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j))) < 0.0001)
									&& !(gap_extend[0] || gap_extend[1])) {
								if (query.symbolAt(i) == subject.symbolAt(j)) {
									path = '|' + path;
									identitySize++;
								} else
									path = ' ' + path;

								align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
								align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];

								// Insert || finish gap if extended gap is
								// opened
							} else if (scoreMatrix[i][j] == E[i][j] || gap_extend[0]) {
								// check if gap has been extended or freshly
								// opened
								gap_extend[0] = Math.abs(E[i][j] - (scoreMatrix[i][j - 1] + insert + gapExt)) > 0.0001;

								align[0] = '-' + align[0];
								align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
								path = ' ' + path;

								// Delete || finish gap if extended gap is
								// opened
							} else {
								// check if gap has been extended or freshly
								// opened
								gap_extend[1] = Math.abs(F[i][j] - (scoreMatrix[i - 1][j] + delete + gapExt)) > 0.0001;

								align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
								align[1] = '-' + align[1];
								path = ' ' + path;
							}
						} while (j > 0);
					}
				} catch (BioException exc) {
					throw new BioRuntimeException(exc);
				}

				/*
				 * No affine gap penalties to save memory.
				 */
			} else {
				for (i = 0; i <= query.length(); i++)
					scoreMatrix[i][0] = 0;
				for (j = 0; j <= subject.length(); j++)
					scoreMatrix[0][j] = 0;
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {

						scoreMatrix[i][j] = max(0, scoreMatrix[i - 1][j] + delete, scoreMatrix[i][j - 1]
								+ insert, scoreMatrix[i - 1][j - 1] + matchReplace(query, subject, i, j));

						if (scoreMatrix[i][j] > scoreMatrix[maxI][maxJ]) {
							maxI = i;
							maxJ = j;
						}
					}

				/*
				 * Here starts the traceback for non-affine gap penalities
				 */
				try {
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
									path = '|' + path;
									identitySize++;
								} else
									path = ' ' + path;

								align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
								align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];

								// Insert
							} else if (Math.abs(scoreMatrix[i][j] - (scoreMatrix[i][j - 1] + insert)) < 0.0001) {
								align[0] = '-' + align[0];
								align[1] = st.tokenizeSymbol(subject.symbolAt(j--)) + align[1];
								path = ' ' + path;

								// Delete
							} else {
								align[0] = st.tokenizeSymbol(query.symbolAt(i--)) + align[0];
								align[1] = '-' + align[1];
								path = ' ' + path;
							}
						} while (j > 0);
					}
				} catch (BioException exc) {
					throw new BioRuntimeException(exc);
				}
			}

			this.costMatrix = new int[1][1];
			costMatrix[0][0] = -scoreMatrix[maxI][maxJ];
			this.time = System.currentTimeMillis() - beginTime;
			this.score = scoreMatrix[maxI][maxJ];
			return this.score;

		}
		throw new BioRuntimeException(
				"The alphabets of the sequences and the substitution matrix have to be equal.");
	}

	@Override
	public int getQueryStart() {
		return queryStart;
	}

	@Override
	public int getQueryEnd() {
		return maxI;
	}

	@Override
	public int getTargetStart() {
		return targetStart;
	}

	@Override
	public int getTargetEnd() {
		return maxJ;
	}

	/**
	 * This just computes the maximum of four integers.
	 * 
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 * @return the maximum of four <code>int</code>s.
	 */
	private int max(int w, int x, int y, int z) {
		if ((w > x) && (w > y) && (w > z))
			return w;
		if ((x > y) && (x > z))
			return x;
		if ((y > z))
			return y;
		return z;
	}

	/**
	 * This method computes the scores for the substution of the i-th symbol of query by the j-th
	 * symbol of subject.
	 * 
	 * @param query
	 *          The query sequence
	 * @param subject
	 *          The target sequence
	 * @param i
	 *          The position of the symbol under consideration within the query sequence (starting
	 *          from one)
	 * @param j
	 *          The position of the symbol under consideration within the target sequence
	 * @return The score for the given substitution.
	 */
	private int matchReplace(SymbolList query, SymbolList subject, int i, int j) {
		try {
			return subMatrix.getValueAt(query.symbolAt(i), subject.symbolAt(j));
		} catch (Exception exc) {
			if (query.symbolAt(i).getMatches().contains(subject.symbolAt(j))
					|| subject.symbolAt(j).getMatches().contains(query.symbolAt(i)))
				return match;
			return replace;
		}
	}

}
