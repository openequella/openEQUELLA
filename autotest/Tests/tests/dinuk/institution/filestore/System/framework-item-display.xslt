<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output method="html" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
      <xsl:template match="/framework_item">
     		<xsl:call-template name="curriculum"/>
      	</xsl:template>
      	<xsl:template match="/standard">
     		<xsl:call-template name="curriculum"/>
      	</xsl:template>
      <xsl:template name="curriculum">
		<html>
			<head>
				<title><xsl:value-of select="/name"/></title>
			</head>
			<body>
				<div>
					<xsl:text disable-output-escaping="yes">&lt;!--TOP--&gt;</xsl:text>
					<p>
						<span>
							<i>
								<span style="font-size:10.0pt;font-family:Verdana;color:#4667C6">
									<xsl:value-of select="description"/>
								</span>
							</i>
						</span>
					</p>
					<xsl:if test="count(dot_points) > 0">
						<p>
							<b>
								<span style="font-size:12.0pt;font-family:Times New Roman">
									<xsl:value-of select="dot_points/heading"/>
								</span>
							</b>
						</p>
						<ul>
							<xsl:for-each select="dot_points/point">
								<li>
									<span style="font-size:12.0pt;font-family:Times New Roman">
										<xsl:value-of select="."/>
									</span>
								</li>
							</xsl:for-each>
						</ul>
					</xsl:if>
					<xsl:if test="resources">
						<xsl:text disable-output-escaping="yes">&lt;hr&gt;</xsl:text>
						<h3>
							<span style="font-family:Verdana;color:blue">Useful Links</span>
						</h3>
						<xsl:for-each select="resources/resource">
							<p>
								<span style="font-size:10.0pt;font-family:Verdana">
									<a href="{location}">
										<span style="font-weight:normal">
											<xsl:value-of select="name"/>
										</span>
									</a>
								</span>
								<span style="font-family:Verdana"/>
							</p>
						</xsl:for-each>
					</xsl:if>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>

