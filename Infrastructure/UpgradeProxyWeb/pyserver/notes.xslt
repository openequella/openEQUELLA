<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<html>
			<h1>Release notes</h1>
			<h2>From: <i><xsl:value-of select="/xml/old" /></i></h2>
			<h2>To: <i><xsl:value-of select="/xml/new" /></i></h2>
			<hr />
			<xsl:for-each select="/xml/issue">
				<h3><xsl:value-of select="title" /> (<xsl:value-of select="key" />)</h3>
				<p><xsl:value-of select="description" disable-output-escaping="yes"/></p>
				<hr />
			</xsl:for-each>
		</html>
	</xsl:template>
</xsl:stylesheet>