package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.DNATools;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank.StorageKind;
import bio.pih.search.SearchManager;

import com.google.common.collect.Lists;

/**
 * Read and execute the configuration from a the XML file.
 * 
 * @author albrecht
 */
public class XMLConfigurationReader {

	private static Logger logger = Logger
			.getLogger("pih.bio.io.ConfigurationXMLReader");

	private static String path = "conf" + File.separator + "genoogle.xml";

	private static Document doc = null;

	static {
		try {
			doc = new SAXReader().read(new File(path));
		} catch (DocumentException e) {
			logger.fatal("Error reading the configuration at " + path + ".", e);
		}
	}

	/**
	 * @return a brand new {@link SearchManager} with the parameters read from
	 *         genoogle.xml and with its data banks.
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 */
	public static SearchManager getSearchManager() throws IOException,
			ValueOutOfBoundsException {
		Element rootElement = doc.getRootElement();
		Element searchManagerElement = rootElement.element("search-manager");
		SearchManager searchManager = new SearchManager(
				getMaxSimultaneousSearchs(searchManagerElement));

		List<SequenceDataBank> dataBanks = XMLConfigurationReader
				.getDataBanks();
		for (SequenceDataBank dataBank : dataBanks) {
			dataBank.load();
			searchManager.addDatabank(dataBank);
		}

		return searchManager;
	}

	/**
	 * @return how many simultaneous searchs a searchManager can handle.
	 */
	private static int getMaxSimultaneousSearchs(Element searchManager) {
		Element maxSimultaneousSearchs = searchManager
				.element("max-simultaneous-searchs");
		String value = maxSimultaneousSearchs.attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return {@link List} of {@link SequenceDataBank} that are configured in
	 *         the XML file.
	 */
	@SuppressWarnings("unchecked")
	public static List<SequenceDataBank> getDataBanks() {
		// TODO: to check if the XML is valid
		Element rootElement = doc.getRootElement();
		Element databanks = rootElement.element("databanks");

		if (databanks == null) {
			return null;
		}

		List<SequenceDataBank> sequenceDataBanks = Lists.newLinkedList();
		Iterator databankIterator = databanks.elementIterator();
		while (databankIterator.hasNext()) {
			SequenceDataBank databank = getDatabank((Element) databankIterator
					.next(), null);
			if (databank == null) {
				return null;
			}
			sequenceDataBanks.add(databank);
		}

		return sequenceDataBanks;
	}

	@SuppressWarnings("unchecked")
	private static SequenceDataBank getDatabank(Element e,
			DatabankCollection<? extends DNASequenceDataBank> parent) {
		String name = e.attributeValue("name");
		String path = readPath(e.attributeValue("path"));
		if (name == null) {
			logger.fatal("Missing attribute name in element " + e.getName());
			return null;
		}

		if (path == null) {
			logger.fatal("Missing attribute path in element " + e.getName());
			return null;
		}

		if (e.getName().trim().equals("databank")) {
			StorageKind storageKind = null;
			String storage = e.attributeValue("storage");
			if (storage == null || storage.equals("disk")) {
				storageKind = StorageKind.DISK;
			} else {
				storageKind = StorageKind.MEMORY;
			}
			try {
				return new IndexedDNASequenceDataBank(name, new File(path),
						parent, storageKind);
			} catch (ValueOutOfBoundsException e1) {
				logger.fatal("Error creating IndexedDNASequenceDataBank.", e1);
			}
			return null;

		} else if (e.getName().trim().equals("databank-collection")) {

			DatabankCollection<IndexedDNASequenceDataBank> databankCollection;
			databankCollection = new DatabankCollection<IndexedDNASequenceDataBank>(
					name, DNATools.getDNA(), new File(path), parent);
			Iterator databankIterator = e.elementIterator();
			while (databankIterator.hasNext()) {
				try {
					IndexedDNASequenceDataBank databank = (IndexedDNASequenceDataBank) getDatabank(
							(Element) databankIterator.next(),
							databankCollection);
					if (databank == null) {
						return null;
					}
					databankCollection.addDatabank(databank);
				} catch (DuplicateDatabankException e1) {
					logger.fatal("Duplicate databanks named "
							+ e1.getDatabankName() + " defined in "
							+ e1.getDatabankName(), e1);
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
	 * @return default subSequenceMinSimilarity specified at the XML
	 *         configuration file.
	 */
	public static int getSubSequenceMinSimilarity() {
		String value = getSearchParameters().element(
				"sub-sequence-min-similarity").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default data bank max sub sequence distance specified at the XML
	 *         configuration file.
	 */
	public static int getDataBankMaxSubSequenceDistance() {
		String value = getSearchParameters().element(
				"databank-max-sub-sequence-distance").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default data bank sequence minimum match area length specified at
	 *         the XML configuration file.
	 */
	public static int getDataBankMinMatchAreaLength() {
		String value = getSearchParameters().element(
				"databank-min-match-area-length").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default query max SubSequence distance specified at the XML
	 *         configuration file.
	 */
	public static int getQueryMaxSubSequenceDistance() {
		String value = getSearchParameters().element(
				"query-max-sub-sequence-distance").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default query minimum SubSequence length specified at the XML
	 *         configuration file.
	 */
	public static int getQueryMinSubSequenceLength() {
		String value = getSearchParameters().element(
				"query-min-sub-sequence-length").attributeValue("value");
		return Integer.parseInt(value);
	}

	/**
	 * @return default extended drop off specified at the XML configuration
	 *         file.
	 */
	public static int getExtendDropoff() {
		String value = getSearchParameters().element("extend-dropoff")
				.attributeValue("value");
		return Integer.parseInt(value);
	}

	private static String readPath(String path) {
		return path.replace('/', File.separatorChar);
	}

}
