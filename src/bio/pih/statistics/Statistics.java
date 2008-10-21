package bio.pih.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Maps;

public class Statistics {

	private static final double BLAST_KARLIN_LAMBDA0_DEFAULT = 0.5;
	private static final double BLAST_KARLIN_LAMBDA_ACCURACY_DEFAULT = (1.e-5);
	private static final int BLAST_KARLIN_LAMBDA_ITER_DEFAULT = 17;
	
	private static final double GAPPED_NORMALIZED_DROPOFF = 30.0;
	private static final double GAPPED_FINAL_NORMALIZED_DROPOFF = 50.0;	
	
	private static final double LOG_2 = Math.log(2.0);

	private Map<Integer, Double> scoreProbabilities(int min, int max, SymbolList query)
			throws IndexOutOfBoundsException, BioException {
		SubstitutionMatrix matrix = new SubstitutionMatrix(DNATools.getDNA(), max, min);

		HashMap<Integer, Double> scoreProbabilities = Maps.newHashMap();
		for (int i = min; i <= max; i++) {
			scoreProbabilities.put(i, 0.0);
		}

		int numRegularLettersInQuery = 0;
		for (int i = 1; i <= query.length(); i++) {
			if (query.symbolAt(i).getMatches().getAlphabets().size() == 1) {
				numRegularLettersInQuery++;
			}
		}

		for (int i = 1; i <= query.length(); i++) {
			if (query.symbolAt(i).getMatches().getAlphabets().size() == 1) {
				Iterator iterator = DNATools.getDNA().iterator();
				while (iterator.hasNext()) {
					Symbol symbol = (Symbol) iterator.next();
					double probability = 250.00 / numRegularLettersInQuery;
					int score = matrix.getValueAt(query.symbolAt(i), symbol);
					Double value = scoreProbabilities.get(score);
					scoreProbabilities.put(score, value += probability);
				}
			}
		}

		final double sum = 1000.00;

		for (int i = min; i <= max; i++) {
			Double probability = scoreProbabilities.get(i);
			scoreProbabilities.put(i, probability / sum);
		}

		return scoreProbabilities;
	}

	private Double calculateLambda(Map<Integer, Double> prob, int min, int max) {
		if (!checkScoreRange(min, max)) {
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
			double x1 = Math.pow(x0, min - 1);
			if (Double.compare(x1, 0.0) == 0) {
				break;
			}
			for (int i = min; i <= max; i++) {
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
		return blastKarlinLambdaBis(prob, min, max);
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

	private double blastH(Map<Integer, Double> prob, double lambda, int min, int max) {
		if (lambda < 0.0) {
			return -1.0;
		}

		if (!checkScoreRange(min, max)) {
			return -1.0;
		}

		double etolam = Math.exp(lambda);
		double etolami = Math.pow(etolam, min - 1);
		double av = 0.0;
		if (etolami > 0.0) {
			for (int score = min; score <= max; score++) {
				etolami *= etolam;
				av += prob.get(score) * score * etolami;
			}
		} else {
			for (int score = min; score <= max; score++) {
				av += prob.get(score) * score * Math.exp(lambda * score);
			}
		}

		return lambda * av;
	}

	private double blastK(Map<Integer, Double> prob, double lambda, double h, int min, int max) {

		if (lambda <= 0.0 || h <= 0.0) {
			return -1.0;
		}

		double av = h / lambda;
		double etolam = Math.exp(lambda);
		if (min == -1 || max == 1) {
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
			int querySize, int databaseSize, int numberOfSequences) {
		double lenghtAdjust = 0;
		double minimumQueryLength = (double) 1 / K;

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

	private double ungappedNominal2Normalized(double nominalScore, double lambda,
			double logK, double log2) {
		return ((nominalScore * lambda) - logK) / log2;
	}

	private double calculateEvalue(double normalizedScore, double searchSpaceSize) {
		return searchSpaceSize / Math.pow(2, normalizedScore);
	}
	
	public double getEvalue(double nominalScore) {
		double normalizedScore = ungappedNominal2Normalized(nominalScore, lambda, logK, LOG_2);
		System.out.println(normalizedScore);
		double eValue = calculateEvalue(normalizedScore, searchSpaceSize);
		System.out.println("Evalue: " + eValue);
		return eValue;
	}

	public static void main(String[] args) throws IndexOutOfBoundsException, IllegalSymbolException, BioException {
		new Statistics(-3, 1, LightweightSymbolList.createDNA("ATGGTCGATGTACGTACGTCAGTGTCAGTGTCAGTC"), 850000, 1000);
	}
	
	private final SymbolList query;
	private final Map<Integer, Double> probabilities; 
	private final double lambda;
	private final double H;
	private final double K;
	private final double logK;
	private final double effectiveQuerySize;
	private final double effectiveDatabaseSize;
	private final double searchSpaceSize;
	private final double lengthAdjust;
	public Statistics(int match, int mismatch, SymbolList query, int databaseSize, int numberOfSequences) throws IndexOutOfBoundsException, IllegalSymbolException, BioException {
		this.query = query;
		this.probabilities = scoreProbabilities(match, mismatch, query);
		this.lambda = calculateLambda(probabilities, match, mismatch);
		this.H = blastH(probabilities, lambda, match, mismatch);
		this.K = blastK(probabilities, lambda, H, match, mismatch);

//		this.gappedNominalDropoff = Math.floor(GAPPED_NORMALIZED_DROPOFF * LOG_2 / lambda);
//		this.gappedFinalNominalDropoff = Math.floor(GAPPED_FINAL_NORMALIZED_DROPOFF * LOG_2/ lambda);
		this.logK = Math.log(K);

		this.lengthAdjust = lengthAdjust(K, logK, H, query.length(), databaseSize, numberOfSequences);

		this.effectiveQuerySize = query.length() - lengthAdjust;
		this.effectiveDatabaseSize = databaseSize - numberOfSequences * lengthAdjust;
		this.searchSpaceSize = effectiveQuerySize * effectiveDatabaseSize;
//		System.out.println("lambda: " + lambda);
//		System.out.println("H: " + H);
//		System.out.println("K: " + K);
//		System.out.println("Gapped Nominal Dropoff: " + gappedNominalDropoff);
//		System.out.println("Gapped Final Nominal Dropoff: " + gappedFinalNominalDropoff);
//		System.out.println("Length Adjust: " + lengthAdjust);
//		System.out.println("Effective query size: " + effectiveQuerySize);
//		System.out.println("Effective database size: " + effectiveDatabaseSize);
//		System.out.println("Search space size: " + searchSpaceSize);
	}
}
