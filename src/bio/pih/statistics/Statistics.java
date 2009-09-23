package bio.pih.statistics;

import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoder;

import com.google.common.collect.Maps;

/**
 * Class to calculate some statistics values for biological searching.
 * Basesd on FSA Blast source code.
 * 
 * TODO: To test with protein score matrix
 * TODO: To implement the calculation of K where the score matrix does not have -1 or 1.
 *    
 * @author albrecht (felipe.albrecht@gmail.com)
 */
public class Statistics {

	private static final double BLAST_KARLIN_LAMBDA0_DEFAULT = 0.5;
	private static final double BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT = (1.e-5);
	private static final int BLAST_KARLIN_LAMBDA_ITER_DEFAULT = 17;
		
	private static final double LOG_2 = Math.log(2.0);

	private Map<Integer, Double> scoreProbabilities(int dismatch, int match, SymbolList query)
			throws IndexOutOfBoundsException, BioException {
		
		int[][] baseValue = new int[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (i == j) {
					baseValue[i][j] = match;
				} else {
					baseValue[i][j] = dismatch;
				}
			}
		}


		int min = Math.min(dismatch, match);
		int max = Math.max(dismatch, match);
		int delta;
		if (min < 0) {
			delta = Math.abs(min);
		} else {
			delta = -min;
		}
		
		int scoreProbabilitiesSize = (min + delta) + (max + delta) + 1; 
		
		double[] scoreProbabilities = new double[scoreProbabilitiesSize];
		for (int i = dismatch + delta; i <= match + delta; i++) {
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
					scoreProbabilities[score+delta] += probability;
				}
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.c());
					int score = baseValue[querySymbolValue][symbolValue];					
					scoreProbabilities[score+delta] += probability;
				}	
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.g());
					int score = baseValue[querySymbolValue][symbolValue];					
					scoreProbabilities[score+delta] += probability;
				}	
				{
					int symbolValue = DNASequenceEncoder.getBitsFromSymbol(DNATools.t());
					int score = baseValue[querySymbolValue][symbolValue];					
					scoreProbabilities[score+delta] += probability;
				}	
			}
		}


		final double sum = 1000.00;

		for (int i = dismatch+delta; i <= match+delta; i++) {
			double probability = scoreProbabilities[i];
			scoreProbabilities[i] = probability / sum;
		}

		Map<Integer, Double> scoreProbabilitiesMap = Maps.newHashMap();
		for (int i = dismatch; i <= match; i++) {
			scoreProbabilitiesMap.put(i, scoreProbabilities[i+delta]);			
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

		throw new RuntimeException("Not implemented if the max or min value is not 1 or -1.");
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

	private double lengthAdjust(double K, double ungappedLogK, double ungappedH,
			int querySize, long databaseSize, long numberOfSequences) {
		double lenghtAdjust = 0;
		double minimumQueryLength =  1 / K;

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
	
	public int getMinLengthDropOut() {
		return minLength;
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
	private final int minLength;
	public Statistics(int match, int mismatch, SymbolList query, long databaseSize, long numberOfSequences, double minEvalue) throws IndexOutOfBoundsException, BioException{
		this.probabilities = scoreProbabilities(mismatch, match, query);
		this.lambda = calculateLambda(probabilities, mismatch, match);
		this.H = blastH(probabilities, lambda, mismatch, match);
		this.K = blastK(probabilities, lambda, H, mismatch, match);

		this.logK = Math.log(K);

		this.lengthAdjust = lengthAdjust(K, logK, H, query.length(), databaseSize, numberOfSequences);

		this.effectiveQuerySize = query.length() - lengthAdjust;
		this.effectiveDatabaseSize = databaseSize - numberOfSequences * lengthAdjust;
		this.searchSpaceSize = effectiveQuerySize * effectiveDatabaseSize;
		this.minLength = (int) Math.floor(gappedEvalueToNominal(minEvalue) / match);
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
