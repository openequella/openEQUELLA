/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.TLEXStream;
import com.tle.core.xstream.XMLData;
import com.tle.core.xstream.XMLDataChild;
import com.tle.core.xstream.XMLDataConverter;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.XMLDataResolver;
import com.tle.core.xstream.XMLDataResolverMapping;
import com.tle.core.xstream.mapping.CollectionMapping;
import com.tle.core.xstream.mapping.DataMapMapping;
import com.tle.core.xstream.mapping.DataMapping;
import com.tle.core.xstream.mapping.DateMapping;
import com.tle.core.xstream.mapping.ElementMapping;
import com.tle.core.xstream.mapping.ListMapping;
import com.tle.core.xstream.mapping.MapMapping;
import com.tle.core.xstream.mapping.NamespaceMapping;
import com.tle.core.xstream.mapping.NodeMapping;
import com.tle.core.xstream.mapping.NodeTypeMapping;
import com.tle.core.xstream.mapping.PropBagMapping;
import com.tle.core.xstream.mapping.URLMapping;

import junit.framework.TestCase;

/**
 *
 */
public class XMLDataConverterTest extends TestCase
{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final String DATE = "2005-05-25T12:11:20";
	private static final String URL = "http://www.google.com.au";

	private TLEXStream xstream;
	private String stringXml;
	private XMLDataConverter converter;
	static XMLDataMappings mappings;
	static XMLDataMappings mappings2;
	private TestBean bean;
	private PropBagEx xml;

	@Override
	protected void setUp() throws Exception
	{
		xstream = TLEXStream.instance();
		stringXml = "<xml xml:base=\"attribute\" attribute=\"attribute\" another=\"another\">"
			+ "	<string>string</string>" + "   <parent><parent reference=\"../..\"/></parent>"
			+ "   <subclasser xclass=\"" + OverideBean.class.getName() + "\">" + "     <string>string</string>"
			+ "   </subclasser>" + "	<attribute attribute=\"attribute\" attribute2=\"attribute\">string</attribute>"
			+ "	<integer>1</integer>" + "	<bool>true</bool>" + "	<floating>1.4</floating>" + "	<doub>1.4</doub>"
			+ "	<shorter>1</shorter>" + "	<default>default</default>" + "	<url>" + URL + "</url>" + "	<date>" + DATE
			+ "</date>" + "	<bean><string>string</string></bean>" + "	<collection>"
			+ "		<string att=\"1\">string</string>" + "		<string att=\"2\">string</string>" + "	</collection>"
			+ "	<urlcollection>" + "		<url>" + URL + "</url>" + "		<url>" + URL + "</url>"
			+ "	</urlcollection>" + "	<blankcollection>" + "		<blank/>" + "		<blank/>"
			+ "	</blankcollection>" + "	<datacollection>" + "		<data><string>string</string></data>"
			+ "		<data><string>string</string></data>" + "	</datacollection>" + "	<diffcollection>"
			+ "		<type1>type1</type1>" + "		<type2>type2</type2>" + "		<type1>type1</type1>"
			+ "		<type2>type2</type2>" + "	</diffcollection>" + "	<node empty=\"\">" + "		<empty/>"
			+ "		<empty2/>" + "	</node>" + "	<element a1=\"a1\" a2=\"a2\">"
			+ "		<node>test<node2 a1=\"a1\" a2=\"a2\">sdfsdf</node2>test2</node>"
			+ "		<node3><node2 a1=\"a1\" a2=\"a2\">sdfsdf</node2></node3>" + "	</element>"
			+ "	<deep deep=\"attribute\"><deep>deep1</deep></deep>"
			+ "	<deep2><deeper><deep>deep2</deep></deeper></deep2>" + "	<prefix:namespace>namespace</prefix:namespace>"
			+ "	<map>" + "		<node key=\"a\" ><value>valuea</value></node>"
			+ "		<node key=\"b\" ><value>valueb</value></node>" + "	</map>" + "	<datamap>" + "		<data>"
			+ "			<key><data>a</data></key>" + "			<value><data>valuea</data></value>" + "		</data>"
			+ "		<data>" + "			<key><data>b</data></key>" + "			<value><data>valueb</data></value>"
			+ "		</data>" + "	</datamap>" + "	<resolver type=\"override\"><string>resolved</string></resolver>"
			+ "	<resolver2 type=\"testbean2\"><string>resolved</string></resolver2>"
			+ "	<namespaced xmlns=\"http://www.imsglobal.org/xsd/imscp_v1p1\" xmlns:imsmd=\"http://www.imsglobal.org/xsd/imsmd_v1p2\">test</namespaced>"
			+ " 	<types>" + "		<type>test1</type>" + "		<type>test2</type>" + "		<type>test3</type>"
			+ "		<type>sdfsdf</type>" + "	</types>" + "</xml>";

		converter = new XMLDataConverter();
		mappings = new XMLDataMappings();
		mappings2 = new XMLDataMappings();
		// xstream.alias("xml", TestBean.class);
		xstream.registerConverter(converter);
	}

