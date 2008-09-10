<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <head><!--
    <style type="text/css">
      .hidden {visibility: hidden;}
      .unhidden {visibility: unhidden;}
    </style> 

    <script type="text/javascript">
     function unhide(divID) {
       var item = document.getElementById(divID);
       if (item) {
         item.className=(item.className=='hidden')?'unhidden':'hidden';
	item.style="visibility: hidden;";
       }
      }
    </script>
	-->
    <title>
 	Genoogle
    </title>
  </head>
  <body>
<!--	<a href="javascript:unhide('parameters');">Show parameters</a>  -->
	<xsl:apply-templates/>
	<hr/>
	Genoogle by Felipe Albrecht
  </body>
  </html>
</xsl:template>

<xsl:template match="genoogle/results/params">
	<h2><xsl:value-of select="@query"/></h2>
	<div id="parameters" class=".hidden">
		<h3>Parameters:</h3>
		Databank: <xsl:value-of select="@databank"/>
		MinSubSequencesSimilarity: <xsl:value-of select="@minSubSequenceSimilarity"/>
		MaxDatababkSubSequencesDistance: <xsl:value-of select="@maxDatabankSubSequencesDistance"/>
		MinMatchAreaLength: <xsl:value-of select="@minMatchAreaLength"/>
		MaxQuerySubSequencesDistance: <xsl:value-of select="@maxQuerySubSequencesDistance"/>
		MinQuerySubSequence: <xsl:value-of select="@minQuerySubSequence"/>
	</div>
</xsl:template>

<xsl:template match="hits/hit">
	<p class="hit" style="border:thin dotted">
	id: <b><xsl:value-of select="@id"/></b>
	description: <b><xsl:value-of select="@description"/></b><br/>
	 <b><xsl:value-of select="@accession"/></b>
	 (<xsl:value-of select="@length"/>)
	<xsl:apply-templates/>
	</p>
</xsl:template>

<xsl:template match="hsps/hsp">
	<p class="hsp">
	Score: <xsl:value-of select="@score"/>
	Query from:<xsl:value-of select="@query-from"/>
	Query to:<xsl:value-of select="@query-to"/>
	Hit from:<xsl:value-of select="@hit-from"/>
	Hit to:<xsl:value-of select="@hit-to"/>
 	Identity lenght:<xsl:value-of select="@identity-len"/>
	Align lenght:<xsl:value-of select="@align-len"/>
	<p class="alignment" style="font-family:monospace">
		<xsl:value-of select="@qseq"/><br/>
		<xsl:value-of select="@midline"/><br/>
		<xsl:value-of select="@hseq"/><br/>

	</p>
	</p>
</xsl:template>


</xsl:stylesheet>
