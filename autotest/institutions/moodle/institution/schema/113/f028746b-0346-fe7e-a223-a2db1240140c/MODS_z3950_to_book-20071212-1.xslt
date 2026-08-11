<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:z="http://www.loc.gov/mods/">
	<xsl:output method="xml"/>
	<xsl:template match="z:mods">
		<itembody>
			<name><xsl:value-of select="z:title"/></name>
			<xsl:for-each select="z:note">
				<description><xsl:value-of select="." /></description>
			</xsl:for-each>
		</itembody>
		<copyright type="book">
			<title><xsl:value-of select="z:title"/></title>
			<xsl:for-each select="z:identifier[@type='isbn']">
				<isbn><xsl:value-of select="." /></isbn>
			</xsl:for-each>
			<xsl:for-each select="z:identifier[@type='issn']">
				<issn><xsl:value-of select="." /></issn>
			</xsl:for-each>
			<authors>
				<xsl:for-each select="z:name[@role='creator']/z:displayForm">
					<author><xsl:value-of select="." /></author>
				</xsl:for-each>
			</authors>
			<editors>
				<xsl:for-each select="z:name[@role='editor']/z:displayForm"> <!-- @type='personal'? -->
					<editor><xsl:value-of select="." /></editor>
				</xsl:for-each>			
			</editors>
			<edition><xsl:value-of select="z:publication/z:edition" /></edition>
			<abstract>
				<xsl:for-each select="z:note">
					<xsl:value-of select="."/>
					<xsl:if test="not(position() = last())">
					    <xsl:text >, </xsl:text>
					</xsl:if>
				</xsl:for-each>
			</abstract>
			<xsl:for-each select="z:publication/z:publisher">
				<publisher><xsl:value-of select="." /></publisher>
			</xsl:for-each>
			<publication>
				<place><xsl:value-of select="z:publication/z:placeOfPublication" /></place>
				<year><xsl:value-of select="z:date[@type='issued']" /></year>
			</publication>
			<pages><xsl:value-of select="z:formAndPhysicalDescription/z:pages" /></pages>
		</copyright>
	</xsl:template>
	<xsl:template match="/">
		<xml>
			<item>
				<xsl:apply-templates select="/xml/z:mods"/>
			</item>
		</xml>
	</xsl:template>
</xsl:stylesheet>