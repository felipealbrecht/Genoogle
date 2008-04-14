package bio.pih.io;

import java.util.List;

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

	public static Document genoogleOutputToXML(List<SearchResults> searchResults) {
		assert searchResults != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Document doc = factory.createDocument();
		doc.setName("Genoogle Output");

		Element output = doc.addElement("GenoogleOutput");
		output.addElement("references").addAttribute("program", "SOIS - Search Over Indexed Sequences").addAttribute("version", "0.01").addAttribute("authors", copyRightNotice);
		Element iterationsElement = output.addElement("iterations");
		
		for (int i = 0; i < searchResults.size(); i++) {
			Element iterationElement = iterationsElement.addElement("iteration").addAttribute("number", String.valueOf(i));
			iterationElement.add(searchResultToXML(searchResults.get(i)));
		}

		return doc;
	}

	public static Element searchResultToXML(SearchResults searchResult) {
		assert searchResult != null;
		DocumentFactory factory = DocumentFactory.getInstance();
		Element resultsElement = factory.createElement("results");
		resultsElement.add(paramsToXML(searchResult.getParams()));
		resultsElement.add(hitsToXML(searchResult.getHits()));

		return resultsElement;
	}

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

	public static Element hitsToXML(List<Hit> hits) {
		assert hits != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hitsElement = factory.createElement("hits");
		for (Hit hit : hits) {
			hitsElement.add(hitToXML(hit));
		}

		return hitsElement;
	}

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

	public static Element hspsToXML(List<HSP> hsps) {
		assert hsps != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hspsElement = factory.createElement("hsps");
		for (HSP hsp : hsps) {
			hspsElement.add(hspToXML(hsp));
		}

		return hspsElement;
	}

	public static Element hspToXML(HSP hsp) {
		assert hsp != null;
		DocumentFactory factory = DocumentFactory.getInstance();

		Element hspElement = factory.createElement("hsp");
		hspElement.addAttribute("score", Double.toString(hsp.getScore()));
		hspElement.addAttribute("query-from", Integer.toString(hsp.getQueryFrom()));
		hspElement.addAttribute("query-to", Integer.toString(hsp.getQueryTo()));
		hspElement.addAttribute("hit-from", Integer.toString(hsp.getHitFrom()));
		hspElement.addAttribute("hit-to", Integer.toString(hsp.getHitTo()));
		hspElement.addAttribute("identity-len", Integer.toString(hsp.getIdentityLength()));
		hspElement.addAttribute("align-len", Integer.toString(hsp.getAlignLength()));
		hspElement.addAttribute("qseq", hsp.getQuerySeq());
		hspElement.addAttribute("hseq", hsp.getTargetSeq());
		hspElement.addAttribute("midline", hsp.getPathSeq());

		return hspElement;
	}

}
