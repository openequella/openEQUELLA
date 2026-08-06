<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:dc="http://purl.org/dc/elements/1.1/" 
xmlns:srw_dc="info:srw/schema/1/dc-schema" >
<xsl:output method="xml" />
<xsl:template match="/srw_dc:dc">
<xml>
	<item>		
		<name><xsl:value-of select="dc:title"/></name>
		<description><xsl:value-of select="dc:description"/></description>
	</item>
</xml>	
</xsl:template>
</xsl:stylesheet>
