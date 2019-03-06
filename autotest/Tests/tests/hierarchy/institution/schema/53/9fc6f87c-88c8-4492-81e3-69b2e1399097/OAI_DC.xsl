<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/">


<xsl:template match ="xml">
<xml>
	<xsl:apply-templates select="item"/>
	<item>
		<lom>
			<general>
				<title><xsl:value-of select="item/oai/oai_dc:dc/dc:title"/></title>
				<description><xsl:value-of select="item/oai/oai_dc:dc/dc:description"/></description>
				<language><xsl:value-of select="item/oai/oai_dc:dc/dc:language"/></language>
			</general>
			<rights>
				<cost>No</cost>
				<copyrightAndOtherRestrictions>Yes</copyrightAndOtherRestrictions>
				<description><xsl:value-of select="item/oai/oai_dc:dc/dc:rights"/></description>
			</rights>
			<lifecycle>
				<contribute>
					<role>publisher</role>
					<entity><xsl:value-of select="item/oai/oai_dc:dc/dc:publisher"/></entity>
					<date><xsl:value-of select="item/oai/oai_dc:dc/dc:date"/></date>
				</contribute>
				<contribute>
					<role>author</role>
					<entity><xsl:value-of select="item/oai/oai_dc:dc/dc:creator"/></entity>
					<date><xsl:value-of select="item/oai/oai_dc:dc/dc:date"/></date>
				</contribute>
			</lifecycle>
			<technical>
				<xsl:for-each select="item/oai/oai_dc:dc/dc:format">
					<format><xsl:value-of select="."/></format>
				</xsl:for-each>
			</technical>
			<educational>
				<learning.resource.type>Primary Source Material</learning.resource.type>
			</educational>
		</lom>
	</item>
	
	
	
</xml>


</xsl:template>

<!-- copy the "item" node -->
<xsl:template match="item">
	<xsl:copy>
	<xsl:copy-of select="@*"/>
	<xsl:copy-of select="name"/>
	<xsl:copy-of select="description"/>
	<xsl:copy-of select="itembody"/>
	<xsl:apply-templates select="oai"/>
	<xsl:copy-of select="staging"/>
	<xsl:copy-of select="newitem"/>
	<xsl:copy-of select="owner"/>
	<xsl:copy-of select="datecreated"/>
	<xsl:copy-of select="datemodified"/>
	<xsl:copy-of select="dateforindex"/>
	<xsl:copy-of select="folder"/>
	<attachments>
		<attachment disabled="false" type="remote">
			<file><xsl:value-of select="oai/oai_dc:dc/dc:identifier"/></file> 
			<description>
				<xsl:value-of select="oai/oai_dc:dc/dc:title"/>
			</description>
		</attachment>
	</attachments>
	<xsl:copy-of select="attachments"/>
	<xsl:copy-of select="badurls"/>
	<xsl:copy-of select="history"/>
	<xsl:copy-of select="moderation"/>
	<xsl:copy-of select="navigationNodes"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="oai">
	<xsl:copy>
	<xsl:copy-of select="@*"/>
	</xsl:copy>
</xsl:template>


<!-- Search and replace function (not used) -->
<xsl:template name="search-and-replace">
	<xsl:param name="input"/>
	<xsl:param name="search-string"/>
	<xsl:param name="replace-string"/>
	<xsl:choose>
		<xsl:when test="$search-string and contains($input,$search-string)">
			<xsl:value-of select="substring-before($input,$search-string)"/>
			<xsl:value-of select="$replace-string"/>
			<xsl:call-template name="search-and-replace">
				<xsl:with-param name="input" select="substring-after($input,$search-string)"/> 
				<xsl:with-param name="search-string" select="$search-string"/>
				<xsl:with-param name="replace-string" select="$replace-string"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$input"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet> 

