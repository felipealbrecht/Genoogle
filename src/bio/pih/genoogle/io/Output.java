package bio.pih.genoogle.io;

import java.util.Formatter;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.ProcessingInstruction;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.search.SearchParams;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.search.results.Hit;
import bio.pih.genoogle.search.results.SearchResults;

import com.google.common.collect.Maps;

/**
 * 
 * Visitator class that create a XML with the results.
 * 
 * @author albrecht
 */
public class Output {

	private final static String SIMPLE_DOUBLE_FORMAT = "%10.4f";
	private final static String SCIENTIFIC_DOUBLE_FORMAT = "%10.4e";

	/**
	 * @param searchResults
	 * 
	 * @return {@link Document} containing the {@link SearchResults} in XML form.
	 */
	public static Document genoogleOutputToXML(List<SearchResults> searchResults) {
		assert searchResults != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Document doc = factory.createDocument();
		doc.setName("genoogle");

		Element output = doc.addElement(Genoogle.SOFTWARE_NAME);
		output.addAttribute("version", Genoogle.VERSION.toString());
		output.addAttribute("copyright", Genoogle.COPYRIGHT_NOTICE);

		Element iterationsElement = output.addElement("iterations");
		for (int i = 0; i < searchResults.size(); i++) {
			Element iterationElement = iterationsElement.addElement("iteration").addAttribute("number",
					String.valueOf(i));
			iterationElement.add(searchResultToXML(searchResults.get(i)));
		}

		return doc;
	}

	/**
	 * @param searchResult
	 * 
	 * @return {@link Document} containing the {@link SearchResults} in XML form.
	 */
	public static Document genoogleOutputToXML(SearchResults searchResult) {
		assert searchResult != null;
		Element output = genoogleXmlHeader();
		output.add(searchResultToXML(searchResult));
				
		return output.getDocument();
	}

	public static Element genoogleXmlHeader() {
		DocumentFactory factory = DocumentFactory.getInstance();

		Document doc = factory.createDocument();
		doc.setName("genoogle");

		Map<String, String> xslProcessing = Maps.newHashMap();
		xslProcessing.put("type", "text/xsl");
		xslProcessing.put("href", "results.xsl");
		ProcessingInstruction xsltInstruction = DocumentHelper.createProcessingInstruction("xml-stylesheet",
				xslProcessing);
		doc.add(xsltInstruction);

		Element output = doc.addElement("genoogle");
		output.addElement("references").addAttribute("program", Genoogle.SOFTWARE_NAME).addAttribute("version",
				Double.toString(Genoogle.VERSION)).addAttribute("copyright", Genoogle.COPYRIGHT_NOTICE);
		return output;
	}
	
	/**
	 * @param searchResult
	 * @return {@link Element} containing the {@link SearchResults} at XML form.
	 */
	public static Element searchResultToXML(SearchResults searchResult) {
		assert searchResult != null;
		if (searchResult.hasFail()) {
			for (Throwable e : searchResult.getFails()) {
				e.printStackTrace(System.err);
			}
		}
		DocumentFactory factory = DocumentFactory.getInstance();
		Element resultsElement = factory.createElement("results");
		resultsElement.add(paramsToXML(searchResult.getParams()));
		resultsElement.add(hitsToXML(searchResult.getHits()));

		return resultsElement;
	}

	/**
	 * @param params
	 * @return {@link Element} containing the {@link SearchParams} at XML form.
	 */
	public static Element paramsToXML(SearchParams params) {
		assert params != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element paramsElement = factory.createElement("params");

		paramsElement.addAttribute("query", params.getQuery().seqString());
		paramsElement.addAttribute("databank", params.getDatabank());
		paramsElement.addAttribute("maxSubSequencesDistance", Integer.toString(params.getMaxSubSequencesDistance()));
		paramsElement.addAttribute("minHspLength", Integer.toString(params.getMinHspLength()));

		return paramsElement;
	}

	/**
	 * @param hits
	 * @return {@link Element} containing the {@link List} of {@link Hit} at XML form.
	 */
	public static Element hitsToXML(List<Hit> hits) {
		assert hits != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hitsElement = factory.createElement("hits");
		for (Hit hit : hits) {
			hitsElement.add(hitToXML(hit));
		}

		return hitsElement;
	}

	/**
	 * @param hit
	 * @return {@link Element} containing the {@link Hit} at XML form.
	 */
	public static Element hitToXML(Hit hit) {
		assert hit != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hitElement = factory.createElement("hit");
		hitElement.addAttribute("id", hit.getId());
		hitElement.addAttribute("gi", hit.getGi());
		hitElement.addAttribute("description", hit.getDescription());
		hitElement.addAttribute("accession", hit.getAccession());
		hitElement.addAttribute("length", Integer.toString(hit.getLength()));
		hitElement.addAttribute("databank", hit.getDatabankName());
		hitElement.add(hspsToXML(hit.getHSPs()));

		return hitElement;
	}

	/**
	 * @param hsps
	 * @return {@link Element} containing the {@link List} of {@link HSP} at XML form.
	 */
	public static Element hspsToXML(List<HSP> hsps) {
		assert hsps != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hspsElement = factory.createElement("hsps");
		for (HSP hsp : hsps) {
			hspsElement.add(hspToXML(hsp));
		}

		return hspsElement;
	}

	/**
	 * @param hsp
	 * @return {@link Element} containing the {@link HSP} at XML form.
	 */
	public static Element hspToXML(HSP hsp) {
		assert hsp != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hspElement = factory.createElement("hsp");
		hspElement.addAttribute("score", Double.toString(hsp.getScore()));
		hspElement.addAttribute("normalized-score", Double.toString(hsp.getNormalizedScore()));
		hspElement.addAttribute("e-value", Double.toString(hsp.getEValue()));
		hspElement.addAttribute("query-from", Integer.toString(hsp.getQueryFrom()));
		hspElement.addAttribute("query-to", Integer.toString(hsp.getQueryTo()));
		hspElement.addAttribute("hit-from", Integer.toString(hsp.getHitFrom()));
		hspElement.addAttribute("hit-to", Integer.toString(hsp.getHitTo()));
		hspElement.addAttribute("identity-len", Integer.toString(hsp.getIdentityLength()));
		hspElement.addAttribute("align-len", Integer.toString(hsp.getAlignLength()));

		hspElement.addElement("query").addText(hsp.getQuerySeq());
		hspElement.addElement("path").addText(hsp.getPathSeq());
		hspElement.addElement("target").addText(hsp.getTargetSeq());

		return hspElement;
	}

	public static String doubleToString(double value) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(SIMPLE_DOUBLE_FORMAT, value);
		return sb.toString();
	}

	public static String doubleToScientificString(double value) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(SCIENTIFIC_DOUBLE_FORMAT, value);
		return sb.toString();
	}

}
