<%@page import="bio.pih.SOIS"%>
<%@page import="org.dom4j.Document"%>
<%@page import="org.dom4j.io.OutputFormat"%>
<%@page import="org.dom4j.io.XMLWriter"%>
<%@page import="bio.pih.io.Output"%>
<%@page import="javax.xml.transform.TransformerFactory"%>
<%@page import="javax.xml.transform.Transformer"%>
<%@page import="javax.xml.transform.stream.StreamSource"%>
<%@page import="org.dom4j.io.DocumentResult"%>
<%@page import="org.dom4j.io.DocumentSource"%>
<%@page import="javax.xml.transform.TransformerConfigurationException"%>
<%@page import="javax.xml.transform.TransformerException"%>
<%@page import="java.io.ByteArrayOutputStream"%>

<%
if (request.getParameter("query") != null) {
  SOIS sois = SOIS.getInstance();
  String query = request.getParameter("query");
  
  if (query == null) {
  	out.print("No query? :-(");
  	return;
  }
  
  query = query.trim();
  
  if (query.length() == 0) {
   out.print("No query? :-(");
   return;
  }
  
  if (query.length() < 20) {
   out.print("Query too short, should be at least 20 bases");
   return;
  }
  
  if (query.length() > 2000) {
   out.print("Query too long, should be until 2000 bases, someday it will be solved :-)");
   return;
  }
  
  String dnaSequence = "[ACTGactg]*";
  if (!query.matches(dnaSequence)) {
   out.print("It doesnt look a DNA sequence!");
   return;
  }
  long begin = System.currentTimeMillis();
  long code = sois.doSearch(query, "RefSeq");
  
  while (sois.checkStatus(code) != true) {
    Thread.yield();
  }
  long total = System.currentTimeMillis() - begin;

out.print("Searching time: " + (total) + " milli seconds.");

Document document = Output.genoogleOutputToXML(sois.getResult(code));

ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

try {
	TransformerFactory factory = TransformerFactory.newInstance();
	Transformer transformer = factory
		.newTransformer(new StreamSource("webapps/genoogle/results.xsl"));
	DocumentSource source = new DocumentSource(document);
	DocumentResult result = new DocumentResult();
	transformer.transform(source, result);
	Document resultDocument = result.getDocument();
	OutputFormat outformat = OutputFormat.createPrettyPrint();
	outformat.setEncoding("UTF-8");
	XMLWriter writer = new XMLWriter(outputStream, outformat);
	writer.write(resultDocument);
	out.print(outputStream);
} catch (TransformerConfigurationException e) {
	e.printStackTrace();
	out.print(e);
} catch (TransformerException e) {
	e.printStackTrace();
	out.print(e);
}

}
%>
