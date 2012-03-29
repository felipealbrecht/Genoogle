package bio.pih.genoogle.statistics;

public interface Statistics {

	public static final double BLAST_KARLIN_LAMBDA0_DEFAULT = 0.5;
	public static final double BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT = (1.e-5);
	public static final int BLAST_KARLIN_LAMBDA_ITER_DEFAULT = 17;
	public static final double LOG_2 = Math.log(2.0);
	public static int BLAST_SCORE_1MIN = -10000;
	public static int BLAST_SCORE_1MAX = 1000;
	public static int BLAST_SCORE_RANGE_MAX = BLAST_SCORE_1MAX - BLAST_SCORE_1MIN;
	public static double BLAST_KARLIN_K_SUMLIMIT_DEFAULT = 0.01;
	public static int BLAST_KARLIN_K_ITER_MAX = 100;
	public static int DIMOFP0_MAX = BLAST_KARLIN_K_ITER_MAX * BLAST_SCORE_RANGE_MAX + 1;

	public static final int MAX_SCORE = 100;
	public static final int MIN_SCORE = -100;

	
	/**
	 * Normalize the score.
	 * 
	 * @param nominalScore
	 *            nominal alignment score.
	 * @return Alignment score normalized.
	 */
	public abstract double nominalToNormalizedScore(double nominalScore);

	/**
	 * Calculate the E-value from the normalized score.
	 * 
	 * @param normalizedScore
	 * @return E-Value.
	 */
	public abstract double calculateEvalue(double normalizedScore);

	/**
	 * E-Value to the score.
	 * 
	 * @param evalue
	 * @return nominal score.
	 */
	public abstract double gappedEvalueToNominal(double evalue);

	/**
	 * Look for the greatest common divisor
	 */
	public abstract long gcd(long a, long b);

}