	private void getBean()
	{
		bean = (TestBean) xstream.fromXML(stringXml, TestBean.class);
	}

	private void getBean(TestBean bean2)
	{
		bean = (TestBean) xstream.fromXML(stringXml, bean2);
	}

	private void getPropBagEx()
	{
		xml = new PropBagEx(xstream.toXML(bean));
	}

	public void testPrimitive()
	{
		mappings.addNodeMapping(new NodeMapping("string", "string"));
		mappings.addNodeMapping(new NodeMapping("integer", "integer"));
		mappings.addNodeMapping(new NodeMapping("bool", "bool"));
		mappings.addNodeMapping(new NodeMapping("floating", "floating"));
		mappings.addNodeMapping(new NodeMapping("doub", "doub"));
		mappings.addNodeMapping(new NodeMapping("shorter", "shorter"));

		getBean();

		assertEquals("string", bean.string);
		assertEquals(1, bean.integer);
		assertEquals(1.3f, 1.5f, bean.floating);
		assertEquals(1.3d, 1.5d, bean.doub);
		assertEquals(1, bean.shorter);

		getPropBagEx();
		assertEquals("string", xml.getNode("string"));
		assertEquals("1", xml.getNode("integer"));
		assertEquals("true", xml.getNode("bool"));
		assertEquals("1.4", xml.getNode("floating"));
		assertEquals("1.4", xml.getNode("doub"));
		assertEquals("1", xml.getNode("shorter"));
	}

	public void testMissingAttribute()
	{
		mappings.addNodeMapping(new NodeMapping("notNull", "string/@nossdfsdfsdfthere"));
		getBean();
		assertNotNull(bean.notNull); // Shouldn't be overridden
		getPropBagEx();
	}

	public void testNull()
	{
		mappings.addNodeMapping(new NodeMapping("string", "nothere"));
		getBean();
		assertEquals(null, bean.string);
		getPropBagEx();
		assertEquals(0, xml.nodeCount("nothere"));
	}

	public void testTwoAttributes()
	{
		mappings.addNodeMapping(new NodeMapping("another", "@another"));
		mappings.addNodeMapping(new NodeMapping("attribute1", "@attribute"));

		getBean();
		assertEquals("another", bean.another);
		assertEquals("attribute", bean.attribute1);
		getPropBagEx();
	}

	public void testDate() throws ParseException
	{
		mappings.addNodeMapping(new DateMapping("date", "date", DATE_FORMAT));

		getBean();

		Date date = DATE_FORMAT.parse(DATE);
		assertEquals(date, bean.date);

		bean.defaultValue = null;

		getPropBagEx();

		assertEquals(DATE_FORMAT.format(date), xml.getNode("date"));
	}

	public void testURL() throws MalformedURLException
	{
		mappings.addNodeMapping(new URLMapping("url", "url"));
		mappings.addNodeMapping(new URLMapping("string", "string")); // Not a
																		// url

		getBean();

		URL url = new URL(URL);
		assertEquals(url.toString(), bean.url.toString());
		assertEquals(null, bean.string);

		bean.defaultValue = null;

		getPropBagEx();

		assertEquals(url.toString(), xml.getNode("url"));
	}

	public void testDataMapping()
	{
		mappings2.addNodeMapping(new NodeMapping("string", "string"));
		mappings.addNodeMapping(new DataMapping("bean", "bean", TestBean2.class));

		getBean();

		assertEquals("string", bean.bean.getString());

		getPropBagEx();

		assertEquals("string", xml.getNode("bean/string"));
	}

	public void testEmptyNode()
	{
		mappings2.addNodeMapping(new NodeMapping("string", ""));
		mappings.addNodeMapping(new DataMapping("bean", "string", TestBean2.class));

		getBean();

		assertEquals("string", bean.bean.getString());

		getPropBagEx();

		assertEquals("string", xml.getNode("string"));
	}

