<%@page import="bio.pih.genoogle.interfaces.webservices.WebServices"
%><%@page import="bio.pih.genoogle.interfaces.webservices.WebServicesService"
%><%@page import="javax.xml.ws.BindingProvider"
%><%@page import="org.apache.log4j.Logger"
%><%@page import="java.util.List"
%><%
/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

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


        List<String> databanks =  proxy.databanks();

        // TOODO: Be possivle to choice the database, for while, the first one is used.
        if (databanks.size() == 0) {
                 out.println("No databases available, please, ask for the administrator format some one");
                 return;
        }
        String databankName = databanks.get(0);


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
		String result = proxy.search(query, databankName);
		long total = System.currentTimeMillis() - begin;
		response.setContentType("text/xml; charset=UTF-8");
		out.print(result);
		out.print("<!-- TOTAL TIME: "+total+" -->");

	}
%>
