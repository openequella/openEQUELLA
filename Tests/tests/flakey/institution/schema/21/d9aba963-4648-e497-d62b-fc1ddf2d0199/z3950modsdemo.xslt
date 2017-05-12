<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:z="http://www.loc.gov/mods/">
<xsl:output method="xml"/>
<xsl:template match="z:mods">
<xml>
	<item>
		<name><xsl:value-of select="z:title"/></name>
		<description><xsl:value-of select="." /></description>
	</item>
</xml>
</xsl:template>	
</xsl:stylesheet>
