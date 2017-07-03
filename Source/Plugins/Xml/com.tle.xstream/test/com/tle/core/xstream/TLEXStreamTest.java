/*
 * Created on Jul 7, 2005
 */
package com.tle.core.xstream;

import com.dytech.devlib.PropBagEx;
import com.tle.core.xstream.TLEXStream;

import junit.framework.TestCase;

public class TLEXStreamTest extends TestCase
{
	private static TLEXStream xstream;

	@Override
	protected void setUp()
	{
		if( xstream == null )
		{
			xstream = TLEXStream.instance();
		}
	}

	public void testPropBagWriter()
	{
		TestBean bean = new TestBean();
		bean.string = "string";
		PropBagEx xml = xstream.toPropBag(bean, "test");
		assertEquals("string", xml.getNode("string"));
		assertEquals("test", xml.getNodeName());
	}

	public void testPropBagReader()
	{
		PropBagEx xml = new PropBagEx();
		xml.setNode("string", "string");
		TestBean bean = (TestBean) xstream.fromXML(xml, TestBean.class);
		assertEquals("string", bean.string);
	}

	private static class TestBean
	{
		String string;
	}
}
