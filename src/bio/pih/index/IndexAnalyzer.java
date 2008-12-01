package bio.pih.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.search.SearchManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IndexAnalyzer {

	public static void main(String[] args) throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException, BioException {
		PropertyConfigurator.configure("conf/log4j.properties");
		SearchManager sm = XMLConfigurationReader.getSearchManager();
		IndexAnalyzer analyzer = new IndexAnalyzer();
				
		String line;
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
		while ((line = lineReader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			
			String[] commands = line.split(" ");
			if (commands[0].equals("analyze") && commands.length >= 2) {
				boolean found = false;
				String dataBankName = commands[1];
				int qtd = 30;
				if (commands.length == 3) {
					qtd = Integer.parseInt(commands[2]);
				}
				for (SequenceDataBank dataBank: sm.getDatabanks()) {
					if (dataBank.getName().equals(dataBankName)) {
						found = true;
						analyzer.analyzerDNASequenceDataBank((IndexedDNASequenceDataBank) dataBank, 30);
					}
					if (dataBank instanceof DatabankCollection) {
						DatabankCollection<SequenceDataBank> collection = (DatabankCollection<SequenceDataBank>) dataBank;
						SequenceDataBank subDataBank = collection.getDatabank(dataBankName);
						if (subDataBank != null) {
							found = true;
							analyzer.analyzerDNASequenceDataBank((IndexedDNASequenceDataBank) subDataBank, qtd);
						}
					}
				}
				if (!found) { 
					System.out.println(dataBankName + " not found.");
				}
			}
			
			if (commands[0].equals("list")) {
				for (SequenceDataBank dataBank: sm.getDatabanks()) {
					System.out.println(dataBank.getName() + "\t" + dataBank.getClass());

					if (dataBank instanceof DatabankCollection) {
						DatabankCollection<SequenceDataBank> collection = (DatabankCollection<SequenceDataBank>) dataBank;
						Iterator<SequenceDataBank> databanksIterator = collection.databanksIterator();
						while (databanksIterator.hasNext()) {
							SequenceDataBank next = databanksIterator.next();
							System.out.println(next.getName() + "\t" + next.getClass());
						}
					}
				}
			} else {
				System.out.println("Unknow command.");
			}
		}		
	}
		
	public void analyzerDNASequenceDataBank(IndexedDNASequenceDataBank dataBank, int maxSequences) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		int indexBitsSize = dataBank.getSubSequenceLength() * SequenceEncoder.bitsByAlphabetSize(dataBank.getAlphabet().size());
		int indexSize = 1 << indexBitsSize;
		List<SequenceQuantity> sequences = Lists.newArrayList();
		
		long total = 0;
		
		for (int i = 0; i < indexSize; i++) {
			long[] matchingSubSequence = dataBank.getMatchingSubSequence(i);			
			SequenceQuantity sequenceQuantity = new SequenceQuantity(i, matchingSubSequence.length);
			total += matchingSubSequence.length;
			sequences.add(sequenceQuantity);
		}
		
		
		Collections.sort(sequences, new Comparator<SequenceQuantity>() {
			@Override
			public int compare(SequenceQuantity o1, SequenceQuantity o2) {
				return o2.qtd - o1.qtd;
			}			
		});
		
		long totalShow = 0;
		System.out.println("Total subsequences: " + total);
		System.out.println("AVG: " + total/indexSize);
		for (int i = 0; i < maxSequences; i++) {
			SequenceQuantity sequenceQuantity = sequences.get(i);
			System.out.println(sequenceQuantity.toString(dataBank.getEncoder()));
			totalShow += sequenceQuantity.getQtd();
		}
		
		
		double perc = (totalShow * 100) / total;
		System.out.println("Total show: " + totalShow + " and is " + perc + "% of all.");
	}
	
	public void analyzerDNASequenceDataBank(DatabankCollection<IndexedSequenceDataBank> collection, int maxSequences) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		int indexBitsSize = collection.getSubSequenceLength() * SequenceEncoder.bitsByAlphabetSize(4);
		int indexSize = 1 << indexBitsSize;
		
		Map<Integer, SequenceQuantity> sequences = Maps.newHashMap();
		
		Iterator<IndexedSequenceDataBank> iterator = collection.databanksIterator();
		while (iterator.hasNext()) {
			IndexedSequenceDataBank dataBank = iterator.next();
			for (int i = 0; i < indexSize; i++) {
				long[] matchingSubSequence = dataBank.getMatchingSubSequence(i);
				
				SequenceQuantity sequenceQuantity = sequences.get(i); 
				if (sequenceQuantity == null) {
					sequenceQuantity = new SequenceQuantity(i, matchingSubSequence.length);
					sequences.put(i, sequenceQuantity);
				} else {
					sequenceQuantity.sum(matchingSubSequence.length);
				}				
			}
		}
						
		List<SequenceQuantity> sequencesQtd = new LinkedList<SequenceQuantity>(sequences.values());		
		Collections.sort(sequencesQtd, new Comparator<SequenceQuantity>() {
			@Override
			public int compare(SequenceQuantity o1, SequenceQuantity o2) {
				return o1.qtd - o2.qtd;
			}			
		});
		
		for (int i = 0; i < maxSequences; i++) {
			System.out.println(sequences.get(i).toString());
		}
	}
	
	protected class SequenceQuantity {
		int sequence;
		int qtd;
		
		public SequenceQuantity(int sequence, int qtd) {
			this.sequence = sequence;
			this.qtd = qtd;
		}
		
		public void sum(int qtd) {
			this.qtd += qtd;
		}
		
		public String toString(DNASequenceEncoderToInteger encoder) {
			StringBuilder sb = new StringBuilder('[');
			String seq = encoder.decodeIntegerToString(sequence);
			sb.append(seq);
			sb.append(',');
			sb.append(qtd);
			sb.append(']');
			
			return sb.toString();
		}
		
		public int getQtd() {
			return qtd;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder('[');
			sb.append(sequence);
			sb.append(',');
			sb.append(qtd);
			sb.append(']');
			
			return sb.toString();
		}		
	}
	
}
