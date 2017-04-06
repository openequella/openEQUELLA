<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>TLE Upgrade Server Administration</title>
			</head>
			<body>
				<pre><xsl:value-of select="/xml/info" /></pre><br />
				<a href="log.do?{/xml/params}">Log view</a>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>