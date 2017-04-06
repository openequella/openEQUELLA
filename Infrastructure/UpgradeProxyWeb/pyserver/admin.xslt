<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	
	<xsl:template name="renderThing">
		<xsl:param name="svn" />
		<xsl:param name="jira" />

		<xsl:choose>
			<xsl:when test="$svn/jira">
				<td>
					<a target="_blank" href="http://janus/jira/browse/{$jira/key}"><xsl:value-of select="$jira/key" /></a>
				</td>
				<xsl:choose>
					<xsl:when test="$jira/scope = 'None' and starts-with($jira/key,'TLX')">
						<td bgcolor="beige">
							&#160;
						</td>
						<td>
							<xsl:value-of select="$jira/title" />
						</td>
					</xsl:when>
					<xsl:when test="$jira/scope = 'None' and not(starts-with($jira/key,'TLX'))">
						<td bgcolor="red">
							&#160;
						</td>
						<td>
							<xsl:value-of select="$jira/title" />
						</td>
					</xsl:when>
					<xsl:when test="$jira/scope = 'Public'">
						<td bgcolor="green">
							Public
						</td>
						<td>
							<xsl:value-of select="$jira/public_title" />
						</td>
					</xsl:when>
					<xsl:when test="$jira/scope = 'Protected'">
						<td bgcolor="orange">
							Protected
						</td>
						<td>
							<xsl:value-of select="$jira/public_title" />
						</td>
					</xsl:when>
					<xsl:when test="$jira/scope = 'Private'">
						<td bgcolor="pink">
							Private
						</td>
						<td>
							<xsl:value-of select="$jira/public_title" />
						</td>
					</xsl:when>
					<xsl:otherwise>
						<td>ERROR</td>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<td />
				<td>
					<xsl:value-of select="$svn/author" />
				</td>
				<td>
					<xsl:value-of select="$svn/message" />
				</td>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="/">
		<html>
			<head>
				<title>TLE Upgrade Server Administration</title>
				<style type="text/css">
					a.white {color: white  }
					a.white:link {color: white }
					a.white:visited {color: white  }
					a.white:active {color: white }
					a.white:hover {color: white }
				</style>
			</head>
			<body>
				<form>
					<h1>TLE Upgrade Server Administration</h1>
					<xsl:choose>
						<xsl:when test="not(/xml/branch = 'None')">
							<h3><xsl:value-of select="/xml/branch" /></h3>
							
							<input type="submit" name="action" value="Change branch" />
							<input type="hidden" name="branch" value="{/xml/branch}" />
							
							<table border="border">
								<tr>
									<th>SVN</th>
									<th>Jira</th>
									<th>Scope</th>
									<th>Title</th>
								</tr>
								<xsl:for-each select="/xml/info">
									<xsl:sort order="descending" select="rev" />
									<xsl:variable name="svn" select="." />
									<xsl:choose>
										<xsl:when test="count($svn/jira) &gt; 1">
											<tr>
												<td rowspan="{count($svn/jira)}"><a href="log.do?rev={$svn/rev}&amp;{/xml/params}" target="svnlog"><xsl:value-of select="$svn/rev" /></a></td>
												<xsl:call-template name="renderThing">
													<xsl:with-param name="svn" select="$svn" />
													<xsl:with-param name="jira" select="$svn/jira[1]" />
												</xsl:call-template>
											</tr>
											<xsl:for-each select="$svn/jira[position()&gt;1]">
												<xsl:variable name="jira" select="." />
												<xsl:call-template name="renderThing">
													<xsl:with-param name="svn" select="$svn" />
													<xsl:with-param name="jira" select="$jira" />
												</xsl:call-template>
											</xsl:for-each>
										</xsl:when>
										<xsl:otherwise>
											<tr>
												<td><a href="log.do?rev={$svn/rev}&amp;{/xml/params}" target="svnlog"><xsl:value-of select="$svn/rev" /></a></td>
												<xsl:call-template name="renderThing">
													<xsl:with-param name="svn" select="$svn" />
													<xsl:with-param name="jira" select="$svn/jira[1]" />
												</xsl:call-template>
											</tr>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</table>
						</xsl:when>
						<xsl:otherwise>
							<p>
								Please choose a branch to administer:
								<select name="branch">
									<option value="/branches/Huon@2074">2.1 - Huon</option>
									<option value="/branches/Tamar@3112">2.2 - Tamar</option>
									<option value="/branches/Gordon@4292">2.3 - Gordon</option>
									<option value="/branches/Weld@5377" selected="selected">2.4 - Weld</option>
									<option value="/trunk@3112">2.5 - Trunk</option>
								</select>
								<input type="submit" name="action" value="Administer" />
							</p>
						</xsl:otherwise>
					</xsl:choose>
					<input type="hidden" name="username" value="{/xml/username}" />
					<input type="hidden" name="password" value="{/xml/password}" />
				</form>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>