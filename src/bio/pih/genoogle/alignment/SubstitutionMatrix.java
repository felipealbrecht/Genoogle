package bio.pih.genoogle.alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import bio.pih.genoogle.io.XMLConfigurationReader;

public class SubstitutionMatrix {
		
	public static final SubstitutionMatrix BLOSUM62 = SubstitutionMatrix.internalLoad("files/blosum/BLOSUM62.txt");
	public static final SubstitutionMatrix DUMMY = new DummySubstitutionTable(XMLConfigurationReader.getMismatchScore(), XMLConfigurationReader.getMatchScore());
	
	
	private final static int  MATRIX_SIZE = 255;
	private final int[][] valuesTable;
	private final int min;
	private final int max;
	private final char[] symbols;
	
	private SubstitutionMatrix(int min, int max) {
		this.valuesTable = null;
		this.symbols = null;
		this.min = min;
		this.max = max;
	}
	
	private SubstitutionMatrix(int[][] valuesTable, char[] symbols, int min, int max) {
		this.valuesTable = valuesTable;
		this.min = min;
		this.max = max;
		this.symbols = symbols;
	}
	
	public int getValue(char a, char b) {
		if (a >= MATRIX_SIZE && b >= MATRIX_SIZE) {
			// TODO			
		}		
		// TODO SUPER WORKARROUND FOR $ (end match
		if ((a == '$') || (a == '#')) {
			a = '*';
		}

		if ((b == '$') || (b == '#')) {
			b = '*';
		}
		
		int value = valuesTable[a][b];
		if (value == Integer.MIN_VALUE) {
			return min;
		}
		return value;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public int getSymbolsCount() {
		return symbols.length;
	}
	
	public char[] getSymbols() {
		return symbols;
	}
	
	public static SubstitutionMatrix internalLoad(String file) {
		try {
			return SubstitutionMatrix.load(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	public static SubstitutionMatrix load(String file) throws IOException {
		int[][] valuesTable = new int[MATRIX_SIZE][MATRIX_SIZE];
		
		int min = Integer.MAX_VALUE;
		int max= Integer.MIN_VALUE;
		
		
		
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				valuesTable[i][j] = Integer.MIN_VALUE;
			}
		}
		
		HashMap<Integer, Character> charPosition = new HashMap<Integer, Character>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
				
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.charAt(0) == '#' || line.isEmpty()) {
				continue;
			} else {
				break;
			}
		}
		
		String[] symbols = line.split("\\s+");		
		char[] chars = new char[symbols.length];
		
		for (int i = 0; i < chars.length; i++) {
			chars[i] = symbols[i].charAt(0);
			charPosition.put(i, chars[i]);
			// TODO: check MATRIX_SIZE
		}
		
		int cPos = 0;
		
		while ((line = br.readLine()) != null && cPos < chars.length ) {
			line = line.trim();
			
			String[] values = line.split("\\s+");
			
			char actualChar = values[0].charAt(0);
			
			for (int i = 1; i <= values.length && i <= chars.length; i++) {
				int value = Integer.parseInt(values[i]);				
				char colChar = charPosition.get(i-1);				
				valuesTable[actualChar][colChar] = value;
				if (value < min) {
					min = value;
				}
				else if (value > max) {
					max = value;
				}
			}			
		}
		
		return new SubstitutionMatrix(valuesTable, chars, min, max);
	}
	
	private static final class DummySubstitutionTable extends SubstitutionMatrix {
		int matchScore;
		int misMatchScore;
		
		public DummySubstitutionTable(int mismatch, int match) {
			super(mismatch, match);
			this.misMatchScore = mismatch;
			this.matchScore = match;
		}
		
		public int getValue(char a, char b) {
			if (a == b) {
				return matchScore;
			}
			return misMatchScore;
		}
	}
}
