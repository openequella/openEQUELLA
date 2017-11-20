/*
 * Created on Jul 7, 2005
 */
package com.tle.core.xstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.tle.core.xstream.XMLCompare;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class XMLCompareTest extends TestCase
{
	private XMLCompare comparer;

	@Override
	protected void setUp()
	{
		comparer = new XMLCompare();
		comparer.setTrimTextValues(true);
	}

	@Override
	protected void tearDown() throws Exception
	{
		comparer = null;
	}

	public void testUnordered() throws Exception
	{
		doTest();
	}

	private void doTest() throws Exception
	{
		int testNumber = 1;

		String baseName = "unordered/base-" + testNumber + ".xml";
		Document base = getDocument(baseName);
		if( base == null )
		{
			fail("Could not find base-" + testNumber + ".xml");
		}

		while( base != null )
		{
			System.out.println("---------------------------------");
			System.out.println("Testing " + baseName);

			System.out.println("\tShould Match...");
			int varNumber = 1;
			Document variation;
			do
			{
				String varName = "unordered/good-" + varNumber + "-for-" + testNumber + ".xml";
				variation = getDocument(varName);
				if( variation != null )
				{
					System.out.println("\t\t" + varName);
					try
					{
						assertTrue(comparer.compare(base, variation));
					}
					catch( AssertionFailedError ex )
					{
						comparer.compare(base, variation);
						throw ex;
					}
				}
				varNumber++;
			}
			while( variation != null );

			System.out.println("\tShould Differ...");
			varNumber = 1;
			do
			{
				String varName = "unordered/bad-" + varNumber + "-for-" + testNumber + ".xml";
				variation = getDocument(varName);
				if( variation != null )
				{
					System.out.println("\t\t" + varName);
					try
					{
						assertFalse(comparer.compare(base, variation));
					}
					catch( AssertionFailedError ex )
					{
						comparer.compare(base, variation);
						throw ex;
					}
				}
				varNumber++;
			}
			while( variation != null );

			// Move to the next test
			testNumber++;
			baseName = "unordered/base-" + testNumber + ".xml";
			base = getDocument(baseName);
		}
	}

	private Document getDocument(String filename)
		throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException
	{
		try( InputStream in = XMLCompareTest.class.getResourceAsStream("/xmlcompare/" + filename) )
		{
			if( in == null )
			{
				return null;
			}
			else
			{
				return XMLCompare.getDocument(new InputStreamReader(in, "UTF-8"));
			}
		}
	}
}
