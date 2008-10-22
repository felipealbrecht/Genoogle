package bio.pih.io;

import java.util.Formatter;
import java.util.List;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import bio.pih.search.SearchParams;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

/**
 * 
 * Visitator class that create a XML with the results.
 * 
 * @author albrecht
 */
public class Output {

	private static String copyRightNotice = "Genoogle by Albrecht, Justel and Pinto. 2008.";
	private static String SIMPLE_DOUBLE_FORMAT = "%10.4f";
	private static String SCIENTIFIC_DOUBLE_FORMAT = "%10.4e";
	
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

		Element output = doc.addElement("genoogle").addAttribute("version", "1");
		output.addElement("references").addAttribute("program", "SOIS - Search Over Indexed Sequences").addAttribute("version", "0.01").addAttribute("authors", copyRightNotice);
		Element iterationsElement = output.addElement("iterations");

		for (int i = 0; i < searchResults.size(); i++) {
			Element iterationElement = iterationsElement.addElement("iteration").addAttribute("number", String.valueOf(i));
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
		DocumentFactory factory = DocumentFactory.getInstance();

		Document doc = factory.createDocument();
		doc.setName("genoogle");

		Element output = doc.addElement("genoogle");
		output.addElement("references").addAttribute("program", "SOIS - Search Over Indexed Sequences").addAttribute("version", "0.01").addAttribute("authors", copyRightNotice);
		output.add(searchResultToXML(searchResult));
		
		return doc;
	}

	/**
	 * @param searchResult
	 * @return {@link Element} containing the {@link SearchResults} at XML form.
	 */
	public static Element searchResultToXML(SearchResults searchResult) {
		assert searchResult != null;
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
		paramsElement.addAttribute("minSubSequenceSimilarity", Integer.toString(params.getMinSimilarity()));
		paramsElement.addAttribute("maxDatabankSubSequencesDistance", Integer.toString(params.getMaxDatabankSequenceSubSequencesDistance()));
		paramsElement.addAttribute("minMatchAreaLength", Integer.toString(params.getMinMatchAreaLength()));
		paramsElement.addAttribute("maxQuerySubSequencesDistance", Integer.toString(params.getMaxQuerySequenceSubSequencesDistance()));
		paramsElement.addAttribute("minQuerySubSequence", Integer.toString(params.getMinQuerySequenceSubSequence()));

		return paramsElement;
	}

	/**
	 * @param hits
	 * @return {@link Element} containing the {@link List} of  {@link Hit} at XML form.
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
		hitElement.addAttribute("num", Integer.toString(hit.getHitNum()));
		hitElement.addAttribute("id", hit.getId());
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

		Element hspElement = factory.createElement("hsp").addAttribute("xml:space","preserve");;
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
	
	private static String doubleToString(double value) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(SIMPLE_DOUBLE_FORMAT, value);
		return sb.toString();
	}
	
	private static String doubleToScientificString(double value) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(SCIENTIFIC_DOUBLE_FORMAT, value);
		return sb.toString();
	}

}
