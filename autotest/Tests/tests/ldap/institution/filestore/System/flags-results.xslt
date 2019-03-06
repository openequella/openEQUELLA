<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" omit-xml-declaration="yes" />
	<xsl:template match="/results">
		<searchresults>
			<summary>
				<copyright>Copyright notice will appear here</copyright>
				<source>The Learning Edge</source>
				<token><xsl:value-of select="token" /></token>
				<count><xsl:value-of select="@count" /></count>
				<found><xsl:value-of select="available" /></found>
			</summary>
			<xsl:for-each select="result">
				<xsl:variable name="item" select="xml/item" />
				<item>
					<title><xsl:value-of select="$item/name" /></title>
					<link><xsl:value-of select="./@url" /></link>
					<relevance>0.9</relevance>
					<description><xsl:value-of select="$item/description" /></description>
					<view><xsl:value-of select="./@url" /></view>
					<xsl:if test="count($item/itembody/packagefile) &gt; 0">
						<download><xsl:value-of select="./@url" />downloadzip.jsp?zip.scorm=true</download>
					</xsl:if>
					<identifier><xsl:value-of select="./@id" /></identifier>
					<rights>Rights will appear here</rights>
					<metadatascheme>lom</metadatascheme>
					<contentpackage>ims</contentpackage>
				</item>
			</xsl:for-each>
		</searchresults>
	</xsl:template>
</xsl:stylesheet>
