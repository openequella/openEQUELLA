<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:z="http://www.loc.gov/mods/">
	<xsl:output method="xml"/>
	<xsl:variable name="col" select="0"/>
	<xsl:template name="docell" match="/">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:if test="normalize-space($value)">
			<tr>
				<td width="50%">
					<table border="0" width="100%" cellspacing="0" cellpadding="0">
						<tr>
							<td width="1%" align="right">
								<img border="0" alt="*" src="images/iconregularitem.gif"/>
								<img border="0" alt=" " src="images/spacer.gif" width="5" height="1"/>
							</td>
							<td width="99%">
								<font class="pagesubheading">
									<xsl:value-of select="$name"/>
								</font>
							</td>
						</tr>
						<tr>
							<td width="1%"/>
							<td>
								<font class="pagetext">
									<xsl:value-of select="$value"/>
								</font>
							</td>
						</tr>
						<tr>
							<td width="100%" colspan="2"><img border="0" alt=" " src="images/spacer.gif" width="1" height="15"/></td>
						</tr>
					</table>
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template name="docell2" match="/">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:if test="normalize-space($value)">
			<xsl:if test="$col = 0">
				<xsl:value-of select="'&lt;tr>'" disable-output-escaping="yes"/>
			</xsl:if>
			<td width="50%">
				<table border="0" width="100%" cellspacing="0" cellpadding="0">
					<tr>
						<td width="1%" align="right">
							<img border="0" alt="*" src="images/iconowner.gif"/>
							<img border="0" alt=" " src="images/spacer.gif" width="5" height="1"/>
						</td>
						<td width="99%">
							<font class="pagesubheading">
								<xsl:value-of select="$name"/>
							</font>
						</td>
					</tr>
					<tr>
						<td width="1%"/>
						<td>
							<font class="pagetext">
								<xsl:value-of select="$value"/>
							</font>
						</td>
					</tr>
					<tr>
						<td width="100%" colspan="2">
							<img border="0" alt=" " src="images/spacer.gif" width="1" height="15"/>
						</td>
					</tr>
				</table>
			</td>
		</xsl:if>
	</xsl:template>
	<xsl:template match="z:mods">
		<xsl:call-template name="docell2">
			<xsl:with-param name="name" select="'Source'"/>
			<xsl:with-param name="value" select="/xml/source"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Title Varies'"/>
			<xsl:with-param name="value" select="z:title[@type='alternative']"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'ISBN'"/>
			<xsl:with-param name="value" select="z:identifier[@type='isbn']"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Bib Note'"/>
			<xsl:with-param name="value" select="z:bibnote"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'LCCN'"/>
			<xsl:with-param name="value" select="z:identifier[@type='lccn']"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'LC Call No'"/>
			<xsl:with-param name="value" select="z:subject/z:lccallno"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Dewey #'"/>
			<xsl:with-param name="value" select="z:subject/z:dewey"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Series'"/>
			<xsl:with-param name="value" select="z:series"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Subject'"/>
			<xsl:with-param name="value" select="z:subject/z:topic"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Physical Description'"/>
			<xsl:with-param name="value" select="z:formAndPhysicalDescription/z:extent"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Other Author'"/>
			<xsl:with-param name="value" select="z:name[@type='personal']"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Publisher'"/>
			<xsl:with-param name="value" select="concat(z:publication/z:placeOfPublication, ' ', z:publication/z:publisher, ' ', z:date)"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Edition'"/>
			<xsl:with-param name="value" select="z:publication/z:edition"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Uniform Title'"/>
			<xsl:with-param name="value" select="z:uniformtitle"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Source of Acq.'"/>
			<xsl:with-param name="value" select="z:sourceacq"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Location'"/>
			<xsl:with-param name="value" select="z:location"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Electronic Location'"/>
			<xsl:with-param name="value" select="z:identifier[@type='uri']"/>
		</xsl:call-template>
		<xsl:call-template name="docell">
			<xsl:with-param name="name" select="'Contents'"/>
			<xsl:with-param name="value" select="z:note[@type='table of contents']"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="/">
		<link rel="stylesheet" type="text/css" href="ledge.css"/>
		<table border="0" width="100%" cellspacing="0" cellpadding="0" class="contenttable">
			<tr>
				<td width="50%">
					<table border="0" width="100%" cellspacing="0" cellpadding="0">
						<tr>
							<td width="1%" valign="top" class="iconalignright">
								<img border="0" alt="*" src="images/greenbullet.gif" width="17" height="17"/>
							</td>
							<td>
								<table border="0" width="100%" cellspacing="0" cellpadding="0">
									<tr>
										<td width="100%" rowspan="2" valign="top">
											<font class="pageheading">
												<xsl:value-of select="concat(xml/z:mods/z:title, ' ', xml/z:mods/z:name/z:displayForm)"/>
											</font>
										</td>
										<!--										<td width="1%" rowspan="2" valign="top">
											<img border="0" alt="*" src="images/iconowner.gif" width="17" height="17"/>
										</td>
										<td width="1%" rowspan="2">
											<img border="0" alt=" " src="images/spacer.gif" width="10" height="1"/>
										</td>
										<td width="1%">
											<img border="0" alt=" " src="images/spacer.gif" width="1" height="2"/>
										</td>-->
									</tr>
									<!--									<tr>
										<td width="1%" nowrap="true">
											<font class="pagesubheading">Owner</font>
											<br/>
											<font class="pagetext">
												<xsl:value-of select="/xml/source"/>
											</font>
										</td>
									</tr>-->
								</table>
								<table border="0" width="100%" cellspacing="0" cellpadding="0">
									<!--
									<tr>
										<td width="100%" colspan="2" background="images/dottedline.gif" height="12"/>
									</tr>
									<tr>
										<td width="1%" align="right">
											<img border="0" alt="*" src="images/icondescription.gif"/>
											<img border="0" alt=" " src="images/spacer.gif" width="5" height="1"/>
										</td>
										<td width="100%">
											<span class="pagesubheading">Description</span>
										</td>
									</tr>
									<tr>
										<td width="1%"/>
										<td width="100%">
											<span class="pagetext">DESCRIPTION</span>
										</td>
									</tr>
									<tr>
										<td width="100%" colspan="2">
											<img border="0" alt=" " src="images/spacer.gif" width="1" height="15"/>
										</td>
									</tr>
									<tr>
										<td width="1%" align="right">
											<img border="0" alt="k" src="images/iconkeyword.gif"/>
											<img border="0" alt=" " src="images/spacer.gif" width="5" height="1"/>
										</td>
										<td width="100%">
											<span class="pagesubheading">Keywords</span>
										</td>
									</tr>
									<tr>
										<td width="1%"/>
										<td width="100%">
											<span class="pagetext">KEYWORDS</span>
										</td>
									</tr>
									<tr>
										<td width="100%" colspan="2">
											<img border="0" alt=" " src="images/spacer.gif" width="1" height="15"/>
										</td>
									</tr>-->
									<tr>
										<td colspan="2">
											<table border="0" width="100%">
												<tr>
													<td width="100%" colspan="2">
														<img border="0" alt=" " src="images/spacer.gif" width="1" height="15"/>
													</td>
												</tr>
												<tr>
													<td width="100%" colspan="2">
														<span class="pagesubheading">Details</span>
													</td>
												</tr>
												<tr>
													<td width="100%" colspan="2" background="images/dottedline.gif" height="12"/>
												</tr>
												<tr>
													<td colspan="2" width="100%">
														<table border="0" width="100%" cellspacing="0" cellpadding="0">
															<xsl:apply-templates select="/xml/z:mods"/>
														</table>
													</td>
												</tr>
											</table>
										</td>
									</tr>
								</table>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>