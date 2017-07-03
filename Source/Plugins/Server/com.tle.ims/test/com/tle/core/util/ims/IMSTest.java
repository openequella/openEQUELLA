package com.tle.core.util.ims;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParserException;

import com.dytech.devlib.PropBagEx;
import com.tle.core.util.ims.IMSUtilities.ManifestResolver;
import com.tle.core.util.ims.beans.IMSItem;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSOrganisation;
import com.tle.core.xstream.TLEXStream;
import com.tle.core.xstream.XMLCompare;
import com.tle.core.xstream.XMLDataConverter;

import junit.framework.TestCase;

/**
 * 
 */
public class IMSTest extends TestCase
{
	private IMSManifest manifest;

	public void setUp(String file)
	{
		TLEXStream xstream = TLEXStream.instance();
		xstream.registerConverter(new XMLDataConverter());
		manifest = (IMSManifest) xstream.fromXML(new InputStreamReader(getClass().getResourceAsStream(file)),
			IMSManifest.class);
	}

	public void testManifest()
	{
		setUp("imsmanifest.xml");
		// assertEquals("Alien life form", manifest.getTitle());

		assertEquals(1, manifest.getOrganisations().size());

		IMSOrganisation org = manifest.getOrganisations().get(0);
		assertEquals("Alien life form", org.getTitle());

		assertEquals(1, org.getItems().size());

		IMSItem item = org.getItems().get(0);
		assertEquals("Start: Alien life form", item.getTitle());
	}

	public void testGetTitleFromManifest() throws XmlPullParserException, IOException, Exception
	{
		String title = IMSUtilities
			.getTitleFromManifest(new InputStreamReader(getClass().getResourceAsStream("imsmanifest.xml")));
		assertEquals("Alien life form", title);

		title = IMSUtilities
			.getTitleFromManifest(new InputStreamReader(getClass().getResourceAsStream("basicchinese.xml")));
		assertEquals("Basic Chinese", title);

		title = IMSUtilities
			.getTitleFromManifest(new InputStreamReader(getClass().getResourceAsStream("scorm13-ieee.xml")));
		assertEquals("Glide: take a flight [no spoken instructions]", title);

		title = IMSUtilities
			.getTitleFromManifest(new InputStreamReader(getClass().getResourceAsStream("metadata_in_org.xml")));
		assertEquals("Another Alien life form", title);
	}

	/**
	 * Jira TLE-2307
	 */
	public void testCombineManifest() throws IOException, XmlPullParserException
	{
		try( Writer imsOutput = new StringWriter() )
		{
			IMSUtilities.combine(createResolver("basicchinese.xml"), imsOutput);
			String title = IMSUtilities.getTitleFromManifest(new StringReader(imsOutput.toString()));
			assertEquals("Basic Chinese", title);
		}
	}

	public void testCombineManifest2() throws IOException, XmlPullParserException
	{
		try( Writer imsOutput = new StringWriter() )
		{
			IMSUtilities.combine(createResolver("foodmaker.xml"), imsOutput);
			String string = imsOutput.toString();
			// This is to ensure namespaces are kept!!!
			assertEquals(51, count(string, "xmlns"));
			assertEquals(-1, string.indexOf("<imsmd:lom>"));

			String title = IMSUtilities.getTitleFromManifest(new StringReader(string));
			assertEquals("The foul food maker", title);
		}
	}

	/**
	 * Test for Stephen.Brain@tafe.tas.edu.au
	 */
	public void testCombineManifest3() throws Exception
	{
		testCombine("combine1");
	}

	private void testCombine(String folder) throws Exception
	{
		StringWriter output = new StringWriter();
		IMSUtilities.combine(createResolver("/" + folder + "/imsmanifest.xml"), output);

		Reader result = new InputStreamReader(getClass().getResourceAsStream("/" + folder + "/results.xml"));

		assertTrue(new XMLCompare().compare(new StringReader(output.getBuffer().toString()), result));
	}

	private int count(String string, String value)
	{
		int count = 0;
		for( int index = string.indexOf(value); index > 0; index = string.indexOf(value, index + 1) )
		{
			count++;
		}
		return count;
	}

	public void testShrinkXML()
	{
		setUp("imsmanifest.xml");
		PropBagEx xml = IMSUtilities.shrinkXML(manifest);
		assertEquals(2, xml.getIntNode("wrapper/@type"));
		assertEquals(1, xml.getIntNode("wrapper/wrapper/@type"));
		assertEquals(3, xml.getIntNode("wrapper/wrapper/wrapper/@type"));
		assertEquals(true, xml.isNodeTrue("wrapper/wrapper/wrapper/@isvisible"));
		assertEquals("Start: Alien life form", xml.getNode("wrapper/wrapper/wrapper"));
		assertEquals("Content/LV532/LO_10/20030130/LO10/index.htm", xml.getNode("wrapper/wrapper/wrapper/file"));
		assertEquals(15, xml.nodeCount("wrapper/file"));
	}

	private ManifestResolver createResolver(String relPath)
	{
		try
		{
			return new IMSUtilities.FileManifestResolver(new File(this.getClass().getResource(relPath).toURI()));
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
