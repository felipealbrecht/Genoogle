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
  long code = sois.doSearch(query, "RefSeq");
  
  while (sois.checkStatus(code) != true) {
    Thread.yield();
}

Document document = Output.genoogleOutputToXML(sois.getResult(code));

ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


 
try {
	TransformerFactory factory = TransformerFactory.newInstance();
	Transformer transformer = factory
		.newTransformer(new StreamSource("webapps/ROOT/results.xsl"));
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
} catch (TransformerException e) {
	e.printStackTrace();
}

}
%>