package com.tle.core.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

public class XmlDocumentTest extends TestCase
{
	public void testCreateNodeFromXPath()
	{
		XmlDocument xml = load("structure.xml");

		xml.createNodeFromXPath("root/A/A1/A1_1", "A1_1 VALUE 2");
		xml.createNodeFromXPath("root/A/A1/A1_1", "A1_1 VALUE 3");

		List<String> nodeValues = xml.nodeValues("root/A/A1/A1_1", null);
		assertTrue(nodeValues.contains("A1_1 VALUE") && nodeValues.contains("A1_1 VALUE 2")
			&& nodeValues.contains("A1_1 VALUE 3") && nodeValues.size() == 3);

		xml.createNodeFromXPath("root/D/D1/D1_1", "D1_1 VALUE");
		nodeValues = xml.nodeValues("root/D/D1/D1_1", null);
		assertTrue(nodeValues.contains("D1_1 VALUE") && nodeValues.size() == 1);
	}

	private XmlDocument load(String file)
	{
		InputStream in = null;
		try
		{
			in = getClass().getResourceAsStream("/com/tle/core/xml/" + file);
			XmlDocument xml = new XmlDocument(in);
			return xml;
		}
		finally
		{
			try
			{
				if( in != null )
				{
					in.close();
				}
			}
			catch( IOException io )
			{
				throw new RuntimeException(io);
			}
		}
	}
}
