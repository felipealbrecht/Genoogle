<%@page import="bio.pih.Genoogle"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">  <head>
    <title><%=Genoogle.SOFTWARE_NAME%> - <%=Genoogle.VERSION %></title>
</head>

<body>
 <center>
 <img src="genoogle-white.png"/>
 
<p>The quantity of genetics sequences available is growing, soon will be possible to sequence the human coding regions for one thousand dollars, so,  we have to organize all these informations!</p>
<p>Genoogle is the Ms.C. project of the <a href="http://www.pih.bio.br">Felipe Albrecht</a> which aims to create a fast and scalable search engine for bio-molecular sequences. </p>
 
 <form action="query.jsp" method="get">
   <input type="text" size="80" name="query">
   <input type="submit" value="Search sequence">
 </form>
 
 <br>
 
   <font size="-1">
 Input example: <a href="query.jsp?query=GGTTATATAGGAATTCACAACGAAATCAGATGGCTCCTAATTGTGTATGCAGTATTGATAACATGGACCTTTGCTGTTCA">GGTTATATAGGAATTCACAACGAAATCAGATGGCTCCTAATTGTGTATGCAGTATTGATAACATGGACCTTTGCTGTTCA</a> <br>
 Input example: <a href="query.jsp?query=ATGGACCCGGTCACAGAGCCTGTAAAGCGCAGGCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGGGTGA">ATGGACCCGGTCACAGAGCCTGTAAAGCGCAGGCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGGGTGA</a> <br>
 Bigger example: <a href="query.jsp?query=AAACCCACAGAAGCTAAAACCAAAGAAATGTCTAATGACATCATCTTCTAATCCCAGTATCTAAAATGTACTTAATAAAGGTTGGTGAGTAAGAAGCCCTGACACACTATAAATTTCCTTGGACATAAAAACTGTTGCTGTTTTTCTTAAAGCTGCCCCCTCTTCTTCTTCCACTGTGTTAGCCAGGATGGTCTTGATCTCCTGACCTCGTGATCCACCCGCCTCGGCCTCCCAAAGTGCTGGGATTACAGGCGTGAGCCACCACGCCCGGCCCGCTCCCTCTTCTTCTTGACCTAGGACTAGAACTAGCTTTGAAGAGAAAGAAAGACACATTCTAGTCT">AAACCCACAGAAGCTAAAACCAAAGAAATGTCTAATGACATCATCTTCTAATCCCAGTATCTAAAATGTACTTAATAAAGGTTGGTGAGTAAGAAGCCCTGACACACTATAAATTTCCTTGGACATAAAAACTGTTGCTGTTTTTCTTAAAGCTGCCCCCTCTTCTTCTTCCACTGTGTTAGCCAGGATGGTCTTGATCTCCTGACCTCGTGATCCACCCGCCTCGGCCTCCCAAAGTGCTGGGATTACAGGCGTGAGCCACCACGCCCGGCCCGCTCCCTCTTCTTCTTGACCTAGGACTAGAACTAGCTTTGAAGAGAAAGAAAGACACATTCTAGTCT</a> <br>


 Try to insert, remove, change some bases.
  </font>
 </center>
 


<div style="position: absolute; bottom: 20px;">
<hr style="width: 100%;"> 
 <font size=-1>
 <p>Genoogle do <b>not</b> have any affiliation with Google. I just think 'genoogle' is a cool name and better than 'Genhooo' or 'Genuil' or 'Gensn' :-)</p>
 <p><%=Genoogle.SOFTWARE_NAME%> - <%=Genoogle.VERSION%> - <%=Genoogle.COPYRIGHT_NOTICE%> - <a href="<%=Genoogle.WEB_PAGE%>"><%=Genoogle.WEB_PAGE%></a></p>
  </font>
</div> 
  
  <script type="text/javascript">
     var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
     document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
 </script>
 <script type="text/javascript">
   var pageTracker = _gat._getTracker("UA-373397-4");
   pageTracker._trackPageview();
 </script>

</body>
</html>
