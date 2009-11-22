package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.search.SearchManager;

import com.google.common.collect.Lists;

/**
 * Read and execute the configuration from a the XML file.
 * 
 * @author albrecht
 */
public class XMLConfigurationReader {

	private static Logger logger = Logger.getLogger("pih.bio.io.ConfigurationXMLReader");

	private static String path = "conf" + File.separator + "genoogle.xml";

	private static Document doc = null;

	static {
		try {
			doc = new SAXReader().read(new File(path));
		} catch (Exception e) {
			logger.fatal("Error reading the configuration at " + path + ".");
			logger.fatal(e);
		}
	}

	/**
	 * @return a brand new {@link SearchManager} with the parameters read from
	 *         genoogle.xml and with its data banks.
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 * @throws InvalidHeaderData 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 * @throws InvalidConfigurationException 
	 */
	public static SearchManager getSearchManager() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException, BioException, InvalidConfigurationException {
		Element rootElement = doc.getRootElement();
		Element searchManagerElement = rootElement.element("search-manager");
		SearchManager searchManager = new SearchManager(getMaxSimultaneousSearchs(searchManagerElement));

		List<AbstractSequenceDataBank> dataBanks = XMLConfigurationReader.getDataBanks();
		for (AbstractSequenceDataBank dataBank : dataBanks) {
			dataBank.load();
			searchManager.addDatabank(dataBank);
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
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 * @throws InvalidConfigurationException 
	 */
	@SuppressWarnings("unchecked")
	public static List<AbstractSequenceDataBank> getDataBanks() throws IOException, InvalidHeaderData, InvalidConfigurationException {
		// TODO: to check if the XML is valid
		Element rootElement = doc.getRootElement();
		Element databanks = rootElement.element("databanks");

		if (databanks == null) {
			return null;
		}

		List<AbstractSequenceDataBank> sequenceDataBanks = Lists.newLinkedList();
		Iterator<AbstractDNASequenceDataBank> databankIterator = databanks.elementIterator();
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
			DatabankCollection<? extends AbstractDNASequenceDataBank> parent) throws IOException, InvalidHeaderData, InvalidConfigurationException {
		String name = e.attributeValue("name");
		String path = readPath(e.attributeValue("path"));
		String mask = e.attributeValue("mask");
		String lowComplexityFilterString = e.attributeValue("low-complexity-filter");
		
		
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

		if (e.getName().trim().equals("split-databanks")) {
			int size = Integer.parseInt(e.attributeValue("number-of-sub-databanks"));

			SplittedSequenceDatabank splittedSequenceDatabank = new SplittedSequenceDatabank(name, new File(path), subSequenceLength, size, mask, lowComplexityFilter);
			
			Iterator databankIterator = e.elementIterator();
			while (databankIterator.hasNext()) {
				try {
					IndexedDNASequenceDataBank databank = (IndexedDNASequenceDataBank) getDatabank(
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
		}

		if (e.getName().trim().equals("databank")) {				
			try {
				return new IndexedDNASequenceDataBank(name, subSequenceLength, mask, new File(path), parent, lowComplexityFilter);
			} catch (ValueOutOfBoundsException e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			}
			return null;

		} else if (e.getName().trim().equals("databank-collection")) {			
			DatabankCollection<IndexedDNASequenceDataBank> databankCollection = new DatabankCollection<IndexedDNASequenceDataBank>(
					name, DNATools.getDNA(), subSequenceLength, new File(path), parent, lowComplexityFilter);
			Iterator databankIterator = e.elementIterator();
			while (databankIterator.hasNext()) {
				try {
					IndexedDNASequenceDataBank databank = (IndexedDNASequenceDataBank) getDatabank(
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

}
