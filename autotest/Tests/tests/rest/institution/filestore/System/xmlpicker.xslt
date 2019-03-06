<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" omit-xml-declaration="yes" />
	<xsl:template match="xml">
		<xsl:call-template name="element">
			<xsl:with-param name="parentid" select="'/'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="element">
		<xsl:param name="parentid"/>

		<xsl:variable name="nodename" select="local-name(.)"/>
		<xsl:variable name="elementid" select="concat($parentid, $nodename)"/>
		<xsl:variable name="jsid" select="translate($elementid, '/', '_')"/>
		<xsl:variable name="hasChildren" select="count(./*[not(@attribute = 'true')]) > 0" />

		<xsl:if test="$hasChildren">
			<div class="nonleaf">
				<a href="javascript:switchDisplay('{$jsid}')"><img src="images/folderclosed.gif" id="{concat('img', $jsid)}"/>&#160;
				<xsl:value-of select="$nodename"/></a>
				<xsl:call-template name="attributes">
					<xsl:with-param name="parentid" select="$elementid"/>
				</xsl:call-template>
			</div>
			<div class="child" id="{concat('div', $jsid)}" style="display: none">
				<xsl:for-each select="child::*[not(@attribute = 'true')]">
					<xsl:sort select="."/>
					<xsl:call-template name="element">
						<xsl:with-param name="parentid" select="concat($elementid,'/')"/>
					</xsl:call-template>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="not($hasChildren)">
			<div class="leaf">
           		<xsl:if test="@field = 'true'">
                    - <a class="leaf" href="javascript:selectElement('{$elementid}')"><xsl:value-of select="$nodename"/></a>
                </xsl:if>
                <xsl:if test="not(@field = 'true')">
                    - <xsl:value-of select="$nodename"/>
                </xsl:if>
				<xsl:call-template name="attributes">
					<xsl:with-param name="parentid" select="$elementid"/>
				</xsl:call-template>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template name="attributes">
		<xsl:param name="parentid"/>
		<xsl:if test="count(./*[@attribute = 'true' and @field = 'true']) > 0">
			&#160;<span class="attributes">[
				<xsl:for-each select="./*[@attribute = 'true' and @field = 'true']">
                    <xsl:variable name="nodename" select="local-name(.)" />
					<a href="javascript:selectElement('{concat($parentid, '/@', $nodename)}')">@<xsl:value-of select="$nodename" /></a>
				</xsl:for-each>
			]</span>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
