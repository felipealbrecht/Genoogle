package bio.pih.genoogle.statistics;

import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.genoogle.encoder.DNASequenceEncoder;

import com.google.common.collect.Maps;

/**
 * Class to calculate statistics values for similar sequences searching process.
 * 
 * @author albrecht (felipe.albrecht@gmail.com)
 **/
/*
 * 
 * This code is a Java implementation from the karlin.c file from the FSA-Blast, which copied from
 * blastkar.c and modified to make it work standalone without using implementations of mathematical
 * functions in ncbimath.c and other definitions in the NCBI BLAST toolkit.
 * 
 * See: Karlin, S. & Altschul, S.F. "Methods for Assessing the Statistical Significance of Molecular
 * Sequence Features by Using General Scoring Schemes," Proc. Natl. Acad. Sci. USA 87 (1990),
 * 2264-2268.
 * 
 * Computes the parameters lambda and K for use in calculating the statistical significance of
 * high-scoring segments or subalignments.
 * 
 * The scoring scheme must be integer valued. A positive score must be possible, but the expected
 * (mean) score must be negative.
 * 
 * A program that calls this routine must provide the value of the lowest possible score, the value
 * of the greatest possible score, and a pointer to an array of probabilities for the occurence of
 * all scores between these two extreme scores. For example, if score -2 occurs with probability
 * 0.7, score 0 occurs with probability 0.1, and score 3 occurs with probability 0.2, then the
 * subroutine must be called with low = -2, high = 3, and pr pointing to the array of values { 0.7,
 * 0.0, 0.1, 0.0, 0.0, 0.2 }. The calling program must also provide pointers to lambda and K; the
 * subroutine will then calculate the values of these two parameters. In this example, lambda=0.330
 * and K=0.154.
 * 
 * The parameters lambda and K can be used as follows. Suppose we are given a length N random
 * sequence of independent letters. Associated with each letter is a score, and the probabilities of
 * the letters determine the probability for each score. Let S be the aggregate score of the highest
 * scoring contiguous segment of this sequence. Then if N is sufficiently large (greater than 100),
 * the following bound on the probability that S is greater than or equal to x applies:
 * 
 * P( S >= x ) <= 1 - exp [ - KN exp ( - lambda * x ) ].
 * 
 * In other words, the p-value for this segment can be written as 1-exp[-KN*exp(-lambda*S)].
 * 
 * This formula can be applied to pairwise sequence comparison by assigning scores to pairs of
 * letters (e.g. amino acids), and by replacing N in the formula with N*M, where N and M are the
 * lengths of the two sequences being compared.
 * 
 * In addition, letting y = KN*exp(-lambda*S), the p-value for finding m distinct segments all with
 * score >= S is given by:
 * 
 * 2 m-1 -y 1 - [ 1 + y + y /2! + ... + y /(m-1)! ] e
 * 
 * Notice that for m=1 this formula reduces to 1-exp(-y), which is the same as the previous formula.
 */
public class Statistics {

	private static final double BLAST_KARLIN_LAMBDA0_DEFAULT = 0.5;
	private static final double BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT = (1.e-5);
	private static final int BLAST_KARLIN_LAMBDA_ITER_DEFAULT = 17;

	private static final double LOG_2 = Math.log(2.0);

