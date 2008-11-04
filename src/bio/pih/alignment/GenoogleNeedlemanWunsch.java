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

import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.SymbolList;

/*
 * Created on 23.06.2005
 */

/**
 * Needleman and Wunsch definied the problem of global sequence alignments, from the first till the last symbol of a sequence. This class is able to perform such global sequence comparisons efficiently by dynamic programing. If inserts and deletes are equally expensive and as expensive as the extension of a gap, the alignment method of this class does not use affine gap panelties. Otherwise it does. Those costs need four times as much memory, which has significant effects on the run time, if the computer needs to swap.
 * 
 * Some changes by Felipe Albrecht for faster alignment methods
 * 
 * TODO: Split this class in two: one for Alignment itself and other to store the results.
 * 
 * @author Andreas Dr&auml;ger
 * @author Gero Greiner
 * @since 1.5
 */

public class GenoogleNeedlemanWunsch extends GenoogleSequenceAlignment {
	/**
	 * A matrix with the size length(sequence1) times length(sequence2)
	 */
	protected int[][] costMatrix;

	/**
	 * A matrix with the size length(alphabet) times length(alphabet)
	 */
	protected transient SubstitutionMatrix subMatrix;

	/**
	 * The result of a successfull alignment as a simple String.
	 */
	protected String alignment;

	/**
	 * Expenses for insterts.
	 */
	private int insert;

	/**
	 * Expenses for deletes.
	 */
	private int delete;

	/**
	 * Expenses for the extension of a gap.
	 */
	private int gapExt;

	/**
	 * Expenses for matches.
	 */
	private int match;

	/**
	 * Expenses for replaces.
	 */
	private int replace;

	/**
	 * Constructs a new Object with the given parameters based on the Needleman-Wunsch algorithm The alphabet of sequences to be aligned will be taken from the given substitution matrix.
	 * 
	 * @param match
	 *            This gives the costs for a match operation. It is only used, if there is no entry for a certain match of two symbols in the substitution matrix (default value).
	 * @param replace
	 *            This is like the match parameter just the default, if there is no entry in the substitution matrix object.
	 * @param insert
	 *            The costs of a single insert operation.
	 * @param delete
	 *            The expenses of a single delete operation.
	 * @param gapExtend
	 *            The expenses of an extension of a existing gap (that is a previous insert or delete. If the costs for insert and delete are equal and also equal to gapExtend, no affine gap penalties will be used, which saves a significant amount of memory.
	 * @param subMat
	 *            The substitution matrix object which gives the costs for matches and replaces.
	 */
	public GenoogleNeedlemanWunsch(int match, int replace, int insert, int delete, int gapExtend, SubstitutionMatrix subMat) {
		this.subMatrix = subMat;
		this.insert = insert;
		this.delete = delete;
		this.gapExt = gapExtend;
		this.match = match;
		this.replace = replace;
		this.alignment = "";
	}

	/**
	 * Sets the substitution matrix to be used to the specified one. Afterwards it is only possible to align sequences of the alphabet of this substitution matrix.
	 * 
	 * @param matrix
	 *            an instance of a substitution matrix.
	 */
	public void setSubstitutionMatrix(SubstitutionMatrix matrix) {
		this.subMatrix = matrix;
	}







	/**
	 * Prints a String representation of the CostMatrix for the given Alignment on the screen. This can be used to get a better understanding of the algorithm. There is no other purpose. This method also works for all extensions of this class with all kinds of matrices.
	 * 
	 * @param CostMatrix
	 *            The matrix that contains all expenses for swaping symbols.
	 * @param queryChar
	 *            a character representation of the query sequence (<code>mySequence.seqString().toCharArray()</code>).
	 * @param targetChar
	 *            a character representation of the target sequence.
	 * @return a String representation of the matrix.
	 */
	public static String printCostMatrix(int[][] CostMatrix, char[] queryChar, char[] targetChar) {
		int line, col;
		String output = "\t";

		for (col = 0; col <= targetChar.length; col++)
			if (col == 0)
				output += "[" + col + "]\t";
			else
				output += "[" + targetChar[col - 1] + "]\t";
		for (line = 0; line <= queryChar.length; line++) {
			if (line == 0)
				output += System.getProperty("line.separator") + "[" + line + "]\t";
			else
				output += System.getProperty("line.separator") + "[" + queryChar[line - 1] + "]\t";
			for (col = 0; col <= targetChar.length; col++)
				output += CostMatrix[line][col] + "\t";
		}
		output += System.getProperty("line.separator") + "delta[Edit] = " + CostMatrix[line - 1][col - 1] + System.getProperty("line.separator");
		return output;
	}

	/**
	 * This gives the edit distance acording to the given parameters of this certain object. It returns just the last element of the internal cost matrix (left side down). So if you extend this class, you can just do the following: <code>int myDistanceValue = foo; this.CostMatrix = new int[1][1]; this.CostMatrix[0][0] = myDistanceValue;</code>
	 * 
	 * @return returns the edit_distance computed with the given parameters.
	 */
	public int getEditDistance() {
		return costMatrix[costMatrix.length - 1][costMatrix[costMatrix.length - 1].length - 1];
	}

