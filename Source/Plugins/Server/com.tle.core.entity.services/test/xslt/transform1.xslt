<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:variable name="itemdir" select="/xml/itemdir" />
	<xsl:variable name="dir" select="string('./')" />
	<xsl:variable name="imgPath">./</xsl:variable>

	<xsl:variable name="LICENSE_TYPE_CC">Creative Commons</xsl:variable>
	<xsl:variable name="LICENSE_TYPE_BCC">BC Commons</xsl:variable>
	<xsl:variable name="LICENSE_TYPE_GPL">GPL</xsl:variable>

	<xsl:variable name="POWER_SEARCH_ROOT">http://solr.bccampus.ca/bcc/access/search.do?paging.page=1&#38;qs.clicked=true&#38;qs.query=&#38;pow.method=select&#38;pow.powerSearchUuid=ae0d5e05-41bb-ccea-a5fd-f68a0ce34629&#38;pow.powerXml=</xsl:variable>

	<xsl:variable name="PS_AUTHOR_1">%3Cxml%3E%3Ccontributordetails%3E%3Cname%3E%22</xsl:variable>
	<xsl:variable name="PS_AUTHOR_2">%22%3C%2Fname%3E%3C%2Fcontributordetails%3E%3Clom%3E%3Clifecycle%3E%3Ccontribute%3E%3Ccentity%3E%3Cvcard%3E%22</xsl:variable>
	<xsl:variable name="PS_AUTHOR_3">%22%3C%2Fvcard%3E%3C%2Fcentity%3E%3C%2Fcontribute%3E%3C%2Flifecycle%3E%3C%2Flom%3E%3C%2Fxml%3E</xsl:variable>

	<xsl:variable name="PS_KEYWORD_1">%3Cxml%3E%3Clom%3E%3Cgeneral%3E%3Ckeyword%3E%22</xsl:variable>
	<xsl:variable name="PS_KEYWORD_2">%22%3C%2Fkeyword%3E%3C%2Fgeneral%3E%3C%2Flom%3E%3Citem%3E%3Ckeywords%3E%22</xsl:variable>
	<xsl:variable name="PS_KEYWORD_3">%22%3C%2Fkeywords%3E%3C%2Fitem%3E%3C%2Fxml%3E</xsl:variable>

	<xsl:variable name="PS_COURSE_1">%3Cxml%3E%3COPDF%3E%3CBC_Course_Name%3E%22</xsl:variable>
	<xsl:variable name="PS_COURSE_2">%22%3C%2FBC_Course_Name%3E%3C%2FOPDF%3E%3C%2Fxml%3E</xsl:variable>

	<xsl:variable name="PS_PROGRAM_1">%3Cxml%3E%3COPDF%3E%3CBC_Program_Name%3E%22</xsl:variable>
	<xsl:variable name="PS_PROGRAM_2">%22%3C%2FBC_Program_Name%3E%3C%2FOPDF%3E%3C%2Fxml%3E</xsl:variable>

	<xsl:variable name="PS_TRACKER_1">%3Cxml%3E%3COPDF%3E%3COPDF_Tracking%3E%22</xsl:variable>
	<xsl:variable name="PS_TRACKER_2">%22%3C%2FOPDF_Tracking%3E%3C%2FOPDF%3E%3C%2Fxml%3E</xsl:variable>


	<xsl:variable name="PS_SUBJECT_11">%3Cxml%3E%3Citem%3E%3Csubject_class_level1%3E%22</xsl:variable>
	<xsl:variable name="PS_SUBJECT_12">%22%3C%2Fsubject_class_level1%3E%3C%2Fitem%3E%3C%2Fxml%3E</xsl:variable>
	<xsl:variable name="PS_SUBJECT_21">%3Cxml%3E%3Citem%3E%3Csubject_class_level2%3E%22</xsl:variable>
	<xsl:variable name="PS_SUBJECT_22">%22%3C%2Fsubject_class_level2%3E%3C%2Fitem%3E%3C%2Fxml%3E</xsl:variable>
	<xsl:variable name="PS_SUBJECT_2b1">%3Cxml%3E%3Citem%3E%3Csubject_class_level2b%3E%22</xsl:variable>
	<xsl:variable name="PS_SUBJECT_2b2">%22%3C%2Fsubject_class_level2b%3E%3C%2Fitem%3E%3C%2Fxml%3E</xsl:variable>

	<!-- ****************************************************************************-->
	<!-- ****************************************************************************-->

	<xsl:template match="xml">
		<link rel="stylesheet" type="text/css" href="css/searchresults.css" />
		<h3>
			<xsl:value-of select="normalize-space(item/name)"/>
		</h3>
		<div id="template-body-main">
			<xsl:call-template name="intro"/>
			<xsl:call-template name="view-links"/>
			<xsl:call-template name="download-links"/>
			<xsl:call-template name="resources"/>
		</div>
		<div id="template-body-side">
			<xsl:variable name="course-name" select="OPDF/BC_Course_Name"/>
			<div id="opdf">
				<div class="itemdescribe">
					<span>Find Related Resources</span>
				</div>
				<ul>
					<xsl:if test="string-length($course-name) &gt; 0">
						<xsl:variable name="qs">
							<xsl:value-of select="$PS_COURSE_1"/>
							<xsl:call-template name="url-encoder">
								<xsl:with-param name="src-url" select="normalize-space($course-name)"/>
							</xsl:call-template>
							<xsl:value-of select="$PS_COURSE_2"/>
						</xsl:variable>
						<xsl:variable name="uri">
							<xsl:value-of select="$POWER_SEARCH_ROOT"/>
							<xsl:value-of select="$qs"/>
						</xsl:variable>

						<li>
							<a href="{$uri}">More from &quot;<xsl:value-of select="$course-name"/>&quot;</a>
						</li>
					</xsl:if>
					<xsl:if test="(OPDF/OPDF_funded = 'Yes') and (string-length(OPDF/OPDF_Tracking) &gt; 0)">
						<xsl:variable name="tracking-number">
							<xsl:call-template name="url-encoder">
								<xsl:with-param name="src-url" select="normalize-space(OPDF/OPDF_Tracking)"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:variable name="qs">
							<xsl:value-of select="$PS_TRACKER_1"/>
							<xsl:value-of select="$tracking-number"/>
							<xsl:value-of select="$PS_TRACKER_2"/>
						</xsl:variable>
						<xsl:variable name="uri">
							<xsl:value-of select="$POWER_SEARCH_ROOT"/>
							<xsl:value-of select="$qs"/>
						</xsl:variable>
						<xsl:variable name="title">
							<xsl:value-of select="OPDF/OPDF_Tracking"/>
						</xsl:variable>
						
						<li>
							<a href="{$uri}" title="OPDF Project Tracking Number: {$title}">More from this OPDF project</a>
						</li>
					</xsl:if>
					<xsl:apply-templates select="item/subject_class_level1"/>
					<xsl:apply-templates select="item/subject_class_level2"/>
					<xsl:apply-templates select="item/subject_class_level2b"/>
				</ul>
			</div>

			<xsl:if test="count(lom/general/keyword) &gt; 0">
				<div class="itemdescribe">
					<span>Keywords</span>
				</div>
				<ul id="keyword-links">
					<xsl:apply-templates select="lom/general/keyword"/>
				</ul>
			</xsl:if>

			<xsl:call-template name="license">
				<xsl:with-param name="type" select="OPDF/licensetype"/>
			</xsl:call-template>

			<xsl:if test="contributordetails">
				<div id="results-contribinst">
					<div class="itemdescribe">
						<span>Contributing Institutions</span>
					</div>
					<xsl:apply-templates select="contributordetails/institution"/>
				</div>
			</xsl:if>
		</div>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="intro">

		<div id="intro-bloc">
			<div class="itemdescribe">
				<span>Description: </span>
				<xsl:value-of select="substring(item/description,1,999)"/>
				<xsl:if test="string-length(item/description) &gt; 999">&#32;...</xsl:if>
			</div>
			<div class="itemdescribe">
				<span>Author<xsl:if test="count(item/rights/offer/party/context/name) &gt; 1">s</xsl:if>: </span>
				<xsl:apply-templates select="item/rights/offer/party/context/name"/>
			</div>
		</div>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/rights/offer/party/context/name">
		<xsl:variable name="author">
			<xsl:choose>
				<xsl:when test="substring-before(.,'[') = ''">
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="normalize-space(substring-before(.,'['))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="qs">
			<xsl:value-of select="$PS_AUTHOR_1"/>
			<xsl:value-of select="normalize-space($author)"/>
			<xsl:value-of select="$PS_AUTHOR_2"/>
			<xsl:value-of select="normalize-space(.)"/>
			<xsl:value-of select="$PS_AUTHOR_3"/>
		</xsl:variable>
		<xsl:variable name="uri">
			<xsl:value-of select="$POWER_SEARCH_ROOT"/>
			<xsl:value-of select="$qs"/>
		</xsl:variable>
		<xsl:if test="position() &gt; 1">, </xsl:if>
		<a href="{$uri}" title="find more from this author">
			<xsl:value-of select="$author"/>
		</a>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="view-links">

		<xsl:variable name="view-attactments">
			<xsl:choose>
				<xsl:when test="item/itembody/packagefile">
					<xsl:variable name="preview"><xsl:value-of select="$itemdir"/>viewims.jsp</xsl:variable>
					<li>
						<a href="{$preview}">this resource</a>
						<xsl:if test="integration/canadd = 'true'">
							&#32;<input type="submit" name="addButton" value="Add to course" onclick="return itemMethod('integration.jsp?integ.filename=viewitem.jsp', 'add');" />
						</xsl:if>
					</li>
				</xsl:when>
				<xsl:when test="item/attachments/attachment">
					<xsl:apply-templates select="item/attachments/attachment" mode="view"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>

		<xsl:if test="string-length($view-attactments) &gt; 0">
			&#32;
			<div class="link-bloc view"
				><div class="itemdescribe">
					<xsl:element name="img">
						<xsl:attribute name="src"><xsl:value-of select="$imgPath"/>images/view.gif</xsl:attribute>
						<xsl:attribute name="alt"/>
					</xsl:element>
					<span id="view">View</span>
				</div>
				
				<ul class="item-links" id="view">
					<xsl:copy-of select="$view-attactments"/>
				</ul>
			</div>
		</xsl:if>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="download-links">

		<xsl:if test="(item/mediatype = 'Zip') or 
			          (item/mediatype = 'binary') or
				      (item/mediatype = 'IMSCP121')">

			<xsl:variable name="dn-attactments">
				<xsl:choose>
					<xsl:when test="item/itembody/packagefile">
						<xsl:variable name="download">
							<xsl:value-of select="$itemdir"/>viewims.jsp?viewMethod=download</xsl:variable>
						<xsl:variable name="title">file: <xsl:value-of select="item/itembody/packagefile"/> - <xsl:value-of select="item/itembody/packagefile/@name"/>
						</xsl:variable>
						<li>
							<a href="{$download}" title="{$title}">this resource</a>
						</li>
					</xsl:when>
					<xsl:when test="item/attachments/attachment">
						<xsl:apply-templates select="item/attachments/attachment" mode="download"/>
					</xsl:when>
					<xsl:when test="count(item/attachments/attachment) = 0">
						<li>no package file or attachments found</li>
					</xsl:when>
				</xsl:choose>
			</xsl:variable>

			<xsl:if test="string-length($dn-attactments) &gt; 0">
				&#32;
				<div class="link-bloc download"
					><div class="itemdescribe">
						<xsl:element name="img">
							<xsl:attribute name="src"><xsl:value-of select="$imgPath"/>images/download.gif</xsl:attribute>
							<xsl:attribute name="alt"/>
						</xsl:element>
						<span id="dowload">Download</span>
					</div>
					
					<ul class="item-links">
						<xsl:copy-of select="$dn-attactments"/>
					</ul>
				</div>
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/attachments/attachment" mode="view">

		<xsl:variable name="desc">
			<xsl:call-template name="breaker">
				<xsl:with-param name="src" select="description"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="@type='remote'">
				<xsl:variable name="ref">
					<xsl:value-of select="file"/>
				</xsl:variable>
				<li>
					<a href="{$ref}">
						<xsl:value-of select="$desc"/>
					</a>
				</li>
			</xsl:when>
			<xsl:when test="(@type='local')">
				<xsl:variable name="dependants">
					<xsl:choose>
						<xsl:when test="(../../mediatype = 'binary') and contains(file,'.zip')"></xsl:when>
						<xsl:otherwise>
							<xsl:text>true</xsl:text>
							<!-- AE: 	12-Sep-2007 commented out as somethings seems to have changed
									in 3.1 and can't quite figure out what the best logic here should be.
									Worst case is a link might now show when it shouldn't which is the
									lesser of many problems.
									Replaced with "true" bit above.
								-->
							<!--xsl:choose>
								<xsl:when test="../../mediatype = 'Zip'">
									<xsl:apply-templates select="../../attachments/attachment" mode="zip">
										<xsl:with-param name="view-file" select="file"/>
									</xsl:apply-templates>
								</xsl:when>
								<xsl:otherwise>true</xsl:otherwise>
							</xsl:choose-->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:if test="(string-length($dependants) &gt; 0) and ($dependants = 'true')">
					<xsl:variable name="query-string">
						<xsl:if test="conversion='true'">?convert=html</xsl:if>
					</xsl:variable>
					<xsl:variable name="ref">
						<xsl:value-of select="$itemdir"/>
						<xsl:value-of select="translate(file,' ','+')"/>
						<xsl:value-of select="$query-string"/>
					</xsl:variable>
					<li>
						<a href="{$ref}"><xsl:value-of select="$desc"/></a>
					</li>
				</xsl:if>
			</xsl:when>
			
			<xsl:when test="(@type='zip') and ((@mapped='true') or (@mapped='false'))">
				<!-- no zips here -->
			</xsl:when>
			<xsl:otherwise>
				<li>
					<xsl:attribute name="title">
						type: <xsl:value-of select="@type"/> | 
						file: <xsl:value-of select="file"/>  | 
						desc: <xsl:value-of select="description"/>
					</xsl:attribute> *** unknown attachment ***
				</li>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/attachments/attachment" mode="zip">
		<xsl:param name="view-file"/>
		<xsl:if test="@type='zip'">
			<xsl:if test="contains($view-file,file)"><xsl:value-of select="@mapped"/></xsl:if>
		</xsl:if>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/attachments/attachment" mode="download">

		<xsl:variable name="desc">
			<xsl:if test="string-length(description) &lt; 1">
				this resource
			</xsl:if>
			<xsl:call-template name="breaker">
				<xsl:with-param name="src" select="description"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="@type = 'zip'">
			<xsl:variable name="ref">
				<xsl:value-of select="$itemdir"/>
				<xsl:text>_zips/</xsl:text>
				<xsl:value-of select="file"/>
			</xsl:variable>
			<xsl:variable name="title">
				<xsl:text>_zips/</xsl:text>
				<xsl:value-of select="file"/>
			</xsl:variable>
			<li>
				<a href="{$ref}" title="{$title}">
					<xsl:value-of select="$desc"/>
				</a>
			</li>
		</xsl:if>
		<xsl:if test="(@type = 'local') and (../../mediatype = 'binary') and (contains(file,'.zip'))">
			<xsl:variable name="ref">
				<xsl:value-of select="$itemdir"/>
				<xsl:value-of select="file"/>
			</xsl:variable>
			<xsl:variable name="title">
				<xsl:value-of select="file"/>
			</xsl:variable>
			<li>
				<a href="{$ref}" title="{$title}">
					<xsl:value-of select="$desc"/>
				</a>
			</li>
		</xsl:if>

	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="breaker">
		<xsl:param name="src"/>
		<xsl:variable name="max-len">28</xsl:variable>
		<xsl:variable name="str" select="normalize-space(translate($src,'_',' '))"/>
		<xsl:choose>
			<xsl:when test="string-length($str) &gt; $max-len">
				<xsl:choose>
					<xsl:when test="contains($str,' ')">
						<xsl:value-of select="concat(substring-before($str,' '),' ')"/>
						<xsl:call-template name="breaker">
							<xsl:with-param name="src" select="substring-after($str,' ')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(substring($str,1,$max-len),' ')"/>
						<xsl:call-template name="breaker">
							<xsl:with-param name="src" select="substring($str,($max-len + 1))"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$str"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="resources">
		<div id="resources">
			<div class="itemdescribe">
				<span>Additional Information:</span>
			</div>
		</div>
		<div id="resource-bloc">
			<dl>
				<xsl:if test="OPDF/BC_Program_Name">
					<dt>Program:</dt>
					<dd>
						<xsl:choose>
							<xsl:when test="normalize-space(translate(OPDF/BC_Program_Name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')) = 'none'">
								<xsl:value-of select="OPDF/BC_Program_Name"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="qs">
									<xsl:value-of select="$PS_PROGRAM_1"/>
									<xsl:value-of select="normalize-space(OPDF/BC_Program_Name)"/>
									<xsl:value-of select="$PS_PROGRAM_2"/>
								</xsl:variable>
								<xsl:variable name="uri">
									<xsl:value-of select="$POWER_SEARCH_ROOT"/>
									<xsl:value-of select="$qs"/>
								</xsl:variable>

								<a href="{$uri}" title="find other items from this program">
									<xsl:value-of select="normalize-space(OPDF/BC_Program_Name)"/>
								</a>
							</xsl:otherwise>
						</xsl:choose>
					</dd>
				</xsl:if>
				<dt>Resource Type:</dt>
				<dd>
					<xsl:choose>
						<xsl:when test="count(lom/educational/learningresourcetype/value) &gt; 1">
							<ul>
								<xsl:apply-templates select="lom/educational/learningresourcetype/value"/>
							</ul>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="normalize-space(lom/educational/learningresourcetype/value)"/>
						</xsl:otherwise>
					</xsl:choose>

				</dd>
				<xsl:if test="string-length(lom/educational/typicalagerange) &gt; 0">
					<dt>Typical Year:</dt>
					<dd>
						<xsl:choose>
							<xsl:when test="count(lom/educational/typicalagerange) &gt; 1">
								<ul>
									<xsl:apply-templates select="lom/educational/typicalagerange"/>
								</ul>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="normalize-space(lom/educational/typicalagerange)"/>
							</xsl:otherwise>
						</xsl:choose>
					</dd>
				</xsl:if>
				<xsl:if test="string-length(lom/educational/typicallearningtime) &gt; 0">
					<dt>Typical Time<br/>to use resource:</dt>
					<dd>
						<xsl:value-of select="lom/educational/typicallearningtime"/>
					</dd>
				</xsl:if>

				<xsl:if test="string-length(lom/technical/format) &gt; 0">
					<dt>Technical Format:</dt>
					<dd>
						<xsl:value-of select="lom/technical/format"/>
					</dd>
				</xsl:if>
				<xsl:if test="string-length(lom/technical/duration) &gt; 0">
					<dt>Duration:</dt>
					<dd>
						<xsl:value-of select="lom/technical/duration"/>
					</dd>
				</xsl:if>
				<xsl:if test="string-length(lom/technical/otherplatformrequirements) &gt; 0">
					<dt>Other Platform:<br/>Requirements</dt>
					<dd>
						<xsl:value-of select="lom/technical/otherplatformrequirements"/>
					</dd>
				</xsl:if>
			</dl>
		</div>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="lom/educational/learningresourcetype/value">
		<li>
			<xsl:value-of select="normalize-space(.)"/>
			<span>&#32;</span>
		</li>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="lom/educational/typicalagerange">
		<li>
			<xsl:value-of select="normalize-space(.)"/>
			<span>&#32;</span>
		</li>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="contributordetails/institution">
		<xsl:variable name="imgLoc">
			<xsl:value-of select="$imgPath"/>/institutional logos/<xsl:value-of
            select="normalize-space(.)"/>.gif</xsl:variable>
		<xsl:element name="img">
			<xsl:attribute name="src">
				<xsl:value-of select="$imgLoc"/>
			</xsl:attribute>
			<xsl:attribute name="alt">
				<xsl:value-of select="normalize-space(.)"/>
			</xsl:attribute>
		</xsl:element>
		<br/>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/subject_class_level1">
		<xsl:variable name="qs">
			<xsl:value-of select="$PS_SUBJECT_11"/>
				<xsl:call-template name="url-encoder">
					<xsl:with-param name="src-url" select="normalize-space(.)"/>
				</xsl:call-template>
			<xsl:value-of select="$PS_SUBJECT_12"/>
		</xsl:variable>
		<xsl:variable name="uri">
			<xsl:value-of select="$POWER_SEARCH_ROOT"/>
			<xsl:value-of select="$qs"/>
		</xsl:variable>

		<li>
			<a href="{$uri}">More from <xsl:value-of select="normalize-space(.)"/>
			</a>
		</li>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/subject_class_level2">
		<xsl:variable name="qs">
			<xsl:value-of select="$PS_SUBJECT_11"/>
			<xsl:call-template name="url-encoder">
				<xsl:with-param name="src-url" select="normalize-space(.)"/>
			</xsl:call-template>
			<xsl:value-of select="$PS_SUBJECT_12"/>
		</xsl:variable>
		<xsl:variable name="uri">
			<xsl:value-of select="$POWER_SEARCH_ROOT"/>
			<xsl:value-of select="$qs"/>
		</xsl:variable>

		<li>
			<a href="{$uri}">More from <xsl:value-of select="normalize-space(.)"/>
			</a>
		</li>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="item/subject_class_level2b">
		<xsl:variable name="qs">
			<xsl:value-of select="$PS_SUBJECT_11"/>
			<xsl:call-template name="url-encoder">
				<xsl:with-param name="src-url" select="normalize-space(.)"/>
			</xsl:call-template>
			<xsl:value-of select="$PS_SUBJECT_12"/>
		</xsl:variable>
		<xsl:variable name="uri">
			<xsl:value-of select="$POWER_SEARCH_ROOT"/>
			<xsl:value-of select="$qs"/>
		</xsl:variable>

		<li>
			<a href="{$uri}">More from <xsl:value-of select="normalize-space(.)"/>
			</a>
		</li>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template match="lom/general/keyword">

		<xsl:if test="string-length(.) &gt; 0">
			<xsl:variable name="qs">
				<xsl:value-of select="$PS_KEYWORD_1"/>
				<xsl:value-of select="normalize-space(.)"/>
				<xsl:value-of select="$PS_KEYWORD_2"/>
				<xsl:value-of select="normalize-space(.)"/>
				<xsl:value-of select="$PS_KEYWORD_3"/>
			</xsl:variable>
			<xsl:variable name="uri">
				<xsl:value-of select="$POWER_SEARCH_ROOT"/>
				<xsl:value-of select="$qs"/>
			</xsl:variable>

			<li>
				<a href="{$uri}" title="find related items">
					<xsl:value-of select="normalize-space(.)"/>
				</a>
			</li>
		</xsl:if>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="license">
		<xsl:param name="type"/>

		<xsl:choose>
			<xsl:when test="$type = $LICENSE_TYPE_CC">
				<xsl:call-template name="license-CC"/>
			</xsl:when>
			<xsl:when test="$type = $LICENSE_TYPE_GPL">
				<xsl:call-template name="license-GPL"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="license-BCC"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- *********************************************************************** -->

	<xsl:template name="license-CC">
		<div id="license-bloc">
			<p>This resource is available under a</p>
			<img>
				<xsl:attribute name="src">
					<xsl:value-of select="$imgPath"/>/license logos/cc-somerights20.gif </xsl:attribute>
			</img>
			<h4>
				<a href="http://creativecommons.org/licenses/by-sa/2.0/ca/">Creative Commons<br/>Attribution-ShareAlike<br/>
         2.0 Canada License</a>
			</h4>
			<p>
				<a href="http://creativecommons.org/licenses/by-sa/2.0/ca/">Learn more about the conditions of use granted by this license</a>
			</p>
		</div>

		<!-- **************************************************************************** -->
		<!-- Creative Commons Machine Readable Code -->
		<!-- **************************************************************************** -->
		<xsl:comment> Creative Commons License </xsl:comment>
		<xsl:comment> &lt;rdf:RDF xmlns="http://web.resource.org/cc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"&gt;
        &lt;Work rdf:about=""&gt;
        &lt;license rdf:resource="http://creativecommons.org/licenses/by-sa/2.5/ca/" /&gt;
        &lt;/Work&gt;
        &lt;License rdf:about="http://creativecommons.org/licenses/by-sa/2.5/ca/"&gt;
        &lt;permits rdf:resource="http://web.resource.org/cc/Reproduction"/&gt;
        &lt;permits rdf:resource="http://web.resource.org/cc/Distribution"/&gt;
        &lt;requires rdf:resource="http://web.resource.org/cc/Notice"/&gt;
        &lt;requires rdf:resource="http://web.resource.org/cc/Attribution"/&gt;
        &lt;permits rdf:resource="http://web.resource.org/cc/DerivativeWorks"/&gt;
        &lt;requires rdf:resource="http://web.resource.org/cc/ShareAlike"/&gt;
        &lt;/License&gt;&lt;/rdf:RDF&gt; </xsl:comment>

	</xsl:template>

	<!-- *********************************************************************** -->
	<xsl:template name="license-BCC">
		<div id="license-bloc">
			<p>
				<span class="ln">This resource is available under a</span>
				<span class="ln">
					<a href="http://solr.bccampus.ca/bcc/customer/BCcommons/publish/bccommons_readable.html">BC COMMONS LICENSE (Version 1.2)</a>
				</span>
			</p>
			<img>
				<xsl:attribute name="src">
					<xsl:value-of select="$imgPath"/>license logos/BC-Ccommons.gif</xsl:attribute>
			</img>
			<xsl:if test="OPDF/third_party_content = 'Yes'">
				<div id="opdf3party">
					<strong>NOTE:</strong> this resource contains 3<sup>rd</sup> 
					party copyrighted materials which may need to be cleared before 
					further reuse.
				</div>
			</xsl:if>
			<p>
				<a href="http://solr.bccampus.ca/bcc/customer/BCcommons/publish/bccommons_readable.html">Learn 
				more about the conditions of use granted by this license</a>
			</p>
		</div>
	</xsl:template>

	<!-- *********************************************************************** -->

	<xsl:template name="license-BCC_X">
		<div class="itemdescribe" id="license-bloc">
			<p>This resource is available under a</p>
			<img>
				<xsl:attribute name="src">
					<xsl:value-of select="$imgPath"/>license logos/BC-Ccommons.gif </xsl:attribute>
			</img>
			<h4>
				<a href="http://solr.bccampus.ca/bcc/customer/BCcommons/publish/bccommons_readable.html">BC COMMONS LICENSE (Version 1.2)</a>
			</h4>
			<xsl:if test="OPDF/third_party_content = 'Yes'">
				<br/>
				<strong>NOTE:</strong> this resource contains 3<sup>rd</sup> party copyrighted
            materials which may need to be cleared before further reuse. </xsl:if>
			<p>
				<a href="http://solr.bccampus.ca/bcc/customer/BCcommons/publish/bccommons_readable.html">Learn about the conditions of use granted by this license.</a>
			</p>
		</div>
	</xsl:template>

	<!-- *********************************************************************** -->

	<xsl:template name="license-GPL">
		<div id="license-bloc">
			<p>This resource is available under a</p>
			<img>
				<xsl:attribute name="src">
					<xsl:value-of select="$imgPath"/>/license logos/cc-GPL.gif </xsl:attribute>
			</img>
			<h4>Creative Commons<br/>GNU General Public License</h4>
			<p>
				<a href="http://creativecommons.org/licenses/GPL/2.0/">Learn about the conditions of use granted by this license.</a>
			</p>
		</div>
		<!-- **************************************************************************** -->
		<!-- Creative Commons Machine Readable Code -->
		<!-- **************************************************************************** -->

		<xsl:comment> Creative Commons License </xsl:comment>
		<xsl:comment> 
        &lt;rdf:RDF xmlns="http://web.resource.org/cc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"&gt;
            &lt;Work rdf:about=""&gt;
                &lt;license rdf:resource="http://creativecommons.org/licenses/GPL/2.0/" /&gt;
            &lt;/Work&gt;
            &lt;License rdf:about="http://creativecommons.org/licenses/GPL/2.0/"&gt;
                &lt;permits rdf:resource="http://web.resource.org/cc/Reproduction"/&gt;
                &lt;permits rdf:resource="http://web.resource.org/cc/Distribution"/&gt;
                &lt;requires rdf:resource="http://web.resource.org/cc/Notice"/&gt;
                &lt;requires rdf:resource="http://web.resource.org/cc/SourceCode"/&gt;
                &lt;permits rdf:resource="http://web.resource.org/cc/DerivativeWorks"/&gt;
                &lt;requires rdf:resource="http://web.resource.org/cc/ShareAlike"/&gt;
            &lt;/License&gt;
        &lt;/rdf:RDF&gt; 
		</xsl:comment>
	</xsl:template>



	<!-- *********************************************************************** -->
	<!-- encodeURL.xsl -->
	<!-- *********************************************************************** -->

	<xsl:template name="url-encode">
		<xsl:param name="src-url" />

		<xsl:variable name="par-url">
			<xsl:call-template name="url-encoder">
				<xsl:with-param name="src-url">
					<xsl:value-of select="$src-url"/>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<xsl:value-of select="translate(normalize-space($par-url),' ','')"/>
	</xsl:template>

	<!-- ****************************************************************************-->

	<xsl:template name="url-encoder">
		<xsl:param name="src-url" />

		<xsl:variable name="nor-url">
			<xsl:value-of select="normalize-space($src-url)"/>
		</xsl:variable>
		<xsl:variable name="hex-char">0123456789ABCDEF</xsl:variable>
		<xsl:variable name="free-char">!'()*-.012356789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>
		<!-- these char's don't usually need to be escaped -->
		<xsl:variable name="ascii-char"> !"#$%&#38;'()*+,-./0123456789:;&#60;=&#62;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
		<xsl:variable name="first-char" select="substring($src-url,1,1)"/>
	
		<xsl:choose>
			<xsl:when test="contains($free-char,$first-char)">
				<xsl:value-of select="$first-char"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="codepoint">
					<xsl:choose>
						<xsl:when test="contains($ascii-char,$first-char)">
							<xsl:value-of select="string-length(substring-before($ascii-char,$first-char)) + 32"/>
						</xsl:when>
						<xsl:otherwise>63</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
			
				<xsl:variable name="hex1" select="substring($hex-char,floor(translate($codepoint,' ','') div 16) + 1,1)"/>
				<xsl:variable name="hex2" select="substring($hex-char,translate($codepoint,' ','') mod 16 + 1,1)"/>
			
				<xsl:value-of select="concat('%',$hex1,$hex2)"/>
			</xsl:otherwise>
		</xsl:choose>
	
	<xsl:if test="string-length($src-url) &gt; 1">
		<xsl:call-template name="url-encoder">
			<xsl:with-param name="src-url" select="substring($src-url,2)"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

<!-- *********************************************************************** -->

<!--

	<xsl:call-template name="t-name">
    	<xsl:with-param name="p-name">p-value</xsl:with-param>
	</xsl:call-template>

	<xsl:template name="t-name">
    	<xsl:param name="p-name" />
	</xsl:template>

-->

</xsl:stylesheet>