	private Map<Integer, Double> scoreProbabilities(int mismatch, int match, SymbolList query)
			throws IndexOutOfBoundsException, BioException {

		int[][] baseValue = new int[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == j) {
					baseValue[i][j] = match;
				} else {
					baseValue[i][j] = mismatch;
				}
			}
		}

		int min = Math.min(mismatch, match);
		int max = Math.max(mismatch, match);
		int delta;
		if (min < 0) {
			delta = Math.abs(min);
		} else {
			delta = -min;
		}

		int scoreProbabilitiesSize = (min + delta) + (max + delta) + 1;

		double[] scoreProbabilities = new double[scoreProbabilitiesSize];
		for (int i = mismatch + delta; i <= match + delta; i++) {
			scoreProbabilities[i] = 0.0;
		}

		int numRegularLettersInQuery = 0;
		int length = query.length();
		for (int i = 1; i <= length; i++) {
			if (checkSymbol(query.symbolAt(i))) {
				numRegularLettersInQuery++;
			}
		}

		for (int i = 1; i <= length; i++) {
			if (checkSymbol(query.symbolAt(i))) {
				double probability = 250.00 / numRegularLettersInQuery;
				int querySymbolValue = DNASequenceEncoder.getBitsFromSymbol(query.symbolAt(i));

				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.a());
					int score = baseValue[querySymbolValue][symbolValue];
					scoreProbabilities[score + delta] += probability;
				}
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.c());
					int score = baseValue[querySymbolValue][symbolValue];
					scoreProbabilities[score + delta] += probability;
				}
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.g());
					int score = baseValue[querySymbolValue][symbolValue];
					scoreProbabilities[score + delta] += probability;
				}
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.t());
					int score = baseValue[querySymbolValue][symbolValue];
					scoreProbabilities[score + delta] += probability;
				}
			}
		}

		final double sum = 1000.00;

		for (int i = mismatch + delta; i <= match + delta; i++) {
			double probability = scoreProbabilities[i];
			scoreProbabilities[i] = probability / sum;
		}

		Map<Integer, Double> scoreProbabilitiesMap = Maps.newHashMap();
		for (int i = mismatch; i <= match; i++) {
			scoreProbabilitiesMap.put(i, scoreProbabilities[i + delta]);
		}

		return scoreProbabilitiesMap;
	}

	private boolean checkSymbol(Symbol s) {
		if (s == DNATools.a() || s == DNATools.c() || s == DNATools.g() || s == DNATools.t()) {
			return true;
		}
		return false;
	}

	private Double calculateLambda(Map<Integer, Double> prob, int mismatch, int match) {
		if (!checkScoreRange(mismatch, match)) {
			return null;
		}

		double lambda0 = BLAST_KARLIN_LAMBDA0_DEFAULT;

		for (int j = 0; j < 20; j++) {
			double sum = -1.0;
			double slope = 0.0;
			if (lambda0 < 0.01) {
				break;
			}
			double x0 = Math.exp(lambda0);
			double x1 = Math.pow(x0, mismatch - 1);
			if (Double.compare(x1, 0.0) == 0) {
				break;
			}
			for (int i = mismatch; i <= match; i++) {
				x1 *= x0;
				double temp = prob.get(i) * x1;
				sum += temp;
				slope += temp * i;
			}
			double amt = sum / slope;
			lambda0 -= amt;
			if (Math.abs(amt / lambda0) < BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT) {
				if (lambda0 > BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT) {
					return lambda0;
				}
				break;
			}
		}
		return blastKarlinLambdaBis(prob, mismatch, match);
	}

	private Double blastKarlinLambdaBis(Map<Integer, Double> prob, int min, int max) {
		if (!checkScoreRange(min, max)) {
			return null;
		}

		double up = BLAST_KARLIN_LAMBDA0_DEFAULT;
		double lambda = 0.0;

		for (;;) {
			up *= 2;
			double x0 = Math.exp(up);
			double x1 = Math.pow(x0, min - 1);
			double sum = 0.0;
			if (x1 > 0.0) {
				for (int i = min; i <= max; i++) {
					x1 *= x0;
					sum += prob.get(i) * x1;
				}
			} else {
				for (int i = min; i <= max; i++) {
					sum += prob.get(i) * Math.exp(up * i);
				}
			}
			if (sum >= 1.0) {
				break;
			}
			lambda = up;
		}

		for (int j = 0; j < BLAST_KARLIN_LAMBDA_ITER_DEFAULT; j++) {
			double newval = (lambda + up) / 2.0;
			double x0 = Math.exp(newval);
			double x1 = Math.pow(x0, min - 1);
			double sum = 0.0;
			if (x1 > 0.0) {
				for (int i = min; i <= max; i++) {
					x1 *= x0;
					sum += prob.get(i) * x1;
				}
			} else {
				for (int i = min; i <= max; i++) {
					sum += prob.get(i) * Math.exp(newval * i);
				}
			}
			if (sum > 1.0) {
				up = newval;
			} else {
				lambda = newval;
			}
		}
		return (lambda + up) / 2.;
	}

	private double blastH(Map<Integer, Double> prob, double lambda, int mismatch, int match) {
		if (lambda < 0.0) {
			return -1.0;
		}

		if (!checkScoreRange(mismatch, match)) {
			return -1.0;
		}

		double etolam = Math.exp(lambda);
		double etolami = Math.pow(etolam, mismatch - 1);
		double av = 0.0;
		if (etolami > 0.0) {
			for (int score = mismatch; score <= match; score++) {
				etolami *= etolam;
				av += prob.get(score) * score * etolami;
			}
		} else {
			for (int score = mismatch; score <= match; score++) {
				av += prob.get(score) * score * Math.exp(lambda * score);
			}
		}

		return lambda * av;
	}

	private static int BLAST_SCORE_1MIN = -10000;
	private static int BLAST_SCORE_1MAX = 1000;
	private static int BLAST_SCORE_RANGE_MAX = BLAST_SCORE_1MAX - BLAST_SCORE_1MIN;

	private static double BLAST_KARLIN_K_SUMLIMIT_DEFAULT = 0.01;
	private static int BLAST_KARLIN_K_ITER_MAX = 100;
	private static int DIMOFP0_MAX = BLAST_KARLIN_K_ITER_MAX * BLAST_SCORE_RANGE_MAX + 1;

	private int DIMOFP0(int iter, int range) {
		return iter * range + 1;
	}

	private double blastK(Map<Integer, Double> prob, double lambda, double h, int mismatch, int match) {

		if (lambda <= 0.0 || h <= 0.0) {
			return -1.0;
		}

		double av = h / lambda;
		double etolam = Math.exp(lambda);
		if (mismatch == -1 || match == 1) {
			double K = av;
			return K * (1.0 - 1.0 / etolam);
		}

		int low = mismatch;
		int high = match;
		int range = high - low;

		double sumlimit = BLAST_KARLIN_K_SUMLIMIT_DEFAULT;
		int iter = BLAST_KARLIN_K_ITER_MAX;

		if (DIMOFP0(iter, range) > DIMOFP0_MAX) {
			return -1;
		}

		double[] P0 = new double[DIMOFP0(iter, range)];

		double Sum = 0.0;
		int lo = 0;
		int hi = 0;

		double sum = 1.0;
		double oldsum = 1.0;
		double oldsum2 = 1.0;
		P0[0] = 1.0;
		int p = low;

		int first;
		int last;
		long j;
		for (j = 0; j < iter & sum > sumlimit; Sum += sum /= ++j) {
			first = last = range;
			lo += low;
			hi += high;

			int ptrP;
			for (ptrP = hi - lo; ptrP >= 0; P0[ptrP--] = sum) {
				int ptr1 = ptrP - first;
				int ptr1e = ptrP - last;
				int ptr2 = p + first;

				for (sum = 0.0; ptr1 >= ptr1e;) {
					sum += (P0[ptr1--] * prob.get(ptr2++));
				}

				if (first != 0) {
					--first;
				}

				if (ptrP <= range) {
					--last;
				}
			}

			double etolami = Math.pow(etolam, lo - 1);
			int i;
			for (sum = 0.0, i = lo; i != 0; ++i) {
				etolami *= etolam;
				sum += P0[++ptrP] * etolami;
			}

			for (; i <= hi; ++i) {
				sum += P0[++ptrP];
			}

			oldsum2 = oldsum;
			oldsum = sum;
		}

		/* Terms of geometric progression added for correction */
		double ratio = oldsum / oldsum2;

		if (ratio >= (1.0 - sumlimit * 0001)) {
			return -1;
		}

		sumlimit *= 0.01;
		while (sum > sumlimit) {
			oldsum *= ratio;
			Sum += sum = oldsum / ++j;
		}

		int i;
		// Look for the greatest common divisor ("delta" in Appendix of PNAS 87 of Karlin&Altschul
		// (1990)
		for (i = 0, j = -low; i <= range && j > 1; ++i) {
			if (prob.get(p + i) != 0) {
				j = gcd(j, i);
			}
		}

		if (j * etolam > 0.05) {
			double etolami = Math.pow(etolam, -j);
			return j * Math.exp(-2.0 * Sum) / (av * (1.0 - etolami));
		} else {
			return -j * Math.exp(-2.0 * Sum) / (av * Math.exp(-j * lambda) - 1);
		}
	}

	private static final int MAX_SCORE = 1000;
	private static final int MIN_SCORE = -10000;

	private boolean checkScoreRange(int min, int max) {
		if (min < MIN_SCORE || max > MAX_SCORE) {
			return false;
		}
		if (max - min > MAX_SCORE - MIN_SCORE) {
			return false;
		}

		return true;
	}

	private double lengthAdjust(double K, double ungappedLogK, double ungappedH, int querySize, long databaseSize,
			long numberOfSequences) {
		double lenghtAdjust = 0;
		double minimumQueryLength = 1 / K;

		for (int count = 0; count < 5; count++) {
			lenghtAdjust = (ungappedLogK + Math.log((querySize - lenghtAdjust)
					* (databaseSize - numberOfSequences * lenghtAdjust)))
					/ ungappedH;

			if (lenghtAdjust > querySize - minimumQueryLength) {
				lenghtAdjust = querySize - minimumQueryLength;
			}
		}
		return lenghtAdjust;
	}

	public double nominalToNormalizedScore(double nominalScore) {
		return ((nominalScore * this.lambda) - this.logK) / LOG_2;
	}

	public double calculateEvalue(double normalizedScore) {
		return this.searchSpaceSize / Math.pow(2, normalizedScore);
	}

	public double gappedEvalueToNominal(double evalue) {
		double normalizedScore = Math.log(this.searchSpaceSize / evalue) / LOG_2;
		return Math.ceil((LOG_2 * normalizedScore + this.logK) / lambda);
	}

	/**
	 * Look for the greatest common divisor
	 */
	public long gcd(long a, long b) {
		long c;
		b = Math.abs(b);
		if (b > a) {
			c = a;
			a = b;
			b = c;
		}

		while (b != 0) {
			c = a % b;
			a = b;
			b = c;
		}
		return a;
	}

	private final Map<Integer, Double> probabilities;
	private final double lambda;
	private final double H;
	private final double K;
	private final double logK;
	private final double effectiveQuerySize;
	private final double effectiveDatabaseSize;
	private final double searchSpaceSize;
	private final double lengthAdjust;

	public Statistics(int match, int mismatch, SymbolList query, long databaseSize, long numberOfSequences)
			throws IndexOutOfBoundsException, BioException {
		this.probabilities = scoreProbabilities(mismatch, match, query);
		this.lambda = calculateLambda(probabilities, mismatch, match);
		this.H = blastH(probabilities, lambda, mismatch, match);
		this.K = blastK(probabilities, lambda, H, mismatch, match);

		this.logK = Math.log(K);

		this.lengthAdjust = lengthAdjust(K, logK, H, query.length(), databaseSize, numberOfSequences);

		this.effectiveQuerySize = query.length() - lengthAdjust;
		this.effectiveDatabaseSize = databaseSize - numberOfSequences * lengthAdjust;
		this.searchSpaceSize = effectiveQuerySize * effectiveDatabaseSize;
	}

	public void print_values(int match) {
		System.out.println((int) Math.floor(gappedEvalueToNominal(100) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(100)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(10) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(10)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(1) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(1)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.1) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.1)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.001) / match));
		System.out.println(0.001);
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.00001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.00001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.0000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.0000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.0000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.0000000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.000000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.000000000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.000000000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.000000000000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.00000000000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.00000000000000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.0000000000000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.0000000000000000001)))));
		System.out.println((int) Math.floor(gappedEvalueToNominal(0.0000000000000000000001) / match));
		System.out.println(calculateEvalue(nominalToNormalizedScore(Math.floor(gappedEvalueToNominal(0.0000000000000000000001)))));
		System.out.println("lambda: " + lambda);
		System.out.println("H: " + H);
		System.out.println("K: " + K);
		System.out.println("Length Adjust: " + lengthAdjust);
		System.out.println("Effective query size: " + effectiveQuerySize);
		System.out.println("Effective database size: " + effectiveDatabaseSize);
		System.out.println("Search space size: " + searchSpaceSize);
	}
}
