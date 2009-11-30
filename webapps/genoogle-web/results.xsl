<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<xsl:template match="/">
  <html>
  <head>
    <title>
 	Genoogle
    </title>
  </head>
  <body>
	<xsl:apply-templates/>
	<hr/>
	Genoogle by Felipe Albrecht
  </body>
  </html>
</xsl:template>

<xsl:template match="genoogle/results/params">
	<h2><xsl:value-of select="@query"/></h2>
	<div id="parameters">
		<h3>Parameters:</h3>
		Databank: <xsl:value-of select="@databank"/>
		MinSubSequencesSimilarity: <xsl:value-of select="@minSubSequenceSimilarity"/>
		MaxSubSequencesDistance: <xsl:value-of select="@maxSubSequencesDistance"/>
		Min E-Value: <xsl:value-of select="@minEvalue"/>
	</div>
</xsl:template>

<xsl:template match="hits/hit">
	<p class="hit" style="border:thin dotted">
	id: <b><xsl:value-of select="@id"/></b>
	Gi: <xsl:value-of select="@gi"/>
	description: <b><xsl:value-of select="@description"/></b>
	length: <xsl:value-of select="@length"/>	 
	 <a><xsl:attribute name="href">http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nuccore&amp;id=<xsl:value-of select="@gi"/></xsl:attribute>More info</a>
	 
	<xsl:apply-templates/>
	</p>
</xsl:template>

<xsl:template match="hsps/hsp">
	<p class="hsp">
	  Score: <xsl:value-of select="@score"/>
	  Normalized Score: <xsl:value-of select="@normalized-score"/>
	  E-Value: <xsl:value-of select="@e-value"/>
	  Query from:<xsl:value-of select="@query-from"/>
	  Query to:<xsl:value-of select="@query-to"/>
	  Hit from:<xsl:value-of select="@hit-from"/>
	  Hit to:<xsl:value-of select="@hit-to"/>
 	  Identity lenght:<xsl:value-of select="@identity-len"/>
	  Align lenght:<xsl:value-of select="@align-len"/>
	  <p>
	    <pre>
		  <xsl:value-of select="query"/><br/>
		  <xsl:value-of select="path"/><br/>
		  <xsl:value-of select="target"/>
	    </pre>
	  </p>
	</p>
</xsl:template>


</xsl:stylesheet>
