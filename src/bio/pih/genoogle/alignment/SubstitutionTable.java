package bio.pih.genoogle.alignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import bio.pih.genoogle.io.XMLConfigurationReader;

public class SubstitutionTable {
		
	public static final SubstitutionTable BLOSUM62 = SubstitutionTable.internalLoad("files/blosum/BLOSUM62.txt");
	public static final SubstitutionTable DUMMY = new DummySubstitutionTable(XMLConfigurationReader.getMatchScore(), XMLConfigurationReader.getMismatchScore());
	
	
	private final static int  MATRIX_SIZE = 255;
	private int[][] valuesTable;
	
	private SubstitutionTable()  { }
	
	private SubstitutionTable(int[][] valuesTable) {
		this.valuesTable = valuesTable;
	}
	
	public int getValue(char a, char b) {
		if (a >= MATRIX_SIZE && b >= MATRIX_SIZE) {
			// TODO			
		}		
		return valuesTable[a][b];
		
	}
	
	public static SubstitutionTable internalLoad(String file) {
		try {
			return SubstitutionTable.load(file);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	public static SubstitutionTable load(String file) throws IOException {
		int[][] valuesTable = new int[MATRIX_SIZE][MATRIX_SIZE];
		
		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				valuesTable[i][j] = -10;
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
		
		String[] chars = line.split("\\s+");
		
		for (int i = 0; i < chars.length; i++) {
			charPosition.put(i, chars[i].charAt(0));
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
			}			
		}
		
//		for (int i = 0; i < chars.length; i++) {
//			for (int j = 0; j < chars.length; j++) {
//				char charLine = charPosition.get(i);
//				char charCol = charPosition.get(j);				
//				System.out.print(valuesTable[charLine][charCol]);				
//				System.out.print(" ");
//			}
//			System.out.println();
//		}
		
		return new SubstitutionTable(valuesTable);
	}
	
	private static final class DummySubstitutionTable extends SubstitutionTable {
		int matchScore;
		int misMatchScore;
		
		public DummySubstitutionTable(int m, int d) {
			this.matchScore = m;
			this.misMatchScore = d;
		}
		
		public int getValue(char a, char b) {
			if (a == b) {
				return matchScore;
			}
			return misMatchScore;
		}
	}
}
