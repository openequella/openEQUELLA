<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="html" media-type="text/html"/>

<!-- UoA style citation -->
<!-- From specification by Vanessa 21/5/07 and subsequent notes -->

<!--
ALGORITHM - 2 cases for XSL citation to blackboard (4 cases for display in Equella):

(Ignore parent book and journal citation cases here):

If book portion citation:
	If no ...portions/title exists and no ...portions/authors/author exists
		then construct citation according to Book parent rules and append the citation with "pages" as described below.
	Else (one of portion author(s) or portion title(s) is present):
		
		If $portionAuthors:
			Author/s of portion: item/copyright/portion/portions/authors/author
			Ampersand to join last author to previous, commas between authors except last two.
			<space> Date: item/copyright/publication/year
			<comma> <space>Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
		Else:
			Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
			<space> Date: item/copyright/publication/year:

		<space> in <space>
		
		If Publisher, Author and Editor are blank and conference name exists:
			item/copyright/conference/name
		Else
			If Author exists
				item/copyright/authors/author: Commas between authors, except last two. Ampersand to join last author to previous.
			Else if Editor exists
				item/copyright/editors/editor: Commas between editors, except last two. Ampersand to join last editor to previous.
				If Editor/s is used then insert '(ed.)' for one editor or '(eds.)' for more than one
			If parentTitle
				Title of parent: item/copyright/title: Italicise and bold
		If $holdingEdition
			Edition: item/copyright/edition
		If $holdingPublisher
			Publisher: item/copyright/publisher
		If Place of publication<>''
			item/copyright/publication/place
		Else if conference name='' and not (publisher='' and editor='' and author=''):
			'n.p.'
		If Pages: item/copyright/portion/portions/section/sections/pages
			Prefix page range , or number in square brackets, with 'pp.' or single page with 'p.'
		If Publisher, Author and Editor are blank and conference name exists:
			Conference location: item/copyright/conference/location
			Conference date: item/copyright/conference/year

If journal portion citation:
	If $portionAuthors (authors of article):
		Author/s of portion: item/copyright/portion/portions/authors/author
		Ampersand to join last author to previous, commas between authors except last two.
		<space> Date: item/copyright/publication/year
		<comma> <space>Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
	Else:
		Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
		<space> Date: item/copyright/publication/year:
	If Title of parent
		item/copyright/title: italic and bold
	If Volume
		item/copyright/volume: Precede with 'vol.'
	If item/copyright/issue/type = number 
		Issue (number): item/copyright/issue/value: Precede with 'no.'
	Else if item/copyright/issue/type = date
		Issue (date): item/copyright/issue/value ?? manipulation?
	If Pages: item/copyright/portion/portions/section/sections/pages
		Prefix page range , or number in square brackets, with 'pp.' or single page with 'p.'

PUNCTUATION:
Separate the first element and the date with space.
After that separate date and each subsequent element from the next element with a comma and space. End citation with a full stop 
-->

    <xsl:template match="/">
	  <xsl:apply-templates select="/xml/item/copyright">
	  	<xsl:with-param name="section" select="/xml/item/copyright/portions/portion/sections/section[attachment = /xml/request/attachment]"/>
	  </xsl:apply-templates>
    </xsl:template>
	
	 <xsl:template match="/xml/item/copyright">
		<xsl:param name="section"/>
		<xsl:variable name="portion" select="$section/../.."/>
<!--
		<xsl:variable name="debug" select="'Yes'"/>
