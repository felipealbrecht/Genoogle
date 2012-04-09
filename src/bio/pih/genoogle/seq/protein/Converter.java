package bio.pih.genoogle.seq.protein;

import java.util.Arrays;
import java.util.HashMap;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.io.Utils;
import bio.pih.genoogle.seq.AminoAcid;
import bio.pih.genoogle.seq.AminoAcidAlphabet;
import bio.pih.genoogle.seq.Codon;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.Reduced_AA_8_Alphabet;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIterator;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

public class Converter {

	static SymbolListWindowIteratorFactory factory = SymbolListWindowIteratorFactory
			.getNotOverlappedFactory();

	public static SymbolList dnaToProtein(SymbolList dna) {
		SymbolListWindowIterator iterator = factory
				.newSymbolListWindowIterator(dna, 3);
		StringBuilder protein = new StringBuilder();

		while (iterator.hasNext()) {
			SymbolList next = iterator.next();
			AminoAcid aa = Codon.INSTANCE.convert(next.seqString());	
			protein.append(aa.getSymbol());
		}

		try {
			return LightweightSymbolList.createProtein(protein.toString());
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static SymbolList dnaToProtein1(SymbolList dna) {
		return dnaToProtein(dna);
	}
	
	public static SymbolList dnaToProtein2(SymbolList dna) {
		return dnaToProtein(dna.subSymbolList(2, dna.getLength()));
	}
	
	public static SymbolList dnaToProtein3(SymbolList dna) {
		return dnaToProtein(dna.subSymbolList(3, dna.getLength()));		
	}
	
	public static SymbolList dnaToProteinComplement1(SymbolList dna) {
		String inverted = Utils.invert(dna.seqString());
		String rcString = Utils.sequenceComplement(inverted);
		try {
			SymbolList sequence = dna.createSequence(rcString);
			return dnaToProtein(sequence);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static SymbolList dnaToProteinComplement2(SymbolList dna) {
		String inverted = Utils.invert(dna.seqString());
		String rcString = Utils.sequenceComplement(inverted);
		String substring = rcString.substring(1);		
		try {
			SymbolList sequence = dna.createSequence(substring);
			return dnaToProtein(sequence);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
			
	public static SymbolList dnaToProteinComplement3(SymbolList dna) {
		String inverted = Utils.invert(dna.seqString());
		String rcString = Utils.sequenceComplement(inverted);
		String substring = rcString.substring(2);
		try {
			SymbolList sequence = dna.createSequence(substring);
			return dnaToProtein(sequence);
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	static HashMap<Character, Character> proteinToReducedMap = new HashMap<Character, Character>();
	static {
		proteinToReducedMap.put('A', 'A');
		proteinToReducedMap.put('V', 'A');
		
		proteinToReducedMap.put('C', 'C');
		proteinToReducedMap.put('G', 'C');
		proteinToReducedMap.put('N', 'C');
		proteinToReducedMap.put('P', 'C');

		proteinToReducedMap.put('D', 'D');

		proteinToReducedMap.put('E', 'E');
		proteinToReducedMap.put('K', 'E');
		proteinToReducedMap.put('R', 'E');
		proteinToReducedMap.put('Q', 'E');

		proteinToReducedMap.put('F', 'F');
		proteinToReducedMap.put('W', 'F');
		proteinToReducedMap.put('Y', 'F');
		proteinToReducedMap.put('H', 'F');

		proteinToReducedMap.put('I', 'I');
		proteinToReducedMap.put('L', 'I');
		proteinToReducedMap.put('M', 'I');

		proteinToReducedMap.put('S', 'S');
		proteinToReducedMap.put('T', 'S');

		proteinToReducedMap.put('#', 'X');
		proteinToReducedMap.put('$', 'X');
	}

	public static SymbolList proteinToReducedAA(SymbolList protein) {
		StringBuilder r = new StringBuilder();
		
		if (protein.getAlphabet() != AminoAcidAlphabet.SINGLETON) {
			throw new RuntimeException("Invalid alphabet " + protein.getAlphabet());
		}

		for (int i = 1; i <= protein.getLength(); i++) {
			r.append(proteinToReducedMap.get(protein.symbolAt(i)));
		}
		
		try {
			return LightweightSymbolList.createReducedAA(r.toString());
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	public static String proteinToReducedAAString(String protein) {
		StringBuilder r = new StringBuilder();
		for (int i = 0; i < protein.length(); i++) {
			r.append(proteinToReducedMap.get(protein.charAt(i)));
		}		
		return r.toString();
	}

	public static SymbolList dnaToReducedAA(SymbolList dna) {
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		StringBuilder r = new StringBuilder();

		while (iterator.hasNext()) {
			SymbolList next = iterator.next();
			AminoAcid aa = Codon.INSTANCE.convert(next.seqString());
			r.append(proteinToReducedMap.get(aa.getSymbol()));
		}

		try {
			return LightweightSymbolList.createReducedAA(r.toString());
		} catch (IllegalSymbolException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws IllegalSymbolException {
		String s = "TAAAAACGTGCAGGCCAACGGTACGGAAAAAGCAGCAAAAGCCTACCTGAACTGGCTCTACAGCCCGCAGGCGCAGACCATCATCACCGACTATTACTAC";
		SymbolList createDNA = LightweightSymbolList.createDNA(s);
		
		SymbolList dnaToProtein = Converter.dnaToProtein(createDNA);
		SymbolList dnaToReduced= Converter.dnaToReducedAA(createDNA);
		
		System.out.println(dnaToProtein);
		System.out.println(dnaToReduced);
		
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(Reduced_AA_8_Alphabet.SINGLETON, 3);
		
		int[] is = encoder.encodeSymbolListToIntegerArray(dnaToReduced);
		System.out.println(Arrays.toString(is));
		System.out.println(encoder.decodeIntegerArrayToString(is));		
	}
	
	
	
	
}
