<%@page import="bio.pih.genoogle.interfaces.webservices.WebServices"
%><%@page import="bio.pih.genoogle.interfaces.webservices.WebServicesService"
%><%@page import="javax.xml.ws.BindingProvider"
%><%@page import="org.apache.log4j.Logger"
%><%
	final Logger logger = Logger.getLogger("bio.pih.web.Query.jsp");

	WebServices proxy;
	
	if (session.getAttribute("proxy") == null) {
		WebServicesService webServicesService = new WebServicesService();
		proxy = webServicesService.getWebServicesPort();		
			((BindingProvider) proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
		session.setAttribute("proxy", proxy);
	} else {
		proxy = (WebServices) session.getAttribute("proxy");
	}
	
	

	if (request.getParameter("query") != null) {
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
		String result = proxy.search(query, "AS");
		long total = System.currentTimeMillis() - begin;		
		response.setContentType("text/xml; charset=UTF-8");
		out.print(result);
		out.print("<!-- TOTAL TIME: "+total+" -->");
		
	}
%>
