package bio.pih.seq.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;


/**
 * @author Albrecht
 * 
 * A simple sequence populator.
 */
public class DNASequencesPopulator {

	/**
	 * Create a random sequence population
	 * @param sequenceQuantity
	 * @param sizeFrom
	 * @param sizeTo
	 * @param path
	 * @return a {@link List} containing random sequences
	 */
	public static List<Sequence> populateSequences(int sequenceQuantity, int sizeFrom, int sizeTo)  {				
		
		Random random = new Random();
		int value = random.nextInt(sizeTo - sizeFrom) + sizeFrom;
		 
		List<Sequence> sequences = new LinkedList<Sequence>();
		for (int i = 0; i < sequenceQuantity; i++) {
			sequences.add( new RandomSequenceGenerator(DNATools.getDNA(), value).generateSequence() );
		}
		
		return sequences;
	}
	
	/**
	 * Write a sequence population to a file. 
	 * @param sequences
	 * @param path
	 * @return <code>true</code> if the operation was done successfully 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean writePopulation(List<Sequence> sequences, String path) throws FileNotFoundException, IOException {
		File file = new File(path);
		if (file.exists()) {
			return false;			
		}
		
		new ObjectOutputStream(new FileOutputStream(file)).writeObject(sequences);
		return true;
	}
	
	/**
	 * Read a sequence population from a given path
	 * @param path
	 * @return a {@link List} containing {@link Sequence}
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static List<Sequence> readPopulation(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(path);
		if (file.exists()) {
			List<Sequence> sequences = (List<Sequence>) new ObjectInputStream(new FileInputStream(file)).readObject();
			return sequences;
		}
		return null;
	}
	
}
