<%@page import="bio.pih.SOIS"%>

<%
if (request.getParameter("query") != null) {
  out.print(request.getParameter("query"));
}
%>