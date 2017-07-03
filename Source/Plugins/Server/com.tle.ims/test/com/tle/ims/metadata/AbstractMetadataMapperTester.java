/*
 * Created on Apr 22, 2005
 */
package com.tle.ims.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.ejb.helpers.metadata.mappers.PackageMapper;
import com.dytech.edge.ejb.helpers.metadata.mappers.XPathMapper;
import com.dytech.edge.ejb.helpers.metadata.mapping.Mapping;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.MetadataMapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping;
import com.tle.core.xstream.XMLCompare;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public abstract class AbstractMetadataMapperTester extends TestCase
{
	protected static final String RESULTS = "results.xml";

	protected static final String MAP1 = "map1";
	protected static final String MAP2 = "map2";
	protected static final String MAP3 = "map3";
	protected static final String MAP4 = "map4";
	protected static final String MAP5 = "map5"; // Jays HTML test
	protected static final String MAP6 = "map6";
	protected static final String MAP7 = "map7";

	protected MetadataMapping itemMapping;
	protected Schema schema;
	protected boolean debug;

	@Override
	protected void setUp() throws Exception
	{
		debug = false;
	}

	public void setUp(String folder)
	{
		try( Reader mappingReader = new UnicodeReader(getClass().getResourceAsStream("/" + folder + "/itemdef.xml"),
			"UTF-8"); InputStream schemaIn = getClass().getResourceAsStream("/" + folder + "/schema.xml") )
		{
			itemMapping = (MetadataMapping) new XStream().fromXML(mappingReader);

			PropBagEx sxml = new PropBagEx(schemaIn);
			if( sxml.nodeExists("definition/xml") )
			{
				sxml = sxml.getSubtree("definition/xml");
			}

			schema = new Schema();
			schema.setDefinition(sxml);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	protected InputStream getResourceAsInputStream(String folder, String file) throws IOException
	{
		final String resource = '/' + folder + '/' + file;
		final InputStream in = AbstractMetadataMapperTester.class.getResourceAsStream(resource);
		if( in != null )
		{
			return in;
		}
		else
		{
			throw new IOException("Resource not found: " + resource);
		}
	}

	private Reader getResourceAsReader(String folder, String file) throws IOException
	{
		return new UnicodeReader(getResourceAsInputStream(folder, file), "UTF-8");
	}

	protected PropBagEx testIms(String folder, String resultsFile) throws IOException
	{
		PropBagEx xml = new PropBagEx();
		return testIms(xml, folder, resultsFile);
	}

	protected PackageMapper createIMSMetadataMapper()
	{
		// Nothing by default
		return null;
	}

	protected Collection<Mapping> mapIMS(String folder, Collection<PackageMapper> mappers)
	{
		// Nothing by default
		return new ArrayList<Mapping>();
	}

	protected PropBagEx testIms(PropBagEx xml, String folder, String resultsFile) throws IOException
	{
		setUp(folder);

		XPathMapper mapper = new XPathMapper(schema);
		Collection<IMSMapping> imsMapping = itemMapping.getImsMapping();
		mapper.processMapping(imsMapping);
		for( IMSMapping ims : imsMapping )
		{
			if( ims.isReplace() )
			{
				String path = ims.getItemdef();
				Iterator<PropBagEx> j = xml.iterateAll(path);
				while( j.hasNext() )
				{
					j.next();
					j.remove();
				}
			}
		}
		try( InputStream imsStream = getResourceAsInputStream(folder, "imsmanifest.xml") )
		{

			Collection<Mapping> mappings = mapper.map(imsStream);
			for( Mapping mapping : mappings )
			{
				mapping.update(xml);
			}
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}

		PropBagEx results = new PropBagEx(getResourceAsReader(folder, resultsFile));

		if( !debug )
		{
			XMLCompare xmlCompare = new XMLCompare();
			xmlCompare.setTrimTextValues(true);
			boolean same;
			try
			{
				same = xmlCompare.compare(new StringReader(xml.toString()), new StringReader(results.toString()));
			}
			catch( Exception e )
			{
				throw new RuntimeException(e.getMessage(), e);
			}
			assertTrue(same);
		}

		return xml;

	}

	protected void assertEquals(PropBagEx xml, String path, String value)
	{
		assertEquals(value, xml.getNode(path));
	}

	protected void assertEquals(PropBagEx xml, String path, int count)
	{
		assertEquals(count, xml.nodeCount(path));
	}

	protected void assertEquals(PropBagEx xml, String path, boolean bool)
	{
		assertEquals(Boolean.toString(bool), xml.getNode(path));
	}
}
