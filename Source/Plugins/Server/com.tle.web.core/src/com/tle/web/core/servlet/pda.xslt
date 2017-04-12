<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:param name="tab">summary</xsl:param>
	<xsl:param name="baseref">/</xsl:param>
	<xsl:param name="itemid"/>
	<xsl:param name="version">1</xsl:param>
	<xsl:param name="query">*</xsl:param>
	<xsl:param name="page">1</xsl:param>
	<xsl:param name="searchtype">standard</xsl:param>
	<xsl:variable name="u" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
	<xsl:variable name="l" select="'abcdefghijklmnopqrstuvwxyz'"/>
	<xsl:template match="/">
		<html>
			<head>
				<base href="{$baseref}" />
				<link rel="stylesheet" type="text/css" href="css/wizard.css"/>
				<link rel="stylesheet" type="text/css" media="handheld" href="pda/pda.css"/>
				<link rel="stylesheet" type="text/css" media="screen" href="pda/screen.css"/>
				<link rel="stylesheet" type="text/css" href="pda/banner.css" />
				<style type="text/css">
					#banner {
						margin-bottom: 0;
						padding-bottom: 0;
					}
				</style>
				<link rel="stylesheet" type="text/css" href="pda/custom.css" />
			</head>
			<body>
				<div id="banner">
					<img class="badge" src="images/pda/badge.jpg" alt="Equella" />
					<img class="background" src="images/pda/banner.jpg" alt="Equella" />
				</div>
				<table cellpadding="0" cellspacing="0">
					<tr>
						<td style="margin-top: 0; padding-top: 0;">
							<xsl:call-template name="renderTab">
								<xsl:with-param name="tabName">Summary</xsl:with-param>
							</xsl:call-template>
							<xsl:if test="/xml/item/attachments">
								<xsl:call-template name="renderTab">
									<xsl:with-param name="tabName">Attachments</xsl:with-param>
								</xsl:call-template>
							</xsl:if>
							<xsl:call-template name="renderTab">
								<xsl:with-param name="tabName">Details</xsl:with-param>
							</xsl:call-template>
						</td>
					</tr>
				</table>
				<xsl:choose>
					<xsl:when test="$tab='summary'">
						<xsl:call-template name="summary"/>
					</xsl:when>
					<xsl:when test="$tab='attachments'">
						<xsl:call-template name="attachments"/>
					</xsl:when>
					<xsl:when test="$tab='details'">
						<xsl:call-template name="details"/>
					</xsl:when>
				</xsl:choose>
				<a href="javascript:window.close();" class="smallText">Close</a>
			</body>
		</html>
	</xsl:template>
	<xsl:template name="renderTab">
		<xsl:param name="tabName">none</xsl:param>
		<a>
			<xsl:attribute name="href">pda/DisplayItem?itemid=<xsl:value-of select="$itemid"/>&amp;version=<xsl:value-of select="$version"/>&amp;tab=<xsl:value-of select="translate($tabName,$u,$l)"/></xsl:attribute>
			<xsl:attribute name="class"><xsl:choose>
				<xsl:when test="translate($tabName,$u,$l)!=translate($tab,$u,$l)">wizardTab wizardTabCurrent</xsl:when>
				<xsl:when test="translate($tabName,$u,$l)=translate($tab,$u,$l)">wizardTab wizardTabEnabled</xsl:when>
			</xsl:choose></xsl:attribute>
			<span><xsl:value-of select="$tabName" /></span>
		</a>
	</xsl:template>
	<xsl:template name="heading">
		<h1><xsl:value-of select="/xml/item/name"/></h1>
		<a class="smallText"><xsl:attribute name="href">items/<xsl:value-of select="$itemid"/>/<xsl:value-of select="$version"/>/ViewItem.jsp</xsl:attribute>[View full item]</a>
	</xsl:template>

	<xsl:template name="summary">
		<xsl:call-template name="heading"/>
		<table width="100%">
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
			<tr>
				<td width="17" valign="top">
					<img src="images/iconowner.gif"/>
				</td>
				<td class="heading">Owner:</td>
				<td class="smallText">
					<xsl:value-of select="concat(/xml/item/owner/username, ' - ', /xml/item/owner/givenname, ' ', /xml/item/owner/surname)"/>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/icondescription.gif"/>
				</td>
				<td class="heading">Description:</td>
				<td class="smallText">
					<xsl:value-of select="/xml/item/description"/>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/iconkeyword.gif"/>
				</td>
				<td class="heading">Keywords:</td>
				<td class="smallText">
					<xsl:value-of select="/xml/item/keywords"/>
				</td>
			</tr>
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template name="attachments">
		<xsl:call-template name="heading"/>
		<table width="100%">
			<tr>
				<td colspan="3" class="heading">Associated Information:</td>
			</tr>
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
			<xsl:if test="/xml/item/attachments/attachment[@type = 'local']">
				<xsl:apply-templates mode="links" select="/xml/item/attachments/attachment[@type = 'local']"/>
			</xsl:if>
			<xsl:if test="/xml/item/attachments/attachment[@type = 'remote']">
				<xsl:apply-templates select="/xml/item/attachments/attachment[@type = 'remote']"/>
			</xsl:if>
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template name="details">
		<xsl:call-template name="heading"/>
		<table width="100%">
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
			<tr>
				<td width="17" valign="top">
					<img src="images/iconregularitem.gif"/>
				</td>
				<td class="heading">Resource Type:</td>
				<td class="smallText">
					<xsl:value-of select="/xml/item/itembody/type"/>
					<xsl:value-of select="/xml/item/itemtype"/>
					<xsl:value-of select="/xml/item/dc/type"/>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/iconregularitem.gif"/>
				</td>
				<td class="heading">Audience:</td>
				<td class="smallText">
					<xsl:for-each select="/xml/item/itembody/edna/audience">
						<xsl:value-of select="."/>
						<xsl:if test="position() != last()">, </xsl:if>
					</xsl:for-each>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/iconregularitem.gif"/>
				</td>
				<td class="heading">Language:</td>
				<td class="smallText">
					<xsl:value-of select="/xml/item/itembody/dc/language"/>
					<xsl:for-each select="/xml/item/itembody/language">
						<xsl:value-of select="."/>
						<xsl:if test="position() != last()">, </xsl:if>
					</xsl:for-each>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/iconregularitem.gif"/>
				</td>
				<td class="heading">Date Modified:</td>
				<td class="smallText">
					<xsl:call-template name="todate">
						<xsl:with-param name="text" select="/xml/item/datemodified"/>
					</xsl:call-template>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<img src="images/iconregularitem.gif"/>
				</td>
				<td class="heading">User Level:</td>
				<td class="smallText">
					<xsl:for-each select="/xml/item/itembody/edna/userlevel">
						<xsl:value-of select="."/>
						<xsl:if test="position() != last()">, </xsl:if>
					</xsl:for-each>
				</td>
			</tr>
			<tr>
				<td colspan="3">
					<hr/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="/xml/item/attachments/attachment[@type = 'remote']">
		<tr>
			<td><img alt="*" src="images/iconweblink.gif"/></td>
			<td colspan="2" class="smallText" width="100%">
				<a href="{file}">
					<xsl:value-of select="description"/>
				</a>
			</td>
		</tr>
	</xsl:template>
	<xsl:template mode="links" match="/xml/item/attachments/attachment[@type = 'local']">
		<tr>
			<td><img alt="*" src="images/iconattachment.gif" class="item"/></td>
			<td class="smallText" width="100%">
				<a>
					<xsl:attribute name="href">items/<xsl:value-of select="concat($itemid, '/', $version, '/',file)"/></xsl:attribute>
					<xsl:value-of select="description"/>
				</a>
			</td>
			<td class="smallText" nowrap="true">
				<xsl:value-of select="concat(' (', size, ' bytes)') "/>
				<br/>
				<xsl:value-of select="mimetype"/>
			</td>
		</tr>
	</xsl:template>
	<xsl:template name="todate">
		<xsl:param name="text"/>
		<xsl:value-of select="concat(substring($text, 9, 2), '/', substring($text, 6, 2), '/', substring($text, 1, 4))"/>
	</xsl:template>
</xsl:stylesheet>
