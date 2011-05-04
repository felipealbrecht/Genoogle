/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

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
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.DuplicateDatabankException;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.io.Output;
import bio.pih.genoogle.io.SplittedDatabankCollection;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.search.SearchParams;
import bio.pih.genoogle.search.SearchParams.Parameter;
import bio.pih.genoogle.search.results.SearchResults;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@WebService(targetNamespace = "http://webservices.interfaces.genoogle.pih.bio")
public class WebServices {
	private static Logger logger = Logger.getLogger(WebServices.class.getName());
	private static Genoogle genoogle = Genoogle.getInstance();

	@Resource
	private WebServiceContext wsContext;

	@WebMethod(exclude = true)
	@Resource
	public void initializeContext(WebServiceContext wsContext) {
		System.out.println("Setting WebServiceContext for the Genoogle Web Services.");
		this.wsContext = wsContext;
	}

	@WebMethod(operationName = "name")
	public String name() {
		return Genoogle.SOFTWARE_NAME;
	}

	@WebMethod(operationName = "version")
	public Double version() {
		return Genoogle.VERSION;
	}

	@WebMethod(operationName = "databanks")
	public List<String> databanks() {
		List<String> databanksList = Lists.newLinkedList();

		Collection<AbstractSequenceDataBank> databanks = genoogle.getDatabanks();
		for (AbstractSequenceDataBank databank : databanks) {
			databanksList.add(databank.getName());
		}

		return databanksList;
	}

	@SuppressWarnings("unchecked")
	@WebMethod(operationName = "parameters")
	public List<String> parameters() {
		MessageContext mc = wsContext.getMessageContext();
		HttpSession session = ((javax.servlet.http.HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST)).getSession();
		if (session == null) {
			throw new WebServiceException("No session in WebServiceContext");
		}

		Map<Parameter, Object> parameters = (Map<Parameter, Object>) session.getAttribute("parameters");
		if (parameters == null) {
			parameters = SearchParams.getSearchParamsMap();
		}

		List<String> parametersList = Lists.newLinkedList();
		for (Entry<Parameter, Object> entry : parameters.entrySet()) {
			parametersList.add(entry.getKey().toString() + "=" + entry.getValue().toString());
		}

		return parametersList;
	}

	@SuppressWarnings("unchecked")
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
		}

		Parameter p = Parameter.getParameterByName(parameter);
		if (p == null) {
			return false;
		}

		Object value = p.convertValue(paramValue);
		parameters.put(p, value);
		session.setAttribute("parameters", parameters);
		return true;
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	@WebMethod(operationName = "searchWithParameters")
	public String searchWithParameters(@WebParam(name = "query") String query,
			@WebParam(name = "databank") String databank, @WebParam(name = "parametersList") List<String> parametersList) {
		MessageContext mc = wsContext.getMessageContext();
		HttpSession session = ((javax.servlet.http.HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST)).getSession();
		if (session == null) {
			throw new WebServiceException("No session in WebServiceContext");
		}

		Map<Parameter, Object> sessionParameters = (Map<Parameter, Object>) session.getAttribute("parameters");
		Map<Parameter, Object> parameters = Maps.newHashMap();
		if (sessionParameters != null) {
			for (Entry<Parameter, Object> e : sessionParameters.entrySet()) {
				parameters.put(e.getKey(), e.getValue());
			}
		}

		for (String param : parametersList) {
			String[] p = param.split("=");
			if (p.length != 2) {
				throw new InvalidParameterException(param + " is invalid.");
			}
			Parameter parameterByName = Parameter.getParameterByName(p[0]);
			if (parameterByName == null) {
				throw new InvalidParameterException(p[0] + " is not a parameter name.");
			}

			// TODO: to verify, protect if the value is not possible.
			Object convertValue = parameterByName.convertValue(p[1]);
			parameters.put(parameterByName, convertValue);
		}

		SearchResults sr = genoogle.doSyncSearch(query, databank, parameters);
		Document doc = Output.genoogleOutputToXML(sr);
		return xmlToString(doc);
	}

	@WebMethod
	public boolean createDatabank(@WebParam(name = "name") String name,
			@WebParam(name = "fastaFiles") List<String> fastaFiles,
			@WebParam(name = "subSequenceLength") int subSequenceLength, @WebParam(name = "mask") String mask,
			@WebParam(name = "numberOfSubDatabanks") int numberOfSubDatabanks,
			@WebParam(name = "lowComplexityFilter") int lowComplexityFilter) {

		// TODO: check if it as at least one element
		File pwd = new File(fastaFiles.get(0)).getParentFile();

		// TODO: get alphabet.
		SplittedDatabankCollection splittedSequenceDatabank = new SplittedDatabankCollection(name, DNAAlphabet.SINGLETON, pwd, subSequenceLength, numberOfSubDatabanks, mask);

		for (String f : fastaFiles) {
			File fastaFile = new File(f);

			// TODO: do not use IndexedSequenceDataBank to store temporary fasta file informations.
			IndexedSequenceDataBank databank = new IndexedSequenceDataBank(fastaFile.getName(), DNAAlphabet.SINGLETON, subSequenceLength, mask, new File(fastaFile.getName()),
					splittedSequenceDatabank);

			try {
				splittedSequenceDatabank.addDatabank(databank);
			} catch (DuplicateDatabankException e) {
				// TODO Auto-generated catch block				
				e.printStackTrace();
			}
		}
		
		try {
			splittedSequenceDatabank.encodeSequences(false);
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValueOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IndexConstructionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
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
