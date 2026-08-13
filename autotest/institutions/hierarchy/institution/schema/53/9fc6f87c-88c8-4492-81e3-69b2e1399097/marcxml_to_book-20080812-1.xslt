<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes"/>

	<!-- Don't display non-matching data -->
	<xsl:template match="varfield">
	</xsl:template>

    <!-- Recursive function to strip trailing characters:  .,/ and whitespace -->
    
    <xsl:template name="StripRight1">
        <xsl:param name="strInput" select="''"/>
        <xsl:variable name="strLen" select="string-length($strInput)" />
        <xsl:variable name="strOutput" select="substring($strInput, 1, $strLen - 1)" />
        <xsl:variable name="strLastChar" select="substring($strInput, $strLen, 1)" />

        <xsl:choose>
            <xsl:when test="$strLastChar = ' ' or $strLastChar = '.' or $strLastChar = ',' or $strLastChar = '/'">
                <xsl:call-template name="StripRight1">
                    <xsl:with-param name="strInput" select="$strOutput"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$strInput" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Recursive function to strip trailing characters:  :;, and whitespace -->    
    
    <xsl:template name="StripRight2">
        <xsl:param name="strInput" select="''"/>
        <xsl:variable name="strLen" select="string-length($strInput)" />
        <xsl:variable name="strOutput" select="substring($strInput, 1, $strLen - 1)" />
        <xsl:variable name="strLastChar" select="substring($strInput, $strLen, 1)" />

        <xsl:choose>
            <xsl:when test="$strLastChar = ' ' or $strLastChar = ';' or $strLastChar = ',' or $strLastChar = ':'">
                <xsl:call-template name="StripRight2">
                    <xsl:with-param name="strInput" select="$strOutput"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$strInput" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
	<!-- ISBN 20.a, push (new array entry in meta-data element) -->
	<xsl:template match="varfield[@id = '020']">
		<xsl:for-each select="subfield[@label = 'a']">
			<isbn>
				<xsl:choose>
					<xsl:when test="substring(.,string-length(.))=':'">
						<xsl:value-of select="substring(.,1,string-length(.)-1)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</isbn>
		</xsl:for-each>
	</xsl:template>

	<!-- Author(s) 100.a, 110.a + 110.b, 700.a + 710.b, or 710.a, push (new array entry in meta-data element)  -->
	<xsl:template match="varfield[@id = '100' or @id = '700' or @id = '710']">
		<xsl:for-each select="subfield[@label = 'a']">
			<author>
				<xsl:choose>
					<xsl:when test="substring(.,string-length(.))=','">
						<xsl:value-of select="substring(.,1,string-length(.)-1)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</author>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="varfield[@id = '110']">
		<author>
			<xsl:variable name="cauth">
				<xsl:value-of select="subfield[@label = 'a']"/>
				<xsl:for-each select="subfield[@label = 'b']">
					<xsl:text> </xsl:text>
					<xsl:value-of select="."/>
				</xsl:for-each>
			</xsl:variable>
			<xsl:value-of select="$cauth"/>
		</author>
	</xsl:template>
	<xsl:template match="varfield[@id = '710']">
		<author>
			<xsl:variable name="cauth">
				<xsl:value-of select="subfield[@label = 'a']"/>
				<xsl:for-each select="subfield[@label = 'b']">
					<xsl:text> </xsl:text>
					<xsl:value-of select="."/>
				</xsl:for-each>
			</xsl:variable>
			<xsl:value-of select="$cauth"/>
		</author>
	</xsl:template>

	<!-- Conference Name 111.a or 711.a, write (populate meta-data element) -->
	<!-- Conference Location 111.c or 711.c, write (populate meta-data element) -->
	<!-- Conference Sect 111.n or 711.n, write (populate meta-data element) -->
	<!-- Conference Date 111.d or 711.d, write (populate meta-data element) -->
	<xsl:template match="varfield[@id = '111' or @id = '711']">
		<conference>
			<xsl:if test="subfield[@label = 'a'] != ''">
				<name>
					<xsl:value-of select="subfield[@label = 'a']"/>
				</name>
			</xsl:if>
			<xsl:if test="subfield[@label = 'c'] != ''">
				<location>
					<xsl:value-of select="subfield[@label = 'c']"/>
				</location>
			</xsl:if>
			<xsl:if test="subfield[@label = 'n'] != ''">
				<number>
					<xsl:value-of select="subfield[@label = 'n']"/>
				</number>
			</xsl:if>
			<xsl:if test="subfield[@label = 'd'] != ''">
				<xsl:for-each select="subfield[@label = 'd']">
					<year>
						<xsl:choose>
							<xsl:when test="substring(.,string-length(.))='.'">
								<xsl:value-of select="substring(.,1,string-length(.)-1)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</year>
				</xsl:for-each>
			</xsl:if>
		</conference>
	</xsl:template>

	<!-- Title 245.a, 245.b, 245.n and 245.p separated by spaces and with trailing "/", "," or "." removed from aggregate -->
	<!-- Responsibility 245.c, write (populate meta-data element)  -->
    
	<xsl:template match="varfield[@id = '245']">
		<xsl:if test="subfield[@label = 'c']">
			<responsibility>
				<xsl:value-of select="subfield[@label = 'c']"/>
			</responsibility>
		</xsl:if>
        
		<xsl:if test="(subfield[@label = 'a']) or (subfield[@label = 'b']) or (subfield[@label = 'n']) or (subfield[@label = 'p'])">
            <xsl:variable name="ctitle">
				<xsl:value-of select="subfield[@label = 'a']"/>
				<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 'b'] != '')">
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:value-of select="subfield[@label = 'b']"/>
				<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'b'] != '')) and (subfield[@label = 'n'] != '')">
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:value-of select="subfield[@label = 'n']"/>
				<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'b'] != '') or (subfield[@label = 'n'] != '')) and (subfield[@label = 'p'] != '')">
					<xsl:text> </xsl:text>
				</xsl:if>
				<xsl:value-of select="subfield[@label = 'p']"/>
			</xsl:variable>
            
			<title>
                <xsl:call-template name="StripRight1">
                    <xsl:with-param name="strInput" select="$ctitle"/>
                </xsl:call-template>
			</title>
		</xsl:if>
	</xsl:template>

	<!-- Edition 250.a, write (populate meta-data element)  -->
	<xsl:template match="varfield[@id = '250'][subfield[@label = 'a']]">
		<edition>
			<xsl:value-of select="subfield[@label = 'a']"/>
		</edition>
	</xsl:template>

	<!-- Publication place 260.a (with trailing ":", ";" or "," removed) and year 260.c (with trailing "." removed), write (populate meta-data element) -->
	<!-- Publisher 260.b , write (populate meta-data element) with trailing ":", ";" or "," removed -->
	<xsl:template match="varfield[@id = '260']">
		<xsl:for-each select="subfield[@label = 'b']">
			<publisher>
                <xsl:call-template name="StripRight2">
                    <xsl:with-param name="strInput" select="."/>
                </xsl:call-template>
			</publisher>
		</xsl:for-each>
		<publication>
			<xsl:for-each select="subfield[@label = 'a']">
				<place>
                    <xsl:call-template name="StripRight2">
                        <xsl:with-param name="strInput" select="."/>
                    </xsl:call-template>
				</place>
			</xsl:for-each>
			<xsl:if test="subfield[@label = 'c'] != ''">
				<xsl:for-each select="subfield[@label = 'c']">
					<year>
                        <xsl:choose>
							<xsl:when test="substring(.,string-length(.))='.'">
								<xsl:value-of select="substring(.,1,string-length(.)-1)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</year>
				</xsl:for-each>
			</xsl:if>
		</publication>
	</xsl:template>

	<!-- Pages 300.a (with trailing , ":", ";" or "," removed), write (populate meta-data element) -->
	<xsl:template match="varfield[@id = '300']">
		<xsl:for-each select="subfield[@label = 'a']">
			<pages>
                <xsl:call-template name="StripRight2">
                    <xsl:with-param name="strInput" select="."/>
                </xsl:call-template>
			</pages>
		</xsl:for-each>
	</xsl:template>

	<!-- Top level oai_marc mapping: set copyright item type and display node empty values plus setting abstract and internal notes -->
	<xsl:template match="oai_marc">
		<itembody>
			<name>Untitled</name>
			<description></description>
		</itembody>
		<copyright type="Book">
			<xsl:apply-templates select="varfield[@id = '020']"/>
			<authors>
				<xsl:apply-templates select="varfield[@id = '100' or @id = '110' or @id = '700' or @id = '710']"/>
			</authors>
			<xsl:apply-templates select="varfield[@id = '111' or @id = '711']"/>
			<xsl:apply-templates select="varfield[@id = '245' or @id = '246' or @id = '250' or @id = '260' or @id = '300']"/>
			<xsl:apply-templates select="varfield[@id = '500' or @id = '505' or @id = '740' or @id = '800' or @id = '810' or @id = '811']"/>
			<!-- Abstract 246.a, 440.anpvx (concatenated), 730.a and 830.ahnpv (concatenated) all appended in a single element separated with <CR> -->
			<xsl:preserve-space elements="varfield[@id = '246 or @id = '440' or @id = '500' or @id = '505' or @id = '730' or @id = '740' or @id = '800' or @id = '810' or @id = '811' or @id = '830']"/>
            <xsl:variable name="abstract">
				<xsl:variable name="abstract246">
					<xsl:for-each select="varfield[@id='246'][subfield[@label = 'a']]">
						<xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="abstract440">
					<xsl:for-each select="varfield[@id = '440']">
                        <xsl:value-of select="subfield[@label = 'a']"/>
						<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 'n'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'n']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'n'] != '')) and (subfield[@label = 'p'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'p']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'n'] != '') or (subfield[@label = 'p'] != '')) and (subfield[@label = 'v'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'v']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'n'] != '') or (subfield[@label = 'p'] != '') or (subfield[@label = 'v'] != '')) and (subfield[@label = 'x'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'x']"/>
						<xsl:text>&#xD;&#xA;</xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="abstract730">
                    <xsl:for-each select="varfield[@id='730'][subfield[@label = 'a']]">
						<xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="abstract830">             
					<xsl:for-each select="varfield[@id = '830']">
						<xsl:value-of select="subfield[@label = 'a']"/>
						<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 'h'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'h']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'h'] != '')) and (subfield[@label = 'n'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'n']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'h'] != '') or (subfield[@label = 'n'] != '')) and (subfield[@label = 'p'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'p']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'h'] != '') or (subfield[@label = 'n'] != '') or (subfield[@label = 'p'] != '')) and (subfield[@label = 'v'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'v']"/>
						<xsl:text>&#xD;&#xA;</xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<!-- set the abstract variable -->
				<xsl:if test="$abstract246 != ''">
					<xsl:value-of select = "$abstract246"/>
				</xsl:if>
				<xsl:if test="$abstract440 != ''">
					<xsl:choose>
						<xsl:when test="$abstract246 != ''">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$abstract440"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$abstract440"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$abstract730 != ''">
					<xsl:choose>
						<xsl:when test="($abstract246 != '') or ($abstract440 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$abstract730"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$abstract730"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$abstract830 != ''">
					<xsl:choose>
						<xsl:when test="($abstract246 != '') or ($abstract440 != '') or ($abstract730 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$abstract830"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$abstract830"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:variable>
			<xsl:if test="$abstract != ''">
				<abstract>
					<xsl:value-of select="$abstract"/>
				</abstract>
			</xsl:if>
			
	<!-- Internal notes 500.a, 505.a, 740.a, 800.a + 800.d + 800.t + 800.v, 810.a + 810.b + 810.t + 810.v, 811.a + 811.t + 811v
		 append (append the data to text already  present in meta-data element. Separate with <CR>.) -->
			<xsl:variable name="intnotes">
				<xsl:variable name="int500">
					<xsl:for-each select="varfield[@id='500'][subfield[@label = 'a']]">
						<xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="int505">
					<xsl:for-each select="varfield[@id='505'][subfield[@label = 'a']]">
						<xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="int740">
					<xsl:for-each select="varfield[@id='740'][subfield[@label = 'a']]">
						<xsl:value-of select="."/>
                        <xsl:text> </xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="int800">
					<xsl:for-each select="varfield[@id = '800']">
						<xsl:value-of select="subfield[@label = 'a']"/>
						<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 'd'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'd']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'd'] != '')) and (subfield[@label = 't'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 't']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'd'] != '') or (subfield[@label = 't'] != '')) and (subfield[@label = 'v'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'v']"/>
						<xsl:text>&#xD;&#xA;</xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="int810">
					<xsl:for-each select="varfield[@id = '810']">
						<xsl:value-of select="subfield[@label = 'a']"/>
						<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 'b'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'b']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'b'] != '')) and (subfield[@label = 't'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 't']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 'b'] != '') or (subfield[@label = 't'] != '')) and (subfield[@label = 'v'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'v']"/>
						<xsl:text>&#xD;&#xA;</xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<xsl:variable name="int811">
					<xsl:for-each select="varfield[@id = '811']">
						<xsl:value-of select="subfield[@label = 'a']"/>
						<xsl:if test="(subfield[@label = 'a'] != '') and (subfield[@label = 't'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 't']"/>
						<xsl:if test="((subfield[@label = 'a'] != '') or (subfield[@label = 't'] != '')) and (subfield[@label = 'v'] != '')">
							<xsl:text> </xsl:text>
						</xsl:if>
						<xsl:value-of select="subfield[@label = 'v']"/>
						<xsl:text>&#xD;&#xA;</xsl:text>
					</xsl:for-each>
				</xsl:variable>
				<!-- set the intnotes variable -->
				<xsl:if test="$int500 != ''">
					<xsl:value-of select = "$int500"/>
				</xsl:if>
				<xsl:if test="$int505 != ''">
					<xsl:choose>
						<xsl:when test="$int500 != ''">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$int505"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$int505"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$int740 != ''">
					<xsl:choose>
						<xsl:when test="($int500 != '') or ($int505 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$int740"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$int740"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$int800 != ''">
					<xsl:choose>
						<xsl:when test="($int500 != '') or ($int505 != '') or ($int740 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$int800"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$int800"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$int810 != ''">
					<xsl:choose>
						<xsl:when test="($int500 != '') or ($int505 != '') or ($int740 != '') or ($int800 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$int810"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$int810"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<xsl:if test="$int811 != ''">
					<xsl:choose>
						<xsl:when test="($int500 != '') or ($int505 != '') or ($int740 != '') or ($int800 != '') or ($int810 != '')">
							<xsl:text>&#xD;&#xA;</xsl:text>
							<xsl:value-of select = "$int811"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select = "$int811"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:variable>
			<xsl:if test="$intnotes != ''">
				<internalnotes>
					<xsl:value-of select="$intnotes"/>
				</internalnotes>
			</xsl:if>
		</copyright>
	</xsl:template>
	
	<!-- root node mapping - set up /xml/item path and look for xml/oai_marc in input XML -->
	<xsl:template match="/">
		<xml>
			<item>
				<xsl:apply-templates select="oai_marc"/>
			</item>
		</xml>
	</xsl:template>
</xsl:stylesheet>