-->
		<xsl:variable name="debug" select="'No'"/>
		<!-- Set up strings/variables -->
		<xsl:variable name="sectionPages">
			<xsl:choose>
				<xsl:when test="(contains($section/pages,'-')) or (contains($section/pages,'['))">
					<xsl:text>pp. </xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>p. </xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="$section/pages"/>
		</xsl:variable>

		<xsl:variable name="portionAuthors">
			<xsl:for-each select="$portion/authors/author">
				<xsl:if test="position() > 1 and (not(position() = last())) and position() ">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(not(position() = 1)) and (position() = (last())) and position() ">
					<xsl:text> &amp; </xsl:text>
				</xsl:if>
				<xsl:value-of select="."/>
			</xsl:for-each>
		</xsl:variable>

		<xsl:variable name="portionTitle">
			<xsl:text>'</xsl:text>
			<xsl:value-of select="$portion/title"/>
			<xsl:text>'</xsl:text>
		</xsl:variable>

		<xsl:variable name="holdingTitle">
			<xsl:value-of select="/xml/holding/xml/item/copyright/title"/>
		</xsl:variable>

		<xsl:variable name="holdingAuthors">
			<xsl:for-each select="/xml/holding/xml/item/copyright/authors/author">
				<xsl:if test="position() > 1 and (not(position() = last())) and position() ">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(not(position() = 1)) and (position() = (last())) and position() ">
					<xsl:text> &amp; </xsl:text>
				</xsl:if>
				<xsl:value-of select="."/>
			</xsl:for-each>
		</xsl:variable>

		<xsl:variable name="holdingNumEditors">
			<xsl:for-each select="/xml/holding/xml/item/copyright/editors/editor">
				<xsl:if test="position() = (last())">
					<xsl:value-of select="position()"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>

		<xsl:variable name="holdingEditors">
			<xsl:for-each select="/xml/holding/xml/item/copyright/editors/editor">
				<xsl:if test="position() > 1 and (not(position() = last())) and position() ">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:if test="(not(position() = 1)) and (position() = (last())) and position() ">
					<xsl:text> &amp; </xsl:text>
				</xsl:if>
				<xsl:value-of select="."/>
			</xsl:for-each>
			<xsl:if test="$holdingNumEditors = 1">
				<xsl:text> (ed.)</xsl:text>
			</xsl:if>
			<xsl:if test="$holdingNumEditors > 1">
				<xsl:text> (eds.)</xsl:text>
			</xsl:if>
		</xsl:variable>

		<xsl:variable name="holdingPublishers">
			<xsl:for-each select="/xml/holding/xml/item/copyright/publisher">
				<xsl:if test="position() > 1">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<xsl:value-of select="."/>
			</xsl:for-each>
		</xsl:variable>

		<xsl:variable name="holdingEdition">
			<xsl:value-of select="/xml/holding/xml/item/copyright/edition"/>
		</xsl:variable>

		<xsl:variable name="holdingPublicationYear">
			<xsl:choose>
				<xsl:when test="/xml/holding/xml/item/copyright/publication/year!=''">
					<xsl:text> </xsl:text>
					<xsl:value-of select="/xml/holding/xml/item/copyright/publication/year"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text> n.d.</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="holdingPublicationPlace">
			<xsl:value-of select="/xml/holding/xml/item/copyright/publication/place"/>
		</xsl:variable>

		<xsl:variable name="holdingIssue">
			<xsl:value-of select="/xml/holding/xml/item/copyright/issue/value"/>
		</xsl:variable>

		<xsl:variable name="holdingVolume">
			<xsl:if test="/xml/holding/xml/item/copyright/volume!=''">
				<xsl:text>vol. </xsl:text>
				<xsl:value-of select="/xml/holding/xml/item/copyright/volume"/>
			</xsl:if>
		</xsl:variable>

		<xsl:variable name="holdingConferenceName">
			<xsl:value-of select="/xml/holding/xml/item/copyright/conference/name"/>
		</xsl:variable>

		<xsl:variable name="holdingConferenceLocation">
			<xsl:value-of select="/xml/holding/xml/item/copyright/conference/location"/>
		</xsl:variable>

		<xsl:variable name="holdingConferenceDate">
			<xsl:value-of select="/xml/holding/xml/item/copyright/conference/year"/>
		</xsl:variable>

		<xsl:if test="$debug='Yes'">
			Section: "<xsl:value-of select="$section"/>"<p/>
			Portion: "<xsl:value-of select="$portion"/>"<p/>
			Holding: "<xsl:value-of select="/xml/holding"/>"<p/>
			
			Section Page Range raw: "<xsl:value-of select="$section/pages"/>"<br/>
			Section Page Range: "<xsl:value-of select="$sectionPages"/>"<br/>
	
			Portion Authors raw: "<xsl:value-of select="$portion/authors/author"/>"<br/>
			Portion Authors: "<xsl:value-of select="$portionAuthors"/>"<br/>
			Portion Title raw: "<xsl:value-of select="$portion/title"/>"<br/>
			Portion Title: "<xsl:value-of select="$portionTitle"/>"<p/>
			Portion ParentType: "<xsl:value-of select="/xml/item/copyright/@parenttype"/>"<p/>
	
			Holding Conference Name raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/conference/name"/>"<br/>
			Holding Conference Name: "<xsl:value-of select="$holdingConferenceName"/>"<br/>
			Holding Conference Location raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/conference/location"/>"<br/>
			Holding Conference Location: "<xsl:value-of select="$holdingConferenceLocation"/>"<br/>
			Holding Conference Date raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/conference/year"/>"<br/>
			Holding Conference Date: "<xsl:value-of select="$holdingConferenceDate"/>"<br/>
			Holding Title raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/title"/>"<br/>
			Holding Title: "<xsl:value-of select="$holdingTitle"/>"<br/>
			Holding Editors raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/editors/editor"/>"<br/> 
			Holding Editors: "<xsl:value-of select="$holdingEditors"/>"<br/> 
			Holding Num Editors: "<xsl:value-of select="$holdingNumEditors"/>"<br/> 
			Holding Authors raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/authors/author"/>"<br/> 
			Holding Authors: "<xsl:value-of select="$holdingAuthors"/>"<br/> 
			Holding Edition raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/edition"/>"<br/> 
			Holding Edition: "<xsl:value-of select="$holdingEdition"/>"<br/> 
			Holding Publishers raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/publisher"/>"<br/> 
			Holding Publishers: "<xsl:value-of select="$holdingPublishers"/>"<br/> 
			Holding Publication Year raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/publication/year"/>"<br/> 
			Holding Publication Year: "<xsl:value-of select="$holdingPublicationYear"/>"<br/> 
			Holding Place of Publication raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/publication/place"/>"<br/> 
			Holding Place of Publication: "<xsl:value-of select="$holdingPublicationPlace"/>"<br/> 
			Holding Issue raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/issue/value"/>"<br/> 
			Holding Issue: "<xsl:value-of select="$holdingIssue"/>"<br/> 
			Holding Volume raw: "<xsl:value-of select="/xml/holding/xml/item/copyright/volume"/>"<br/> 
			Holding Volume: "<xsl:value-of select="$holdingVolume"/>"<br/> 
		</xsl:if>