	public void testOverrideMapping()
	{
		mappings2.addNodeMapping(new NodeMapping("string", "string"));
		mappings.addNodeMapping(new DataMapping("bean", "bean", OverideBean.class));

		getBean();

		assertEquals("string", bean.bean.getString());

		getPropBagEx();

		assertEquals("string", xml.getNode("bean/string"));
	}

	public void testAttributes()
	{
		mappings.addNodeMapping(new NodeMapping("attribute1", "@attribute"));
		mappings.addNodeMapping(new NodeMapping("attribute2", "attribute/@attribute"));

		getBean();

		assertEquals("attribute", bean.attribute1);
		assertEquals("attribute", bean.attribute2);

		getPropBagEx();

		assertEquals("attribute", xml.getNode("@attribute"));
		assertEquals("attribute", xml.getNode("attribute/@attribute"));
	}

	public void testCollection()
	{
		mappings.addNodeMapping(new CollectionMapping("collection", "collection/string"));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals("string", bean.collection.iterator().next().toString());
		getPropBagEx();
		assertEquals(2, xml.nodeCount("collection/string"));
		assertEquals("string", xml.getNode("collection/string"));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals("string", bean.collection.iterator().next().toString());
	}

	public void testIncorrectCollection()
	{
		try
		{
			new CollectionMapping("", "", HashMap.class);
			assertTrue("HashMap is not of type Collection", false);
		}
		catch( Exception e )
		{
			assertTrue(true);
		}

	}

	public void testMultipleCollections()
	{
		mappings.addNodeMapping(new CollectionMapping("collection", "diffcollection/type1"));
		mappings.addNodeMapping(new CollectionMapping("collection2", "diffcollection/type2"));

		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals(2, bean.collection2.size());

		getPropBagEx();
		assertEquals(1, xml.nodeCount("diffcollection"));
		assertEquals(2, xml.nodeCount("diffcollection/type1"));
		assertEquals(2, xml.nodeCount("diffcollection/type2"));
		assertEquals("type1", xml.getNode("diffcollection/type1"));
	}

	public void testCollectionAdvanced() throws MalformedURLException
	{
		String xpath = "urlcollection/url";
		mappings.addNodeMapping(new CollectionMapping("collection", xpath, ArrayList.class, new URLMapping("", "")));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals(new URL(URL).toString(), bean.collection.iterator().next().toString());
		getPropBagEx();
		assertEquals(2, xml.nodeCount(xpath));
		assertEquals(URL, xml.getNode(xpath));
	}

	public void testCollectionBlank()
	{
		String xpath = "blankcollection/blank";
		mappings.addNodeMapping(new CollectionMapping("collection", xpath, ArrayList.class, new NodeMapping("", "")));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals("", bean.collection.iterator().next());
		getPropBagEx();
		assertEquals(2, xml.nodeCount(xpath));
		assertEquals("", xml.getNode(xpath));
	}

	public void testList()
	{
		mappings.addNodeMapping(new ListMapping("collection", "collection/string"));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals("string", bean.collection.iterator().next().toString());
		getPropBagEx();
		assertEquals(2, xml.nodeCount("collection/string"));
		assertEquals("string", xml.getNode("collection/string"));
	}

	public void testAttCollection()
	{
		mappings.addNodeMapping(new CollectionMapping("collection", "collection/string/@att"));
		getBean();
		assertEquals(2, bean.collection.size());
		getPropBagEx();
		assertEquals("1", xml.getNode("collection/string[0]/@att"));
		assertEquals("2", xml.getNode("collection/string[1]/@att"));
	}

	public void testNodeAndAttribute()
	{
		// Test out of order because if don't evaluate attributes first
		// then an error is thrown
		mappings.addNodeMapping(new NodeMapping("attribute1", "attribute/@attribute"));
		mappings.addNodeMapping(new NodeMapping("string", "attribute"));
		mappings.addNodeMapping(new NodeMapping("attribute2", "attribute/@attribute2"));

		getBean();

		assertEquals("string", bean.string);
		assertEquals("attribute", bean.attribute1);
		assertEquals("attribute", bean.attribute2);
		getPropBagEx();

		assertEquals("string", xml.getNode("attribute"));
		assertEquals("attribute", xml.getNode("attribute/@attribute"));
		assertEquals("attribute", xml.getNode("attribute/@attribute2"));
	}

