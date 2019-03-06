<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>
<xsl:template match="/">
<xml>
	<item>
		<name><xsl:value-of select="item/title"/></name>
		<description><xsl:value-of select="item/description"/></description>
	</item>
</xml>
</xsl:template>
</xsl:stylesheet>
