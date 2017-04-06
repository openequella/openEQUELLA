<xsl:stylesheet xmlns:xalan="http://xml.apache.org/xslt" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" indent="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
	<xsl:preserve-space elements="*"/>
	<xsl:template match="items/xml/item">
		<tr>
			<td width="10%" bgcolor="#E5E5E5">
				<font class="pagetext">
					<xsl:value-of select="@version"/>
				</font>
			</td>
			<td width="55%" bgcolor="#E5E5E5">
				<a class="itemheading" href="{/xml/server}items/{@id}/{@version}/">
					<xsl:value-of select="name"/>
				</a>
			</td>
			<td bgcolor="#E5E5E5">
				<span class="pagetext">
					<xsl:value-of select="concat(user/givenname, ' ', user/surname, ' (', user/username, ')')"/>
				</span>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="/">
		<html>
			<head>
				<link rel="stylesheet" type="text/css" href="{xml/server}ledge.css"/>
			</head>
			<body>
				<table border="0" width="80%" cellspacing="0" cellpadding="7" class="MainTable">
					<tr>
						<td colspan="2" width="100%" valign="top">
							<table border="0" width="100%" cellspacing="0" cellpadding="0" background="{xml/server}images/FrameBannerRepeat.jpg">
								<tr>
									<td width="33%">
										<img border="0" src="{xml/server}images/FrameBanner.jpg" width="570" height="70" alt="EQUELLA"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td colspan="2" style="padding-left:20px" class="pagetext">Hello <xsl:value-of select="xml/user/givenname"/>&#160;<xsl:value-of select="xml/user/surname"/>,</td>
					</tr>
					<tr>
						<td colspan="2" style="padding-left:20px" class="pagetext">The following item(s) are now publicly available in EQUELLA:</td>
					</tr>
					<td height="15">&#160;</td>
					<tr>
						<td width="100%" background="{xml/server}images/dottedline.gif" height="12"/>
					</tr>
					<tr>
						<td>
							<table border="0" width="100%" cellspacing="1" cellpadding="3">
								<tr>
									<td width="10%" bgcolor="#666666">
										<font class="labeltextwhite">Version</font>
									</td>
									<td width="55%" bgcolor="#666666">
										<font class="labeltextwhite">Title</font>
									</td>
									<td width="35%" bgcolor="#666666">
										<font class="labeltextwhite">Owner</font>
									</td>
								</tr>
								<xsl:apply-templates select="xml/items/xml/item"/>
							</table>
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>