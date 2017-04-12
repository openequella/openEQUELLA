<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">
<xsl:template match="/">
  <html>
  <body>
          <ol>
      <xsl:for-each select="taglib/tag" order-by="+ name">
            <li><b><xsl:value-of select="name"/></b></li><br/>
            <i><xsl:value-of select="tagclass"/></i>
            <ul>
              
              
              <li><b>Body:</b> <xsl:value-of select="bodycontent"/></li>
              
              <li><b>Info:</b> <xsl:value-of select="info"/></li><br/><br/>
              
              <b>Attributes:</b> 
              <xsl:for-each select="attribute" order-by="+ name">
              <ul>
                <li><i><xsl:value-of select="name"/></i></li><br/>
                  <b>Required:</b> <xsl:value-of select="required"/><br/>
                  <b>Real-time evaluated:</b> <xsl:value-of select="rtexprvalue"/><br/>
              </ul>
              </xsl:for-each>
            </ul><br/><br/>
        
      </xsl:for-each>
      </ol>
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>