	public void testEmpty()
	{
		mappings.addNodeMapping(new NodeMapping("string", "node/empty"));
		mappings.addNodeMapping(new NodeMapping("another", "node/empty2"));

		getBean();

		assertEquals("", bean.string);
		assertEquals("", bean.another);

		getPropBagEx();

		assertEquals(true, xml.nodeExists("node/empty"));
		assertEquals(true, xml.nodeExists("node/empty2"));
	}

	public void testDeep()
	{
		mappings.addNodeMapping(new NodeMapping("string", "deep/deep"));
		mappings.addNodeMapping(new NodeMapping("attribute1", "deep/@deep"));
		mappings.addNodeMapping(new NodeMapping("another", "deep2/deeper/deep"));

		getBean();

		assertEquals("deep1", bean.string);
		assertEquals("deep2", bean.another);
		assertEquals("attribute", bean.attribute1);

		getPropBagEx();

		assertEquals(true, xml.nodeExists("deep/deep"));
		assertEquals("attribute", xml.getNode("deep/@deep"));
		assertEquals(1, xml.nodeCount("deep"));
		assertEquals(true, xml.nodeExists("deep2/deeper/deep"));
	}

	public void testCollectionSuperDuperAdvanced()
	{
		String xpath = "datacollection/data";
		mappings2.addNodeMapping(new NodeMapping("string", "string"));

		mappings.addNodeMapping(
			new CollectionMapping("collection", xpath, ArrayList.class, new DataMapping("", "", TestBean2.class)));
		getBean();
		assertEquals(2, bean.collection.size());
		assertEquals("string", (((TestBean2) bean.collection.iterator().next()).getString()));
		getPropBagEx();
		assertEquals(2, xml.nodeCount(xpath));
		assertEquals("string", xml.getNode(xpath + "/string"));
	}

	public void testNullDataMapping()
	{
		mappings.addNodeMapping(new DataMapping("bean", "nothere", TestBean2.class));

		getBean();

		assertEquals(null, bean.bean);

		getPropBagEx();

		assertEquals(false, xml.nodeExists("nothere"));
	}

	public void testElement()
	{
		mappings.addNodeMapping(new ElementMapping("element", "element"));

		getBean();

		PropBagEx bag = new PropBagEx(bean.element);
		assertEquals("test", bag.getNode("node"));
		assertEquals("sdfsdf", bag.getNode("node/node2"));

		getPropBagEx();

		assertEquals("sdfsdf", xml.getNode("element/node/node2"));
	}

	public void testPropBag()
	{
		mappings.addNodeMapping(new PropBagMapping("xml", "element"));
		mappings.addNodeMapping(new NodeMapping("string", "namespace"));

		getBean();

		assertEquals("sdfsdf", bean.xml.getNode("node/node2"));
		assertEquals("namespace", bean.string);

		getPropBagEx();

		assertEquals("sdfsdf", xml.getNode("element/node/node2"));
	}

	public void testNamespaceDeclaration()
	{
		mappings.addNodeMapping(new NodeMapping("string", "namespaced"));
		mappings.addNodeMapping(new NamespaceMapping("map", "namespaced"));
		getBean();
		assertEquals("test", bean.string);
		assertEquals(2, bean.map.size());

		getPropBagEx();

		assertEquals("test", xml.getNode("namespaced"));
		assertEquals("http://www.imsglobal.org/xsd/imscp_v1p1", xml.getNode("namespaced/@xmlns"));
		assertEquals("http://www.imsglobal.org/xsd/imsmd_v1p2", xml.getNode("namespaced/@xmlns:imsmd"));
	}

	public void testNamespace()
	{
		mappings.setIgnoreNS(true);
		mappings.addNodeMapping(new NodeMapping("string", "namespace"));

		getBean();

		assertEquals("namespace", bean.string);

		getPropBagEx();

		assertEquals("namespace", xml.getNode("namespace"));
	}

	public void testNamespace2()
	{
		mappings.setIgnoreNS(false);
		mappings.addNodeMapping(new NodeMapping("string", "prefix:namespace"));

		getBean();

		assertEquals("namespace", bean.string);

		getPropBagEx();

		assertEquals("namespace", xml.getNode("namespace"));
	}

