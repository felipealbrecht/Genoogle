package bio.pih.indexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;
import bio.pih.util.NotOverlappedSymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIterator;

/**
 * @author albrecht
 */
public class Indexer {
	/**
	 * Test the index generation
	 * @param args
	 */
	public static void main(String[] args) {

		
		try {
			int subSequenceLenght = 11;
			SimpleSubSequencesIndex index = new SimpleSubSequencesIndex(AlphabetManager.alphabetForName("DNA"), subSequenceLenght);
			BufferedReader is = new BufferedReader(new FileReader("data/ecoli.nt"));

			LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);		
			
			long currentTimeMillis = System.currentTimeMillis();
			while (readFastaDNA.hasNext()) {				
				Sequence s = readFastaDNA.nextSequence();
				System.out.println("Loading " + s.getName());
				SymbolListWindowIterator iterator = new NotOverlappedSymbolListWindowIterator(s, subSequenceLenght);				                                           
				while (iterator.hasNext()) {
					SymbolList subSymbolList= iterator.next();
					SubSequenceInfo info = new SubSequenceInfo(s, subSymbolList, iterator.getActualPos(), iterator.getWindowSize());
					index.addSubSequence(subSymbolList, info);					
				}			
			}
			System.out.println("Cache count: " + LightweightSymbolList.getCacheCount());
			System.out.println("Item no indice: " + index.getTotal());
			System.out.println("Subsequencias: " + index.getSubSymbolTotal());
			System.out.println("Tempo total: " + (System.currentTimeMillis() - currentTimeMillis) + "ms");
		} catch (NoSuchElementException ex) {
			// no fasta sequences in the file
			ex.printStackTrace();
		} catch (BioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException ex) {
			// problem reading file
			ex.printStackTrace();
		}
	}
}
