<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" media-type="text/html"/>

<!-- http://www.lib.monash.edu.au/tutorials/citing/harvard.html -->
<!-- Kizza, JM 2002, Computer network security and cyberethics, McFarland, Jefferson, N.C.-->
<!-- BOOK: Author(s) of book - surname and initials Year of publication, Title of book -  italicised, Edition, Publisher, Place of publication. -->
<!-- BOOK CHAPTER: Author(s) of chapter - surname and initials Year of publication, 'Title of chapter - in single quotation marks' [in] Author of book (if different), Title of book - italicised, Edition, Publisher, Place of publication, (optional) page numbers. -->
<!-- JOURNAL: Author(s) of article -surname and initials Year of publication, 'Title of article - in single quotation marks', Journal name - italicised, volume number, issue number, page number(s). -->
    
    <xsl:template match="/">
	  <xsl:apply-templates select="/xml/item/copyright">
	  	<xsl:with-param name="section" select="/xml/item/copyright/portions/portion/sections/section[attachment = /xml/request/attachment]"/>
	  </xsl:apply-templates>
    </xsl:template>
	
	 <xsl:template match="/xml/item/copyright">
		<xsl:param name="section"/>
		<xsl:variable name="portion" select="$section/../.."/>
		
		<xsl:for-each select="authors/author">
			<xsl:if test="position() > 1 and position() ">
				<xsl:text>, </xsl:text>
			</xsl:if>
			<xsl:value-of select="."/>
		</xsl:for-each>
		
		<xsl:for-each select="publication/year">
			<xsl:text> </xsl:text>
			<xsl:value-of select="."/>
		</xsl:for-each>
		
		<xsl:for-each select="$portion/title">
			<xsl:text>, '</xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>' in</xsl:text>
		</xsl:for-each>
		
		<xsl:for-each select="title">
			<xsl:text>, </xsl:text>
			<i>
				<xsl:value-of select="."/>
			</i>
		</xsl:for-each>
		
		<xsl:for-each select="publisher">
			<xsl:text>, </xsl:text>
			<xsl:value-of select="."/>
		</xsl:for-each>
		<!--
		<xsl:for-each select="$section/pages">
			<xsl:text>, </xsl:text>
			<xsl:value-of select="."/>
		</xsl:for-each>
		-->
    </xsl:template>
</xsl:stylesheet>