	public void testAttributeNamespace()
	{
		mappings.addNodeMapping(new NodeMapping("attribute1", "@xml:base"));

		getBean();

		assertEquals("attribute", bean.attribute1);

		getPropBagEx();

		assertEquals("attribute", xml.getNode("@xml:base"));
	}

	public void testReferences()
	{
		mappings.addNodeMapping(new DataMapping("parent", "parent", TestBean.class));

		getBean();

		assertSame(bean, bean.parent.parent);

		getPropBagEx();

		assertEquals("../..", xml.getNode("parent/parent/@reference"));
	}

	public void testNullsInCollections()
	{
		mappings.addNodeMapping(new CollectionMapping("collection", "collection/string"));

		getBean();

		bean.collection.add(null);
		bean.collection.add("Nonnull");
		bean.collection.add(null);
		bean.collection.add("Nonnull2");
		getPropBagEx();

		assertEquals(4, xml.nodeCount("collection/string"));
	}

	public void testChildObject()
	{
		mappings.addNodeMapping(new DataMapping("parent", "parent", TestBean.class));

		getBean();

		assertSame(bean, bean.parent.actualParent);
	}

	public void testSubclass()
	{
		mappings.addNodeMapping(new DataMapping("subclasser", "subclasser", TestBean2.class));
		mappings2.addNodeMapping(new NodeMapping("string", "string"));

		getBean();

		assertTrue(bean.subclasser.getClass() == OverideBean.class);

		getPropBagEx();

		assertEquals(OverideBean.class.getName(), xml.getNode("subclasser/@xclass"));
	}

	public void testClassResolver()
	{
		XMLDataResolver resolver = new XMLDataResolver()
		{
			@Override
			public Class resolveClass(HierarchicalStreamReader reader)
			{
				String type = reader.getAttribute("type");
				Class clazz = null;
				if( type.equals("override") )
				{
					clazz = OverideBean.class;
				}
				else if( type.equals("testbean2") )
				{
					clazz = TestBean2.class;
				}
				return clazz;
			}

			@Override
			public void writeClass(HierarchicalStreamWriter writer, Object object)
			{
				String type = null;
				if( object.getClass() == OverideBean.class )
				{
					type = "override";
				}
				else
				{
					type = "testbean2";
				}
				writer.addAttribute("type", type);
			}
		};
		resolverTest(resolver);
		assertEquals("testbean2", xml.getNode("resolver2/@type"));
	}

	public void testClassResolverMapping()
	{
		XMLDataResolverMapping resolver = new XMLDataResolverMapping("type");
		resolver.addMapping("override", OverideBean.class);
		resolver.addMapping("testbean2", TestBean2.class);
		resolverTest(resolver);
		assertEquals("testbean2", xml.getNode("resolver2/@type"));
	}

	public void testClassResolverMappingDefault()
	{
		XMLDataResolverMapping resolver = new XMLDataResolverMapping("type", TestBean2.class);
		resolver.addMapping("override", OverideBean.class);
		resolverTest(resolver);
	}

	private void resolverTest(XMLDataResolver resolver)
	{
		mappings.addNodeMapping(new DataMapping("subclasser", "resolver", TestBean2.class, resolver));
		mappings.addNodeMapping(new DataMapping("subclasser2", "resolver2", TestBean2.class, resolver));
		mappings2.addNodeMapping(new NodeMapping("string", "string"));

		getBean();

		assertEquals(OverideBean.class, bean.subclasser.getClass());
		assertEquals("resolved", bean.subclasser.getString());

		assertEquals(TestBean2.class, bean.subclasser2.getClass());
		assertEquals("resolved", bean.subclasser2.getString());

		getPropBagEx();

		assertEquals("override", xml.getNode("resolver/@type"));
		assertEquals("resolved", xml.getNode("resolver/string"));

		assertEquals("resolved", xml.getNode("resolver2/string"));
	}

	private boolean checkPathContainValues(PropBagEx xml, String path, String... values)
	{
		Set<String> vs = new HashSet<String>(Arrays.asList(values));
		for( String v : xml.iterateAllValues(path) )
		{
			if( !vs.remove(v) )
			{
				return false;
			}
		}
		return true;
	}

	public void testSimpleMap()
	{
		mappings.addNodeMapping(new MapMapping("map", "map", "node/@key", "node/value"));

		getBean();

		assertEquals(2, bean.map.size());
		assertEquals("valuea", bean.map.get("a"));
		assertEquals("valueb", bean.map.get("b"));

		getPropBagEx();

		assertTrue(checkPathContainValues(xml, "map/node/value", "valuea", "valueb"));
	}

