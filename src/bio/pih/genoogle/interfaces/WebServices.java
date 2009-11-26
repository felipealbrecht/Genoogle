package bio.pih.genoogle.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.Output;
import bio.pih.genoogle.search.SearchParams;
import bio.pih.genoogle.search.SearchParams.Parameter;
import bio.pih.genoogle.search.results.SearchResults;

@WebService(targetNamespace="http://genoogle.pih.bio.br")
public class WebServices {
	static Logger logger = Logger.getLogger(WebServices.class.getName());

	private static Genoogle genoogle = Genoogle.getInstance();

	@Resource
	private WebServiceContext wsContext;

	@WebMethod(exclude = true)
	
	@Resource
	public void initializeContext(WebServiceContext wsContext) {
		System.out.println("Setting WebServiceContext");
		this.wsContext = wsContext;
	}
	
	@WebMethod(operationName = "version")
	public double version() {
		return Genoogle.VERSION;
	}

	@WebMethod(operationName = "list")
	public String list() {
		Element genoogleXmlHeader = Output.genoogleXmlHeader();

		Collection<AbstractSequenceDataBank> databanks = genoogle.getDatabanks();
		Element element = genoogleXmlHeader.addElement("databanks");
		for (AbstractSequenceDataBank databank : databanks) {
			element.addElement(databank.getName());
		}

		return xmlToString(genoogleXmlHeader.getDocument());
	}

	@WebMethod(operationName = "parameters")
	public String parameters() {
		Element genoogleXmlHeader = Output.genoogleXmlHeader();

		Map<Parameter, Object> defaultParameters = SearchParams.getSearchParamsMap();
		Element element = genoogleXmlHeader.addElement("parameters");
		for (Entry<Parameter, Object> entry : defaultParameters.entrySet()) {
			element.addElement(entry.getKey().getName()).addAttribute("value", entry.getValue().toString());
		}

		return xmlToString(genoogleXmlHeader.getDocument());
	}

	@WebMethod(operationName = "setParameter")
	public boolean setParameter(@WebParam(name = "parameter") String parameter,
			@WebParam(name = "value") String paramValue) {
		MessageContext mc = wsContext.getMessageContext();
		HttpSession session = ((javax.servlet.http.HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST)).getSession();
		if (session == null) {
			throw new WebServiceException("No session in WebServiceContext");
		}

		Map<Parameter, Object> parameters = (Map<Parameter, Object>) session.getAttribute("parameters");
		if (parameters == null) {
			parameters = SearchParams.getSearchParamsMap();
			mc.put("parameters", parameters);
		}

		Parameter p = Parameter.getParameterByName(parameter);
		if (p == null) {
			return false;
		}

		Object value = p.convertValue(paramValue);
		parameters.put(p, value);

		return true;
	}

	@WebMethod(operationName = "search")
	public String search(@WebParam(name = "query") String query, @WebParam(name = "databank") String databank) {
		MessageContext mc = wsContext.getMessageContext();
		HttpSession session = ((javax.servlet.http.HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST)).getSession();
		if (session == null) {
			throw new WebServiceException("No session in WebServiceContext");
		}

		Map<Parameter, Object> parameters = (Map<Parameter, Object>) session.getAttribute("parameters");
		if (parameters == null) {
			parameters = SearchParams.getSearchParamsMap();
		}
				
		SearchResults sr = genoogle.doSyncSearch(query, databank, parameters);
		Document doc = Output.genoogleOutputToXML(sr);
		return xmlToString(doc);
	}

	private String xmlToString(Document doc) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setTrimText(false);
		XMLWriter writer = null;

		try {
			writer = new XMLWriter(outputStream, outformat);
		} catch (UnsupportedEncodingException e) {
			logger.fatal(e);
			return e.getLocalizedMessage();
		}

		try {
			writer.write(doc);
		} catch (IOException e) {
			logger.fatal(e);
			return e.getLocalizedMessage();
		}

		try {
			return outputStream.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.fatal(e);
			return e.getLocalizedMessage();
		}
	}

}
