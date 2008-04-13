package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.biojava.bio.seq.DNATools;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import com.google.common.collect.Lists;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;

public class ConfigurationXMLReader {
	private Document doc;
	
	private static Logger logger = Logger.getLogger("pih.bio.io.ConfigurationXMLReader");

	public ConfigurationXMLReader(String path) throws DocumentException {
		SAXReader xmlReader = new SAXReader();
		this.doc = xmlReader.read(new File(path));		
	}
	
	
	public List<SequenceDataBank> getDataBanks() {
		Element rootElement = doc.getRootElement();
		Element databanks = rootElement.element("databanks");
		
		if (databanks == null) {
			return null;
		}
		
		List<SequenceDataBank> sequenceDataBanks = Lists.newLinkedList();
		Iterator databankIterator = databanks.elementIterator();
		while(databankIterator.hasNext()) {
			SequenceDataBank databank = getDatabank((Element)databankIterator.next());
			if (databank == null) {
				return null;
			}
			sequenceDataBanks.add(databank);			
		}
				
		return sequenceDataBanks;		
	}
	
	private SequenceDataBank getDatabank(Element e) {
		String name = e.attributeValue("name");
		String path = e.attributeValue("path");
		if (name == null) {
			logger.fatal("Missing attribute name in element " + e.getName());
			return null;
		}
		
		if (path == null) {
			logger.fatal("Missing attribute path in element " + e.getName());
			return null;
		}
		
		if (e.getName().trim().equals("databank")) {
			try {
				return new IndexedDNASequenceDataBank(name, new File(path));
			} catch (ValueOutOfBoundsException e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			} catch (IOException e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			} catch (InvalidHeaderData e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			}
			return null;
			
		} else if (e.getName().trim().equals("databank-collection")) {
			
			DatabankCollection<IndexedDNASequenceDataBank> databankCollection;
			databankCollection = new DatabankCollection<IndexedDNASequenceDataBank>(name, DNATools.getDNA(), new File(path));
			Iterator databankIterator = e.elementIterator();
			while(databankIterator.hasNext()) {
				try {
					IndexedDNASequenceDataBank databank = (IndexedDNASequenceDataBank) getDatabank((Element)databankIterator.next());
					if (databank == null) {
						return null;
					}
					databankCollection.addDatabank(databank);
				} catch (DuplicateDatabankException e1) {
					logger.fatal("Duplicate databanks named " + e1.getDatabankName() + " defined in " + e1.getDatabankName(), e1);
					return null;
				}
			}
			return databankCollection;
		}
		logger.error("Unknow element name " + e.getName());
		return null;
	}
}
