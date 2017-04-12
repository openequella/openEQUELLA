package com.tle.ims.metadata;

import java.io.IOException;

import com.dytech.devlib.PropBagEx;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class IMSMetadataMapperTest extends AbstractMetadataMapperTester
{
	private static final String MAP_EQ_NO_ATTRS = "mapEqNoAttrs";
	private static final String MAP_EQ_BOTH = "mapEqBoth";
	private static final String MAP_EQ_SIMPLIFIED_WITH_ATTRS = "mapEqSimplifiedWithAttrs";

	/**
	 * Test for Shaun Keatings
	 */
	public void testAnotherManifest() throws IOException
	{
		PropBagEx xml = testIms(MAP2, RESULTS);
		assertEquals(xml, "item/description", "The foul food maker");
		assertEquals(xml, "item/keywords", 3);
	}

	/**
	 * Test for Lisa.Gliddon@qed.qld.gov.au
	 */
	public void testYetAnotherManifest() throws IOException
	{
		PropBagEx xml = testIms(MAP3, RESULTS);
		assertEquals(xml, "item/general/title/main", "It's not just wind");
		assertEquals(
			xml,
			"item/general/description",
			"Test design settings for a windmill to generate electric power for an island lighthouse.  Set the blade length and pitch of the windmill to suit wind speed for each season.  Try to maximise energy efficiency of windmill operation while also minimising the back-up use of diesel fuel in power generation.");
		assertEquals(
			xml,
			"item/description",
			"Test design settings for a windmill to generate electric power for an island lighthouse.  Set the blade length and pitch of the windmill to suit wind speed for each season.  Try to maximise energy efficiency of windmill operation while also minimising the back-up use of diesel fuel in power generation.");
		assertEquals(xml, "item/general/keywords", 5);
		assertEquals(xml, "item/keywords", 5);
		assertEquals(xml, "item/lifecycle/version", "3.0");
		assertEquals(xml, "item/lifecycle/assurance/status", "Published");
		assertEquals(xml, "item/lifecycle/assurance/date", "2004-02-27T15:29:52Z");
		assertEquals(xml, "item/technical/format", 3);
		assertEquals(xml, "item/qed/subject", 8);
		assertEquals(xml, "item/qed/yearlevel", 2);
		assertEquals(xml, "item/tlf/*", 19);
		assertEquals(xml, "item/educational/context", "School");
	}

	/**
	 * Test for Stephen.Brain@tafe.tas.edu.au
	 */
	public void testOneMoreManifest() throws IOException
	{
		PropBagEx xml = testIms(MAP4, RESULTS);
		assertEquals(
			xml,
			"item/keywords",
			"alcohol literacy numeracy assessments CGEA Reading Writing Self expression Knowledge Practical purposes Structure of language Percentages metric");
		assertEquals(xml, "item/name", "Where is the Party at? - Alcohol");
		assertTrue(xml.getNode("item/description").startsWith("Discover useful information about "));
	}

	/**
	 * Test compound mapping that has mappings to standard nodes as well.
	 */
	public void testInnerCompoundMapping() throws IOException
	{
		testIms(MAP6, RESULTS);
	}

	public void testRepeatingCompoundMapping() throws IOException
	{
		testIms(MAP7, RESULTS);
	}

	public void testRepeating() throws IOException
	{
		PropBagEx xml = testIms(MAP3, RESULTS);
		PropBagEx xml2 = xml.newSubtree("item");
		xml2.setNode("general/title/main", "blah");
	}

	public void testIMS() throws IOException
	{
		PropBagEx xml = testIms(MAP1, "imsResults.xml");
		assertEquals(xml, "item/itembody/language", "en");
		assertEquals(xml, "item/itembody/catalogentry/catalog/", "Australian Flexible Learning Toolboxes");
		assertEquals(xml, "item/name", "Communicate effectively in a Customer Contact Centre");

		// Testing repeat
		assertEquals(xml, "item/itembody/audience/userlevel", 2);

		// Testing compound
		assertEquals(xml, "item/itembody/classification/description/langstring",
			"ICTCC101A Communicate effectively in a Customer Contact Centre");

		assertEquals(xml, "item/itembody/classification/keyword/langstring",
			"information, communication technology, AQF Level Certificate III");

		assertEquals(xml, "item/itembody/compound/type", 2);
		assertEquals(xml, "item/itembody/compound/type/*", 2);

		// Testing attribute on mapped node
		assertEquals(xml, "item/itembody/compound/@blah", true);

		// Testing attributes
		assertEquals("a", xml.getNode("item/itembody/compound/type[0]/@test"));
		assertEquals("b", xml.getNode("item/itembody/compound/type[1]/@test"));

		// Testing a node that exists in the ims package but NOT the schema
		assertFalse(xml.nodeExists("item/itembody/compound/shouldntbehere"));

		// Tests if langstring can be ignore
		assertEquals("10", xml.getNode("item/itembody/rights/cost/source"));
	}

	public void testEqCompoundNoAttrs() throws Exception
	{
		testIms(MAP_EQ_NO_ATTRS, "results.xml");
	}

	public void testEqSimplifiedCompoundWithAttrs() throws Exception
	{
		testIms(MAP_EQ_SIMPLIFIED_WITH_ATTRS, "results.xml");
	}

	public void testEqCompoundBoth() throws Exception
	{
		testIms(MAP_EQ_BOTH, "results.xml");
	}
}
