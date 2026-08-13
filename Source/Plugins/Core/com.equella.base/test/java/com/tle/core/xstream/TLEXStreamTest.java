/*
 * Created on Jul 7, 2005
 */
package com.tle.core.xstream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dytech.devlib.PropBagEx;
import org.junit.jupiter.api.Test;

public class TLEXStreamTest {
  private static final TLEXStream xstream = TLEXStream.instance();

  @Test
  public void testPropBagWriter() {
    TestBean bean = new TestBean();
    bean.string = "string";
    PropBagEx xml = xstream.toPropBag(bean, "test");
    assertEquals("string", xml.getNode("string"));
    assertEquals("test", xml.getNodeName());
  }

  @Test
  public void testPropBagReader() {
    PropBagEx xml = new PropBagEx();
    xml.setNode("string", "string");
    TestBean bean = (TestBean) xstream.fromXML(xml, TestBean.class);
    assertEquals("string", bean.string);
  }

  private static final class TestBean {
    String string;
  }
}
