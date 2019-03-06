<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/material">
		<xml>
			<item>
				<itembody>
					<name>
						<xsl:value-of select="title"/>
					</name>
					<description>
						<xsl:value-of select="description"/>
					</description>
				</itembody>
				<lom>
					<general>
						<title>
							<xsl:value-of select="title"/>
						</title>
						<description>
							<xsl:value-of select="description"/>
						</description>
						<language>
							<xsl:value-of select="languages/language"/>
						</language>
						<keyword>
							<xsl:value-of select="categories/category"/>
						</keyword>
					</general>
					<educational>
						<learning.resource.type>
							<xsl:value-of select="materialType"/>
						</learning.resource.type>
					</educational>
					<technical>
						<format>
							<xsl:value-of select="materialType"/>
						</format>
					</technical>
				</lom>
				<itembody>
					<item_type>URL</item_type>
				</itembody>
				<attachments>
					<attachment disabled="false" type="remote">
						<file><xsl:value-of select="URL"/></file>
						<description><xsl:value-of select="title"/></description>
					</attachment>
				</attachments>
				<rights>
					<offer>
						<party>
							<context>
								<name>
									<xsl:value-of select="authorName"/>
								</name>
							</context>
						</party>
						<permission>
							<display />
							<execute />
							<play />
							<print />
							<tle_ownerMustAccept />
							<tle_showLicenceInComposition />
							<container>
								<constraint />
							</container>
						</permission>
					</offer>
				</rights>
									
			</item>
		</xml>
	</xsl:template>
</xsl:stylesheet>
