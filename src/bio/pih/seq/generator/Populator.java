package bio.pih.seq.generator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.biojava.bio.seq.Sequence;

/**
 * @author albrecht
 *
 */
public class Populator {
 /**
 * <dna> <qtd> <range_from> <range_to> <file_path> <1-3>
 * @param args
 * @throws FileNotFoundException
 * @throws IOException
 */
public static void main(String[] args) throws FileNotFoundException, IOException {
	if (args.length != 6) 
	{
		System.out.println("Quantidade de parametros invalido");
		System.out.println("<dna|rna|prot> <qtd> <length_range_from> <length_range_to> <file_path>" );
		return;
	}
	 
	if (!args[0].equals("dna")) {
		System.out.println("Only dna is currently availiable :-(");
		return;
	}
	
	int qtd = Integer.parseInt(args[1]);
	int lengthRangeFrom = Integer.parseInt(args[2]);
	int lengthRangeTo = Integer.parseInt(args[3]);
	String filePath = args[4];
	int writeTo = Integer.parseInt(args[5]);
	
	List<Sequence> populateSequences = DNASequencesPopulator.populateSequences(qtd, lengthRangeFrom, lengthRangeTo);
	if ((writeTo & 1) == 1) {
		DNASequencesPopulator.writePopulation(populateSequences, filePath+".seqs");	
	}
	
	if ((writeTo & 2) == 2) {
		DNASequencesPopulator.writePopulationAsFasta(populateSequences, filePath+".fasta");
	}
}
}
