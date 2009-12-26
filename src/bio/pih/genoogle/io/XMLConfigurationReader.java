/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.search.SearchManager;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RNAAlphabet;

import com.google.common.collect.Lists;

/**
 * Read and execute the configuration from a the XML file.
 * 
 * @author albrecht
 */
public class XMLConfigurationReader {

	private static Logger logger = Logger.getLogger(XMLConfigurationReader.class.getCanonicalName());

	private static File confFile = new File(Genoogle.getHome(), "conf" + File.separator + "genoogle.xml");

	private static Document doc = null;

	static {
		try {
			doc = new SAXReader().read(confFile);
		} catch (Exception e) {
			logger.fatal("Error reading the configuration at " + confFile + ".");
			logger.fatal(e);
		}
	}

	/**
	 * @return a brand new {@link SearchManager} with the parameters read from
	 *         genoogle.xml and with its data banks.
	 */
	public static SearchManager getSearchManager() throws IOException, ValueOutOfBoundsException, IllegalSymbolException, InvalidConfigurationException {
		Element rootElement = doc.getRootElement();
		Element searchManagerElement = rootElement.element("search-manager");
		SearchManager searchManager = new SearchManager(getMaxSimultaneousSearchs(searchManagerElement));

		List<AbstractSequenceDataBank> dataBanks = XMLConfigurationReader.getDataBanks();
		for (AbstractSequenceDataBank dataBank : dataBanks) {
			if (dataBank.load()) {
				searchManager.addDatabank(dataBank);
			} else {
				logger.fatal("It was not possible to load the data bank \"" + dataBank.getName() + "\".");
			}
		}

		return searchManager;
	}
	
	/**
	 * @return how many simultaneous searchs a searchManager can handle.
	 */
	private static int getMaxSimultaneousSearchs(Element searchManager) {
		Element maxSimultaneousSearchs = searchManager.element("max-simultaneous-searchs");
		String value = maxSimultaneousSearchs.attributeValue("value");
		return Integer.parseInt(value);
	}
	
	
	private static Integer match = null;
	private static Integer mismatch = null;
	
	public static int getMatchScore() {
		if (match == null) {
			Element rootElement = doc.getRootElement();
			Element scoreElement = rootElement.element("score");
			Element matchElement = scoreElement.element("match");
			String value = matchElement.attributeValue("value");
			match = Integer.parseInt(value);
		}		

		return match.intValue();
	}
	
	public static int getMismatchScore() {
		if (mismatch == null) {
			Element rootElement = doc.getRootElement();
			Element scoreElement = rootElement.element("score");
			Element mismatchElement = scoreElement.element("mismatch");
			String value = mismatchElement.attributeValue("value");
			mismatch = Integer.parseInt(value);
		}		

		return mismatch.intValue();
	}
	
	
	/**
	 * @return {@link List} of {@link AbstractSequenceDataBank} that are configured in
	 *         the XML file.
	 */
	@SuppressWarnings("unchecked")
	public static List<AbstractSequenceDataBank> getDataBanks() throws IOException, InvalidConfigurationException {
		Element rootElement = doc.getRootElement();
		Element databanks = rootElement.element("databanks");

		if (databanks == null) {
			return null;
		}

		List<AbstractSequenceDataBank> sequenceDataBanks = Lists.newLinkedList();
		Iterator<AbstractSimpleSequenceDataBank> databankIterator = databanks.elementIterator();
		while (databankIterator.hasNext()) {
			AbstractSequenceDataBank databank = getDatabank((Element) databankIterator.next(), null);
			if (databank == null) {
				return null;
			}
			sequenceDataBanks.add(databank);
		}

		return sequenceDataBanks;
	}

