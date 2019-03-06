<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:z="http://www.loc.gov/mods/">
	<xsl:output method="xml"/>
	<xsl:template match="z:mods">
		<itembody>
			<name><xsl:value-of select="z:title"/></name>
		</itembody>
		<copyright type="Journal">
			<title><xsl:value-of select="z:title"/></title>
			<xsl:for-each select="z:identifier[@type='issn']">
				<issn><xsl:value-of select="." /></issn>
			</xsl:for-each>
			<publication>
				<year><xsl:value-of select="z:date[@type='issued']" /></year>
			</publication>
		</copyright>
	</xsl:template>
	<xsl:template match="/">
		<xml>
			<item>
				<xsl:apply-templates select="/xml/z:mods"/>
			</item>
		</xml>
	</xsl:template>
</xsl:stylesheet>