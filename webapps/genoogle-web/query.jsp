<%@page import="org.dom4j.io.HTMLWriter"
%><%@page import="java.lang.management.ThreadInfo"
%><%@page import="java.lang.management.ManagementFactory"
%><%@page import="java.lang.management.ThreadMXBean"
%><%@page import="bio.pih.genoogle.Genoogle"
%><%@page import="org.dom4j.Document"
%><%@page import="org.dom4j.io.OutputFormat"
%><%@page import="org.dom4j.io.XMLWriter"
%><%@page import="bio.pih.genoogle.io.Output"
%><%@page import="bio.pih.genoogle.search.results.SearchResults"
%><%@page import="bio.pih.genoogle.interfaces.webservices.WebServicesClient"
%><%@page import="javax.xml.transform.TransformerFactory"
%><%@page import="javax.xml.transform.Transformer"
%><%@page import="javax.xml.transform.stream.StreamSource"
%><%@page import="org.dom4j.io.DocumentResult"
%><%@page import="org.dom4j.io.DocumentSource"
%><%@page import="javax.xml.transform.TransformerConfigurationException"
%><%@page import="javax.xml.transform.TransformerException"
%><%@page import="java.io.ByteArrayOutputStream"
%><%@page import="org.apache.log4j.Logger"
%><%
	final Logger logger = Logger.getLogger("bio.pih.web.Query.jsp");

	if (request.getParameter("query") != null) {
		WebServicesClient client = WebServicesClient.getInstance();
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

		if (query.length() < 18) {
			out.print("Query too short, should be at least 18 bases");
			return;
		}

		String dnaSequence = "[ACTGactg]*";
		if (!query.matches(dnaSequence)) {
			out.print("It doesnt look a DNA sequence!");
			return;
		}
		long begin = System.currentTimeMillis();
		String result = client.doSearch(query, "AS");
		long total = System.currentTimeMillis() - begin;		
		response.setContentType("text/xml; charset=UTF-8");
		out.print(result);
		out.print("<!-- TOTAL TIME: "+total+" -->");
		
	}
%>
