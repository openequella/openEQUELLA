<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>


	<xsl:template match="item/name">
		<name2>
			<xsl:value-of select="." />
		</name2>
	</xsl:template>

	<xsl:template match="item/description">
		<description2>
			<xsl:value-of select="." />
		</description2>
	</xsl:template>

</xsl:stylesheet>
