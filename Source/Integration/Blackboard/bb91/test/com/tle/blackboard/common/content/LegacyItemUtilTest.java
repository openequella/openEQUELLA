/*
 * Created on Dec 21, 2004
 */
package com.tle.blackboard.common.content;

import java.io.UnsupportedEncodingException;

import org.apache.cxf.common.util.URIParserUtil;

import com.tle.blackboard.common.propbag.PropBagMin;

import junit.framework.TestCase;

/**
 * @author Chris Beach
 */
@SuppressWarnings("nls")
public class LegacyItemUtilTest extends TestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testFixUnescapedAmpersandsEmpty()
	{
		assertEquals(LegacyItemUtil.fixUnescapedAmpersands(""), "");
	}

	public void testFixUnescapedAmpersandsSingle()
	{
		assertEquals(LegacyItemUtil.fixUnescapedAmpersands("&"), "&amp;");
	}

	public void testFixUnescapedAmpersandsGeneral()
	{
		assertEquals(LegacyItemUtil.fixUnescapedAmpersands("&amp; & &nsbp; &tc., &tc. &tc &"),
			"&amp; &amp; &nsbp; &amp;tc., &amp;tc. &amp;tc &amp;");
	}

	public void testFixUnescapedAmpersandsMultiples()
	{
		assertEquals(LegacyItemUtil.fixUnescapedAmpersands("&& &&& &ns bp;"), "&amp;&amp; &amp;&amp;&amp; &amp;ns bp;");
	}

	public void testFixUnescapedAmpersandsXml()
	{
		assertEquals(
			LegacyItemUtil
				.fixUnescapedAmpersands("<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test &</name><description>Desc - EQAS-403 test &</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse-&.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse-&.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse-&.jpg</file><description>mauna-kea-heaven-time-lapse-&.jpg</description></attachment></attachments></item>"),
			"<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test &amp;</name><description>Desc - EQAS-403 test &amp;</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse-&amp;.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse-&amp;.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse-&amp;.jpg</file><description>mauna-kea-heaven-time-lapse-&amp;.jpg</description></attachment></attachments></item>");
	}

	public void testCreatePropBagWithAmpersands()
	{
		String xmlStr = "<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test &</name><description>Desc - EQAS-403 test &</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse-&.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse-&.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse-&.jpg</file><description>mauna-kea-heaven-time-lapse-&.jpg</description></attachment></attachments></item>";
		PropBagMin props = LegacyItemUtil.createPropBag(xmlStr);
		assertEquals(
			props.toString(),
			"<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test &amp;</name><description>Desc - EQAS-403 test &amp;</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse-&amp;.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse-&amp;.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse-&amp;.jpg</file><description>mauna-kea-heaven-time-lapse-&amp;.jpg</description></attachment></attachments></item>");
	}

	public void testCreatePropBagWithXmlEscapedCharacters()
	{
		String xmlStr = "<item id=\"fc5d6113-ba7c-4bed-9a0b-77b5d70e452c\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>amp test &&lt;&gt;'&quot;; & &lt; &gt; ' &quot; ;</name><description>amp test &&lt;&gt;'&quot;; & &lt; &gt; ' &quot; ; desc</description><requestUuid/><attachments selected=\"amp%20test%20att%20%26%27%3C%3B%3E%2522.jpg.jpg\" selectedDescription=\"\" selectedTitle=\"amp test att &'&lt;;&gt;%22.jpg.jpg\"><attachment scorm=\"\"><file>amp test att &'&lt;;&gt;%22.jpg.jpg</file><description>amp test att &'&lt;;&gt;%22.jpg.jpg</description></attachment></attachments></item>";
		PropBagMin props = LegacyItemUtil.createPropBag(xmlStr);
		assertEquals(
			props.toString(),
			"<item id=\"fc5d6113-ba7c-4bed-9a0b-77b5d70e452c\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>amp test &amp;&lt;&gt;'&quot;; &amp; &lt; &gt; ' &quot; ;</name><description>amp test &amp;&lt;&gt;'&quot;; &amp; &lt; &gt; ' &quot; ; desc</description><requestUuid/><attachments selected=\"amp%20test%20att%20%26%27%3C%3B%3E%2522.jpg.jpg\" selectedDescription=\"\" selectedTitle=\"amp test att &amp;'&lt;;&gt;%22.jpg.jpg\"><attachment scorm=\"\"><file>amp test att &amp;'&lt;;&gt;%22.jpg.jpg</file><description>amp test att &amp;'&lt;;&gt;%22.jpg.jpg</description></attachment></attachments></item>");
	}

	public void testCreatePropBagWithoutAmpersands()
	{
		String xmlStr = "<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test</name><description>Desc - EQAS-403 test</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse.jpg</file><description>mauna-kea-heaven-time-lapse.jpg</description></attachment></attachments></item>";
		PropBagMin props = LegacyItemUtil.createPropBag(xmlStr);
		assertEquals(
			props.toString(),
			"<item id=\"4341ba7e-cba6-4c1d-8def-c462fd02bd04\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>EQAS-403 test</name><description>Desc - EQAS-403 test</description><requestUuid/><attachments selected=\"mauna-kea-heaven-time-lapse.jpg\" selectedDescription=\"\" selectedTitle=\"mauna-kea-heaven-time-lapse.jpg\"><attachment scorm=\"\"><file>mauna-kea-heaven-time-lapse.jpg</file><description>mauna-kea-heaven-time-lapse.jpg</description></attachment></attachments></item>");
	}

	// Currently this fails. If clients have non-xml entities in the item names,
	// descriptions, or filenames of attachments, then this kind of tests will
	// need to be setup on a case by case basis.
	// public void testCreatePropBagWithNonXmlHtmlEntities()
	// {
	// String xmlStr =
	// "<item id=\"ee3f5d9f-d7cc-4104-9556-993a1d96f506\" itemdefid=\"6e85ce64-9a11-c5e7-69a4-bd30ec61007f\" link=\"false\" live=\"false\" modified=\"false\" notify=\"true\" version=\"1\"><name>amp test &nbsp;</name><description>amp test &nbsp; desc</description><requestUuid/><attachments selected=\"amp%20test%20att%20%26nbsp%3B.jpg\" selectedDescription=\"\" selectedTitle=\"amp test att &nbsp;.jpg\"><attachment scorm=\"\"><file>amp test att &nbsp;.jpg</file><description>amp test att &nbsp;.jpg</description></attachment></attachments></item>";
	// PropBagMin props = LegacyItemUtil.createPropBag(xmlStr);
	// assertEquals(props.toString(), "TODO");
	//
	// }

	public void testScrubLegacyPageSoloSpace()
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage(" "), "%20");
	}

	public void testScrubLegacyPageSoloEncodedPlus()
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage("%2B"), "%252B");
	}

	public void testScrubLegacyPageSoloPlus()
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage("+"), "%2B");
	}

	public void testScrubLegacyPageSoloSlash()
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage("/"), "/");
	}

	public void testScrubLegacyPageSoloPreEncodedSlash()
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage("%2F"), "%252F");
	}

	public void testScrubLegacyPageOnlyFilename() throws UnsupportedEncodingException
	{
		// Test the special characters in a filename.
		// Note - It's by design to not encode the slash.
		String page = "I%2Fam a spacey filename !#$&'%()*+,:;=?@[].jpg";
		String uri = (URIParserUtil.escapeChars(page)).toString();
		System.out.println("URI parser output [" + uri + "]");
		assertEquals(LegacyItemUtil.scrubLegacyPage(page),
			"I%252Fam%20a%20spacey%20filename%20%21%23%24%26%27%25%28%29*%2B%2C%3A%3B%3D%3F%40%5B%5D.jpg");
	}

	public void testScrubLegacyPageFolderAndFilename() throws UnsupportedEncodingException
	{
		String page = "this / is / a / folder/I%2Fam a spacey filename !#$&'%()*+,:;=?@[].jpg";

		assertEquals(LegacyItemUtil.scrubLegacyPage(page), page);
	}

	public void testScrubLegacyPageOnlyFolder() throws UnsupportedEncodingException
	{
		// Test the special characters except for : since you can't put that in
		// a filename.
		// Note - It's by design to not encode the slash.
		String page = "this / is / a / folder/";

		assertEquals(LegacyItemUtil.scrubLegacyPage(page), page);
	}
		
	public void testScrubLegacyPageNullPage() throws UnsupportedEncodingException
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage(null), "");
	}
	
	public void testScrubLegacyPageDotSlashPage() throws UnsupportedEncodingException
	{
		assertEquals(LegacyItemUtil.scrubLegacyPage("./"), "");
	}
}