	@SuppressWarnings("unchecked")
	private static AbstractSequenceDataBank getDatabank(Element e,
			DatabankCollection<? extends AbstractSimpleSequenceDataBank> parent) throws IOException, InvalidConfigurationException {
		String name = e.attributeValue("name");
		String path = readPath(e.attributeValue("path"));
		String mask = e.attributeValue("mask");
		String lowComplexityFilterString = e.attributeValue("low-complexity-filter");
		String type = e.attributeValue("type");
				
		String subSequenceLengthString = e.attributeValue("sub-sequence-length");
		int subSequenceLength; 
		if (parent != null) {
			subSequenceLength = parent.getSubSequenceLength();
		} else {
			subSequenceLength = Integer.parseInt(subSequenceLengthString);
		}
		
		if (name == null) {
			throw new InvalidConfigurationException("Missing attribute name in element " + e.getName());
		}

		if (path == null) {
			throw new InvalidConfigurationException("Missing attribute path in element " + e.getName());
		}
		
		if (path.equals(name)) {
			throw new InvalidConfigurationException("It is not possible to have a FASTA (" + path + ") file with the same name (" + name + ").");
		}
		
		if ((parent != null) && path.equals(parent.getName())) {
			throw new InvalidConfigurationException("It is not possible to have a FASTA (" + path + ") file with the same name (" + parent.getName() + ") of the its parent data bank.");
		}
		
		int lowComplexityFilter = -1;
		if (lowComplexityFilterString != null) {
			lowComplexityFilter = Integer.parseInt(lowComplexityFilterString);
		}
		
		Alphabet alphabet = DNAAlphabet.SINGLETON;
		if (type != null) {
			if (type.toLowerCase().equals("dna")) {
				alphabet = DNAAlphabet.SINGLETON;
			} else if (type.toLowerCase().equals("rna")) {
				alphabet = RNAAlphabet.SINGLETON;
			} else {
				throw new InvalidConfigurationException("Sequences type: " + type + " is invalid.");
			}
		} else {
			if (parent != null) {
				alphabet = parent.getAlphabet();
			}
		}

		if (e.getName().trim().equals("split-databanks")) {
			int size = Integer.parseInt(e.attributeValue("number-of-sub-databanks"));

			SplittedSequenceDatabank splittedSequenceDatabank = new SplittedSequenceDatabank(name, alphabet, new File(Genoogle.getHome(), path), subSequenceLength, size, mask, lowComplexityFilter);
			
			Iterator databankIterator = e.elementIterator();
			while (databankIterator.hasNext()) {
				try {
					IndexedSequenceDataBank databank = (IndexedSequenceDataBank) getDatabank(
							(Element) databankIterator.next(), splittedSequenceDatabank);
					if (databank == null) {
						return null;
					}
					splittedSequenceDatabank.addDatabank(databank);
				} catch (DuplicateDatabankException e1) {
					logger.fatal("Duplicate databanks named " + e1.getDatabankName()
							+ " defined in " + e1.getDatabankName(), e1);
					return null;
				}
			}
			return splittedSequenceDatabank;
	
		} else if (e.getName().trim().equals("databank")) {				
			try {
				return new IndexedSequenceDataBank(name, alphabet, subSequenceLength, mask, new File(path), parent, lowComplexityFilter);
			} catch (ValueOutOfBoundsException e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			}
			return null;

		} else if (e.getName().trim().equals("databank-collection")) {			
			DatabankCollection<IndexedSequenceDataBank> databankCollection = new DatabankCollection<IndexedSequenceDataBank>(
					name, DNAAlphabet.SINGLETON, subSequenceLength, new File(path), parent, lowComplexityFilter);
			Iterator databankIterator = e.elementIterator();
			while (databankIterator.hasNext()) {
				try {
					IndexedSequenceDataBank databank = (IndexedSequenceDataBank) getDatabank(
							(Element) databankIterator.next(), databankCollection);
					if (databank == null) {
						return null;
					}
					databankCollection.addDatabank(databank);
				} catch (DuplicateDatabankException e1) {
					logger.fatal("Duplicate databanks named " + e1.getDatabankName()
							+ " defined in " + e1.getDatabankName(), e1);
					return null;
				}
			}
			return databankCollection;
		}
		logger.error("Unknow element name " + e.getName());
		return null;
	}

	private static Element getSearchParameters() {
		return doc.getRootElement().element("search-parameters");
	}

	/**
	 * @return max SubSequence distance
	 */
	public static int getMaxSubSequenceDistance() {
		String value = getSearchParameters().element("max-sub-sequence-distance")
				.attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default extended drop off specified at the XML configuration
	 *         file.
	 */
	public static int getExtendDropoff() {
		String value = getSearchParameters().element("extend-dropoff").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	/**
	 * @return default minimum length of a HSP to be keep to the next seaching phase.
	 */
	public static int getMinHspLength() {
		String value = getSearchParameters().element("min-hsp-length").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	/**
	 * @return how many Hits results. 
	 */
	public static int getMaxResults() {
		String value = getSearchParameters().element("max-hits-results").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return Max number of threads that will be used to search sub-sequences at the index.
	 */
	public static int getMaxThreadsIndexSearch() {
		String value = getSearchParameters().element("max-threads-index-search").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	/**
	 * @return Max number of threads that will be used to extend and align the HSP.
	 */
	public static int getMaxThreadsExtendAlign() {
		String value = getSearchParameters().element("max-threads-extend-align").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	/**
	 * @return minimum size of each input query slice.
	 */
	public static int getMinQuerySliceLength() {
		String value = getSearchParameters().element("min-query-slice-length").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	/**
	 *  @return how many slices the input query will be divided.
	 */
	public static int getQuerySplitQuantity() {
		String value = getSearchParameters().element("query-split-quantity").attributeValue("value");
		return Integer.parseInt(value);
	}
	
	private static String readPath(String path) {
		return path.replace('/', File.separatorChar);
	}


	private static Element getWebService() {
		return doc.getRootElement().element("web-service");
	}
	
	public static String getWebServiceAddress() {
		return getWebService().element("server-address").attributeValue("value");
	}
	
	public static Boolean useSessions() {
		String value = getWebService().element("use-sessions").attributeValue("value");
		return Boolean.parseBoolean(value);
	}

}