	/**
	 * This just computes the minimum of three int values.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return Gives the minimum of three ints
	 */
	protected static int min(int x, int y, int z) {
		if ((x < y) && (x < z))
			return x;
		if (y < z)
			return y;
		return z;
	}

	/**
	 * A simpler version of pairwiseAlignment that align two SymbolList and return its score.
	 */
	@Override
	public int fastPairwiseAlignment(SymbolList query, SymbolList subject) throws BioRuntimeException {
		if (query.getAlphabet().equals(subject.getAlphabet()) && query.getAlphabet().equals(subMatrix.getAlphabet())) {

			int i, j;
			this.costMatrix = new int[query.length() + 1][subject.length() + 1]; // Matrix

			// construct the matrix:
			costMatrix[0][0] = 0;

			/*
			 * If we want to have affine gap penalties, we have to initialise additional matrices: If this is not necessary, we won't do that (because it's expensive).
			 */
			if ((gapExt != delete) || (gapExt != insert)) {

				int[][] E = new int[query.length() + 1][subject.length() + 1]; // Inserts
				int[][] F = new int[query.length() + 1][subject.length() + 1]; // Deletes

				E[0][0] = F[0][0] = Integer.MAX_VALUE;
				for (i = 1; i <= query.length(); i++) {
					// CostMatrix[i][0] = CostMatrix[i-1][0] + delete;
					E[i][0] = Integer.MAX_VALUE;
					costMatrix[i][0] = F[i][0] = delete + i * gapExt;
				}
				for (j = 1; j <= subject.length(); j++) {
					// CostMatrix[0][j] = CostMatrix[0][j - 1] + insert;
					F[0][j] = Integer.MAX_VALUE;
					costMatrix[0][j] = E[0][j] = insert + j * gapExt;
				}
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {
						E[i][j] = Math.min(E[i][j - 1], costMatrix[i][j - 1] + insert) + gapExt;
						F[i][j] = Math.min(F[i - 1][j], costMatrix[i - 1][j] + delete) + gapExt;
						costMatrix[i][j] = min(E[i][j], F[i][j], costMatrix[i - 1][j - 1] - matchReplace(query, subject, i, j));
					}

				/*
				 * No affine gap penalties, constant gap penalties, which is much faster and needs less memory.
				 */
			} else {

				for (i = 1; i <= query.length(); i++)
					costMatrix[i][0] = costMatrix[i - 1][0] + delete;
				for (j = 1; j <= subject.length(); j++)
					costMatrix[0][j] = costMatrix[0][j - 1] + insert;
				for (i = 1; i <= query.length(); i++)
					for (j = 1; j <= subject.length(); j++) {
						costMatrix[i][j] = min(costMatrix[i - 1][j] + delete, costMatrix[i][j - 1] + insert, costMatrix[i - 1][j - 1] - matchReplace(query, subject, i, j));
					}

			}

			return getEditDistance();

		}
		throw new BioRuntimeException("Alphabet missmatch occured: sequences with different alphabet cannot be aligned.");
	}

	/*
	 * Variables needed for traceback
	 */
	int score = Integer.MIN_VALUE;
	String[] align = new String[] { "", "" };
	String path = "";
	int identitySize;
	long time;

	/**
	 * @return {@link String} containing the representation of the query aligned.
	 */
	public String getQueryAligned() {
		return align[0];
	}
	/**
	 * @return {@link String} containing the representation of the target aligned.
	 */
	public String getTargetAligned() {
		return align[1];
	}

	/**
	 * @return {@link String} containing the representation of the alignment path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return position in the alignment where the query starts. 
	 */
	public int getQueryStart() {
		return 0;
	}

	/**
	 * @return position in the alignment where the target starts. 
	 */
	public int getTargetStart() {
		return 0;
	}

	/**
	 * @return position in the alignment where the query ends. 
	 */
	public int getQueryEnd() {
		return costMatrix.length - 1;
	}

	/**
	 * @return position in the alignment where the target ends. 
	 */
	public int getTargetEnd() {
		return costMatrix[0].length - 1;
	}

	/**
	 * @return total time of the alignment. 
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @return alignment score. 
	 */
	public int getScore() {
		return score;
	}	
	
	/**
	 * @return the identity size of the alignment. 
	 */
	public int getIdentitySize() {
		return identitySize;
	}

	/**
	 * This method computes the scores for the substution of the i-th symbol of query by the j-th symbol of subject.
	 * 
	 * @param query
	 *            The query symbolList
	 * @param subject
	 *            The target symbolList
	 * @param i
	 *            The position of the symbol under consideration within the query sequence (starting from one)
	 * @param j
	 *            The position of the symbol under consideration within the target sequence
	 * @return The score for the given substitution.
	 */
	private int matchReplace(SymbolList query, SymbolList subject, int i, int j) {
		try {
			return subMatrix.getValueAt(query.symbolAt(i), subject.symbolAt(j));
		} catch (Exception exc) {
			if (query.symbolAt(i).getMatches().contains(subject.symbolAt(j)) || subject.symbolAt(j).getMatches().contains(query.symbolAt(i)))
				return -match;
			return -replace;
		}
	}

}