	public void testBasicDataMap()
	{
		mappings2.addNodeMapping(new NodeMapping("string", "data"));

		mappings.addNodeMapping(new DataMapMapping("map", "datamap", "data/key/data", "data/value", TestBean2.class));

		getBean();

		assertEquals(2, bean.map.size());
		assertEquals(new TestBean2("valuea"), bean.map.get("a"));
		assertEquals(new TestBean2("valueb"), bean.map.get("b"));

		getPropBagEx();

		assertEquals(2, xml.nodeCount("datamap/data"));
		assertTrue(checkPathContainValues(xml, "datamap/data/value/data", "valuea", "valueb"));
	}

	public void testDataMap()
	{
		mappings2.addNodeMapping(new NodeMapping("string", "data"));

		mappings.addNodeMapping(
			new DataMapMapping("map", "datamap", "data/key", "data/value", TestBean2.class, TestBean2.class));

		getBean();

		assertEquals(2, bean.map.size());
		assertEquals(new TestBean2("valuea"), bean.map.get(new TestBean2("a")));
		assertEquals(new TestBean2("valueb"), bean.map.get(new TestBean2("b")));

		getPropBagEx();

		assertEquals(2, xml.nodeCount("datamap/data"));
		assertTrue(checkPathContainValues(xml, "datamap/data/value/data", "valuea", "valueb"));
	}

	public void testTypeMap()
	{
		BiMap<String, Integer> map = HashBiMap.create();
		map.put("test1", 1);
		map.put("test2", 2);
		map.put("test3", 3);
		map.put("test4", 4);

		mappings.addNodeMapping(

		new ListMapping("collection", "types/type", ArrayList.class, new NodeTypeMapping("", "", map, new Integer(4))));
		getBean();

		assertEquals(1, bean.collection.get(0));
		assertEquals(2, bean.collection.get(1));
		assertEquals(3, bean.collection.get(2));
		assertEquals(4, bean.collection.get(3));

		getPropBagEx();

		assertEquals("test1", xml.getNode("types/type[0]"));
		assertEquals("test2", xml.getNode("types/type[1]"));
		assertEquals("test3", xml.getNode("types/type[2]"));
		assertEquals("test4", xml.getNode("types/type[3]"));
	}

	public void testNotNullBean()
	{
		TestBean bean2 = new TestBean();
		mappings.addNodeMapping(new DataMapping("notNullBean", "bean", TestBean2.class));
		getBean(bean2);

		assertSame(bean2.notNullBean, bean.notNullBean);

		getPropBagEx();

		assertEquals(true, xml.nodeExists("bean"));
	}

	public static class TestBean implements XMLData, XMLDataChild
	{
		private static final long serialVersionUID = 1L;

		public TestBean actualParent;
		public TestBean2 notNullBean = new TestBean2();
		public String notNull = "";
		public String string;
		public String another;
		public String attribute2;
		public String attribute1;
		public String defaultValue;
		public int integer;
		public boolean bool;
		public float floating;
		public double doub;
		public short shorter;
		public Date date;
		public TestBean2 bean;
		public Node element;
		public PropBagEx xml;
		public URL url;
		public List collection;
		public List collection2;
		public TestBean parent;
		public TestBean2 subclasser;
		public TestBean2 subclasser2;
		public Map map;

		@Override
		public XMLDataMappings getMappings()
		{
			return XMLDataConverterTest.mappings;
		}

		@Override
		public void setParentObject(Object o)
		{
			actualParent = (TestBean) o;
		}
	}

	private static class TestBean2 implements XMLData
	{
		private static final long serialVersionUID = 1L;

		private String string;

		public TestBean2()
		{
			super();
		}

		public TestBean2(String string)
		{
			this.string = string;
		}

		String getString()
		{
			return string;
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.common.xml.XMLData#getMappings()
		 */
		@Override
		public XMLDataMappings getMappings()
		{
			return mappings2;
		}

		@Override
		public boolean equals(Object obj)
		{
			return string.equals(obj.toString());
		}

		@Override
		public int hashCode()
		{
			return string.hashCode();
		}

		@Override
		public String toString()
		{
			return string;
		}
	}

	private static class OverideBean extends TestBean2
	{
		private static final long serialVersionUID = 1L;
		// Empty on purpose!
	}
}