<!-- ************************************************************************************************************** -->

<!--
If a book portion citation
-->
		<xsl:if test="/xml/item/copyright/@parenttype='Book'">
			<xsl:if test="$debug='Yes'">Book chapter citation: </xsl:if>
			<xsl:choose>
				<!--
					If $portionAuthors:
						Author/s of portion: item/copyright/portion/portions/authors/author
						Ampersand to join last author to previous, commas between authors except last two.
						<space> Date: item/copyright/publication/year
						<comma> <space>Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
				-->
				<xsl:when test="$portion/authors/author">
					<xsl:if test="$debug='Yes'">Authors first: </xsl:if>
					<xsl:value-of select="$portionAuthors"/>
					<xsl:value-of select="$holdingPublicationYear"/>
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$portionTitle"/>
				</xsl:when>
				<!--
					Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
					<space> Date: item/copyright/publication/year:
				-->
				<xsl:when test="($portionAuthors='') and $portion/title">
					<xsl:if test="$debug='Yes'">Title first, no authors: </xsl:if>
					<xsl:value-of select="$portionTitle"/>
					<xsl:value-of select="$holdingPublicationYear"/>
				</xsl:when>
				<!--
					If no ...portions/title exists and no ...portions/authors/author exists
						then construct citation according to Book parent rules and append the citation with "pages" as described below.
				-->
				<xsl:otherwise>
					<xsl:if test="$debug='Yes'">No portion authors or title: Book Citation style: </xsl:if>
					<xsl:choose>
						<!--
							If Publisher, Author and Editor are blank
								item/copyright/conference/name
						-->
						<xsl:when test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name">
							<xsl:value-of select="$holdingConferenceName"/>
							<xsl:value-of select="$holdingPublicationYear"/>
						</xsl:when>
						<!--
							If Author exists
								item/copyright/authors/author: Commas between authors, except last two. Ampersand to join last author to previous.
						-->
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="/xml/holding/xml/item/copyright/authors/author">
									<xsl:value-of select="$holdingAuthors"/>
									<xsl:value-of select="$holdingPublicationYear"/>
								</xsl:when>
								<xsl:otherwise>
								<!--
									Else if Editor exists
										item/copyright/editors/editor: Commas between editors, except last two. Ampersand to join last editor to previous.
										If Editor/s is used then insert '(ed.)' for one editor or '(eds.)' for more than one
								-->
									<xsl:if test="/xml/holding/xml/item/copyright/editors/editor">
										<xsl:value-of select="$holdingEditors"/>
										<xsl:value-of select="$holdingPublicationYear"/>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
					<!--
						If no author, editor or conference name then date should follow, instead of preceding title. 
						If not $conferenceName
							Title of parent: item/copyright/title: Italicise and bold
					-->
					<xsl:choose>
						<xsl:when test="($holdingAuthors='') and ($holdingEditors='') and ($holdingConferenceName='') and /xml/holding/xml/item/copyright/title">
							<b><i><xsl:value-of select="$holdingTitle"/></i></b>
							<xsl:value-of select="$holdingPublicationYear"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="($holdingConferenceName='') and /xml/holding/xml/item/copyright/title">
								<xsl:text>, </xsl:text>
								<b><i><xsl:value-of select="$holdingTitle"/></i></b>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					<!--
						If $holdingEdition
							Edition: item/copyright/edition
					-->
					<xsl:if test="/xml/holding/xml/item/copyright/edition!=''">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$holdingEdition"/>
					</xsl:if>
					<!--
						If $holdingPublisher
							Publisher: item/copyright/publisher
					-->
					<xsl:if test="/xml/holding/xml/item/copyright/publisher!=''">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$holdingPublishers"/>
					</xsl:if>
					<!--
						If Place of publication<>''
							item/copyright/publication/place
						Else if conference name='' and not (publisher='' and editor='' and author=''):
							'n.p.'
					-->
					<xsl:choose>
						<xsl:when test="/xml/holding/xml/item/copyright/publication/place!=''">
							<xsl:text>, </xsl:text>
							<xsl:value-of select="$holdingPublicationPlace"/>
						</xsl:when>
						<xsl:otherwise>
                            <xsl:if test="$holdingPublishers!='' and $holdingAuthors!='' and $holdingEditors!='' and /xml/holding/xml/item/copyright/conference/name!=''">
							<!--<xsl:if test="not(($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name)">-->
								<xsl:text>, n.p.</xsl:text>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					<!--
						If Publisher, Author and Editor are blank and conference name exists:
							Conference location: item/copyright/conference/location
							Conference date: item/copyright/conference/year
					-->
					<xsl:if test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name and /xml/holding/xml/item/copyright/conference/location">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$holdingConferenceLocation"/>
					</xsl:if>
					<xsl:if test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name and /xml/holding/xml/item/copyright/conference/year">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$holdingConferenceDate"/>
					</xsl:if>
					<!--
						Pages: item/copyright/portion/portions/section/sections/pages
							Prefix page range , or number in square brackets, with 'pp.' or single page with 'p.'
					-->
					<xsl:if test="$section/pages!=''">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$sectionPages"/>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<!-- rest of portion details -->
			<xsl:if test="$portion/authors/author or $portion/title">
				<xsl:text>, in </xsl:text>
				<xsl:choose>
					<!--
						If Publisher, Author and Editor are blank
							item/copyright/conference/name
					-->
					<xsl:when test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name">
						<xsl:value-of select="$holdingConferenceName"/>
					</xsl:when>
					<!--
						If Author exists
							item/copyright/authors/author: Commas between authors, except last two. Ampersand to join last author to previous.
					-->
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when test="/xml/holding/xml/item/copyright/authors/author">
								<xsl:value-of select="$holdingAuthors"/>
							</xsl:when>
							<xsl:otherwise>
							<!--
								Else if Editor exists
									item/copyright/editors/editor: Commas between editors, except last two. Ampersand to join last editor to previous.
									If Editor/s is used then insert '(ed.)' for one editor or '(eds.)' for more than one
							-->
								<xsl:if test="/xml/holding/xml/item/copyright/editors/editor">
									<xsl:value-of select="$holdingEditors"/>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
						<!--
							If not $conferenceName
								Title of parent: item/copyright/title: Italicise and bold
						-->
						<xsl:if test="/xml/holding/xml/item/copyright/title!=''">
							<xsl:if test="/xml/holding/xml/item/copyright/editors/editor!='' or /xml/holding/xml/item/copyright/authors/author!=''">
								<xsl:text>, </xsl:text>
							</xsl:if>
							<b><i><xsl:value-of select="$holdingTitle"/></i></b>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
				<!--
					If $holdingEdition
						Edition: item/copyright/edition
				-->
				<xsl:if test="/xml/holding/xml/item/copyright/edition!=''">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$holdingEdition"/>
				</xsl:if>
				<!--
					If $holdingPublisher
						Publisher: item/copyright/publisher
				-->
				<xsl:if test="/xml/holding/xml/item/copyright/publisher!=''">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$holdingPublishers"/>
				</xsl:if>
				
				<!--
					If Place of publication<>''
						item/copyright/publication/place
					Else if conference name='' and not (publisher='' and editor='' and author=''):
						'n.p.'
				-->
				<xsl:choose>
					<xsl:when test="/xml/holding/xml/item/copyright/publication/place!=''">
						<xsl:text>, </xsl:text>
						<xsl:value-of select="$holdingPublicationPlace"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$holdingPublishers!='' and $holdingAuthors!='' and $holdingEditors!='' and /xml/holding/xml/item/copyright/conference/name!=''">
                        <!--<xsl:if test="not(($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name)">-->
							<xsl:text>, n.p.</xsl:text>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
				<!--
					Pages: item/copyright/portion/portions/section/sections/pages
						Prefix page range , or number in square brackets, with 'pp.' or single page with 'p.'
				-->
				<xsl:if test="$section/pages!=''">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$sectionPages"/>
				</xsl:if>
				<!--
					If Publisher, Author and Editor are blank and conference name exists:
						Conference location: item/copyright/conference/location
						Conference date: item/copyright/conference/year
				-->
				<xsl:if test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name!='' and /xml/holding/xml/item/copyright/conference/location!=''">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$holdingConferenceLocation"/>
				</xsl:if>
				<xsl:if test="($holdingPublishers='') and ($holdingAuthors='') and ($holdingEditors='') and /xml/holding/xml/item/copyright/conference/name!='' and /xml/holding/xml/item/copyright/conference/year!=''">
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$holdingConferenceDate"/>
				</xsl:if>
			</xsl:if>
		</xsl:if>

