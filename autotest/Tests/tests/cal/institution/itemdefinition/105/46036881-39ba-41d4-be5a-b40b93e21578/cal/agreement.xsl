<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
  <body>
	  <h2><center>CAL Agreement Title (XSLT)</center></h2>
  <div style="color:#ff0000;text-align: center;">
	  <xsl:value-of select="xml/item/copyright/portions/portion/title"/></div>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>
