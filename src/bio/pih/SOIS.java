package bio.pih;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.ConfigurationXMLReader;
import bio.pih.io.DatabankCollection;
import bio.pih.io.DuplicateDatabankException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Output;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.mutation.SequenceMutator;

public class SOIS {

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchElementException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData
	 * @throws DuplicateDatabankException
	 * @throws UnknowDataBankException
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException, InvalidHeaderData, DuplicateDatabankException, UnknowDataBankException, DocumentException {
		BasicConfigurator.configure();
		            
		String seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
		//String seq = SequenceMutator.mutateSequence(s, 20, 4);
		//System.out.println(s);
		//System.out.println(seq);
		LightweightSymbolList sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);
		
		SearchManager sm = new SearchManager();
		
		List<SequenceDataBank> dataBanks = new ConfigurationXMLReader("conf/genoogle.xml").getDataBanks();
		for (SequenceDataBank dataBank: dataBanks) {
			dataBank.loadInformations();
			sm.addDatabank(dataBank);
		}
		
		SearchParams sp = new SearchParams(sequence, "RefSeq");
		long beginTime = System.currentTimeMillis(); 
		long code = sm.doSearch(sp);

		while (!sm.checkSearch(code)) {
			Thread.yield();
		}

		Document document = Output.genoogleOutputToXML(sm.getResult(code));
		System.out.println("total time: " + (System.currentTimeMillis() - beginTime));

		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(new FileOutputStream(new File("output.xml")), outformat);
		writer.write(document);
		writer.flush();
		
		
		beginTime = System.currentTimeMillis();
		code = sm.doSearch(sp);
		while (!sm.checkSearch(code)) {
			Thread.yield();
		}
		System.out.println("total time: " + (System.currentTimeMillis() - beginTime));
		
		
		beginTime = System.currentTimeMillis();
		code = sm.doSearch(sp);
		while (!sm.checkSearch(code)) {
			Thread.yield();
		}
		System.out.println("total time: " + (System.currentTimeMillis() - beginTime));
	}
}
