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
		Genoogle sois = Genoogle.getInstance();
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

		if (query.length() > 40000) {
			out.print("Query too long, should be until 40000 bases, someday it will be solved :-)");
			return;
		}

		String dnaSequence = "[ACTGactg]*";
		if (!query.matches(dnaSequence)) {
			out.print("It doesnt look a DNA sequence!");
			return;
		}
		long begin = System.currentTimeMillis();
		SearchResults sr = sois.doSyncSearch(query);
		long total = System.currentTimeMillis() - begin;

		if (sr.hasFail()) {			
			out.println("<body><title>Epic Fail</title>");
			out.println("<center>");
			out.println("<h1>Genoogle Fail!</h1>");
			out.println("<img src=\"fail.jpg\"/>");
			out.println("<code>");
			out.println("<br>");
			for (Throwable e : sr.getFails()) {
				logger.fatal("Fail while doing searching process", e);
				out.println(e);
			}
			out.println("<br>Please, inform Felipe Albrecht at felipe.albrecht@gmail.com about this error, Because shit happens...</br>");
			out.println("</center>");
			out.println("</code>");
			out.println("</body>");
			
		} else {		
			response.setContentType("text/xml; charset=UTF-8");
			Document resultDocument = Output.genoogleOutputToXML(sr);
			resultDocument.getRootElement().addElement("infos").addAttribute("search-time", Long.toString(total));
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setTrimText(false);
			XMLWriter writer = new XMLWriter(outputStream, outformat);
			writer.write(resultDocument);
			out.print(outputStream);
		}
	}
%>