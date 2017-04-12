<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- For Use with JSP2.x TLDs -->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:j2ee="http://java.sun.com/xml/ns/j2ee">
                

  <xsl:output method="html" indent="yes"/>
  
  <xsl:template match="/j2ee:taglib">
    <xsl:element name="taglib" namespace="http://java.sun.com/xml/ns/javaee">
      <xsl:attribute name="xsi:schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd</xsl:attribute>
      <xsl:attribute name="version">2.1</xsl:attribute>
            
      <html>
      <body>
        
        <table border="1">
          <tr>
            <th width="1%">Tag</th>
            <th>Body</th>
            <th>Attributes</th>
          </tr>
            
          <xsl:for-each select="j2ee:tag">
          <xsl:sort select="j2ee:name"/>
      
          <tr>              
            <td valign="top">
              <b><xsl:value-of select="j2ee:name"/></b>
              <p>
                <i>Description:</i><br/>
                <xsl:if test="not(j2ee:description)">
                  <span style="color:red">( no description defined for this tag )</span>
                </xsl:if>
                <xsl:value-of select="j2ee:description"/>                                
              </p>          
              <p>
                <i>Tag Class:</i><br/>
                <xsl:value-of select="j2ee:tag-class"/><br/>
                <xsl:value-of select="j2ee:tei-class"/>
              </p>
            </td>
            <td valign="top">
              <xsl:value-of select="j2ee:body-content"/>
            </td>
            <td valign="top">
              <xsl:if test="not(j2ee:attribute)">
                <b>no attributes</b>
              </xsl:if>            
              <xsl:for-each select="j2ee:attribute">
              <xsl:sort select="j2ee:name"/>
                <ul>
                  <li>
                    <xsl:if test="j2ee:required = 'true'">
                      <b><xsl:value-of select="j2ee:name"/></b>
                    </xsl:if>
                                      
                    <xsl:if test="(j2ee:required = 'false')">
                      <xsl:value-of select="j2ee:name"/>  
                    </xsl:if>
                    
                    <xsl:if test="(j2ee:required = 'true') or (j2ee:rtexprvalue = 'false')">
                      <span style="color:red">
                      <i> (
                        <xsl:if test="(j2ee:required = 'true')">                
                        required
                        </xsl:if>
                        <xsl:if test="(j2ee:rtexprvalue = 'false')">                
                        <b> and not real time evaluated</b>
                        </xsl:if>
                      )</i>
                      </span>
                    </xsl:if>
                    <br/>
                    <xsl:value-of select="j2ee:description"/>
                  </li>                            
                  
                </ul>
              </xsl:for-each>            
            </td>      
          </tr>                      
          </xsl:for-each>
        </table>                       
      </body>
      </html>

    </xsl:element>
  </xsl:template>
   
</xsl:stylesheet>
