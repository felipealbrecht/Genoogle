package bio.pih.genoogle.interfaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.io.Output;
import bio.pih.genoogle.search.results.SearchResults;

@WebService
public class WebServices {
	
	static Logger logger = Logger.getLogger(WebServices.class.getName());
	
	private static Genoogle genoogle = Genoogle.getInstance();

    @WebMethod(operationName = "search")
    public String search(@WebParam(name = "query") String query, @WebParam(name = "databank") String databank) {        
        SearchResults sr = genoogle.doSyncSearch(query);
        Document doc = Output.genoogleOutputToXML(sr);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setTrimText(false);
		XMLWriter writer = null;
		
		String ret = "";
		
		try {
			writer = new XMLWriter(outputStream, outformat);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		try {
			writer.write(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		try {
			ret = outputStream.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return ret;
    }
	
}