<!-- ************************************************************************************************************** -->

<!--
If a journal portion citation
-->
		<xsl:if test="/xml/item/copyright/@parenttype='Journal'">
			<xsl:if test="$debug='Yes'">Journal article citation: </xsl:if>
			<xsl:choose>
				<!--
					If $portionAuthors (authors of article):
						Author/s of portion: item/copyright/portion/portions/authors/author
						Ampersand to join last author to previous, commas between authors except last two.
						<space> Date: item/copyright/publication/year
						<comma> <space>Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
				-->
				<xsl:when test="$portion/authors/author">
					<xsl:if test="$debug='Yes'">Authors first: </xsl:if>
					<xsl:value-of select="$portionAuthors"/>
					<xsl:value-of select="$holdingPublicationYear"/>
					<xsl:text>, </xsl:text>
					<xsl:value-of select="$portionTitle"/>
				</xsl:when>
				<!--
					Else:
						Title of portion: item/copyright/portion/portions/title: Place title in single quotation marks
						<space> Date: item/copyright/publication/year:
				-->
				<xsl:when test="($portionAuthors='') and $portion/title">
					<xsl:if test="$debug='Yes'">Title first, no authors: </xsl:if>
					<xsl:value-of select="$portionTitle"/>
					<xsl:value-of select="$holdingPublicationYear"/>
				</xsl:when>
			</xsl:choose>
			<!--
				If Title of parent
					item/copyright/title: italic and bold
			-->
			<xsl:if test="/xml/holding/xml/item/copyright/title!=''">
				<xsl:if test="$portion/authors/author or $portion/title">
					<xsl:text>, </xsl:text>
				</xsl:if>
				<b><i><xsl:value-of select="$holdingTitle"/></i></b>
			</xsl:if>
			<!--
				If Volume
					item/copyright/volume: Precede with 'vol.'
			-->
			<xsl:if test="/xml/holding/xml/item/copyright/volume!=''">
				<xsl:text>, </xsl:text>
				<xsl:value-of select="$holdingVolume"/>
			</xsl:if>
			<!--
				If item/copyright/issue/type = number 
					Issue (number): item/copyright/issue/value: Precede with 'no.'
			-->
			<xsl:if test="(/xml/holding/xml/item/copyright/issue/type='number') and /xml/holding/xml/item/copyright/issue/value!=''">
				<xsl:text>, no. </xsl:text>
				<xsl:value-of select="$holdingIssue"/>
			</xsl:if>
			<!--
				Else if item/copyright/issue/type = date
					Issue (date): item/copyright/issue/value
			-->
			<xsl:if test="(/xml/holding/xml/item/copyright/issue/type='date') and /xml/holding/xml/item/copyright/issue/value!=''">
				<xsl:text>, </xsl:text>
				<!-- <xsl:value-of select="$holdingIssue"/> Removed - 30 Jul 08 by Carl -->
                
                <!-- Transform date from:  YYYY-MM-DDTHH:MM:SS     style to:    DD Month -->
                
                <xsl:variable name="month" select="substring($holdingIssue,6,2)" />
                <xsl:variable name="day" select="substring($holdingIssue,9,2)" />
                <xsl:value-of select="$day" />
                <xsl:text> </xsl:text>
                
                <xsl:if test="contains ($month,'01')">
                    <xsl:text>Jan</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'02')">
                    <xsl:text>Feb</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'03')">
                    <xsl:text>Mar</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'04')">
                    <xsl:text>Apr</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'05')">
                    <xsl:text>May</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'06')">
                    <xsl:text>Jun</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'07')">
                    <xsl:text>Jul</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'08')">
                    <xsl:text>Aug</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'09')">
                    <xsl:text>Sep</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'10')">
                    <xsl:text>Oct</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'11')">
                    <xsl:text>Nov</xsl:text>
                </xsl:if>
                <xsl:if test="contains ($month,'12')">
                    <xsl:text>Dec</xsl:text>
                </xsl:if>
                
			</xsl:if>
			<!--
				If Pages: item/copyright/portion/portions/section/sections/pages
					Prefix page range , or number in square brackets, with 'pp.' or single page with 'p.'
			-->
			<xsl:if test="$section/pages!=''">
				<xsl:text>, </xsl:text>
				<xsl:value-of select="$sectionPages"/>
			</xsl:if>
		</xsl:if>
		<!-- Always end with a '.' -->
		<xsl:text>.</xsl:text>
    </xsl:template>
</xsl:stylesheet>