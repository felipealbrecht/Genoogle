
<%@page import="java.lang.management.ThreadInfo"%>
<%@page import="java.lang.management.ManagementFactory"%>
<%@page import="java.lang.management.ThreadMXBean"%><%@page import="bio.pih.SOIS"%>
<%@page import="org.dom4j.Document"%>
<%@page import="org.dom4j.io.OutputFormat"%>
<%@page import="org.dom4j.io.XMLWriter"%>
<%@page import="bio.pih.io.Output"%>
<%@page import="bio.pih.search.results.SearchResults"%>
<%@page import="javax.xml.transform.TransformerFactory"%>
<%@page import="javax.xml.transform.Transformer"%>
<%@page import="javax.xml.transform.stream.StreamSource"%>
<%@page import="org.dom4j.io.DocumentResult"%>
<%@page import="org.dom4j.io.DocumentSource"%>
<%@page import="javax.xml.transform.TransformerConfigurationException"%>
<%@page import="javax.xml.transform.TransformerException"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="org.apache.log4j.Logger"%>

<%
	final Logger logger = Logger.getLogger("bio.pih.web.Query.jsp");

ThreadMXBean threads = ManagementFactory.getThreadMXBean();
ThreadInfo[] infos = threads.getThreadInfo( threads.getAllThreadIds() );
for (ThreadInfo info: infos) {
	out.print(info.getThreadName() + " " + threads.getThreadCpuTime(info.getThreadId()) + " <BR> ");
}

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
		SearchResults sr = sois.doSyncSearch(query, "RefSeq");
		long total = System.currentTimeMillis() - begin;

		if (sr.hasFail()) {			
			out.println("<body><title>Epic Fail</title>");
			out.println("<center>");
			out.println("<h1>Genoogle Fail!</h1>");
			out.println("<img src=\"fail.jpg\"/>");
			out.println("<code>");
			out.println("<br>");
			for (Exception e : sr.getFails()) {
				logger.fatal("Fail while doing searching process", e);
				out.println(e);
			}
			out.println("<br>Please, inform Felipe Albrecht at felipe.albrecht@gmail.com about this error, Because shit happens...</br>");
			out.println("</center>");
			out.println("</code>");
			out.println("</body>");
			
		} else {
			out.print("Searching time: " + (total) + " milli seconds.");
			Document document = Output.genoogleOutputToXML(sr);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			long tBegin = System.currentTimeMillis();
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(new StreamSource(
						"webapps/genoogle/results.xsl"));
				DocumentSource source = new DocumentSource(document);
				DocumentResult result = new DocumentResult();
				transformer.transform(source, result);
				System.out.println((System.currentTimeMillis() - tBegin) + " para transformacao");
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

	}
%>
