/*
 * Created on Dec 21, 2004
 */
package com.dytech.devlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

@SuppressWarnings("nls")
public class PropBagExTest extends TestCase {
  private static final String DOC1 = "doc1.xml";
  private static final String DOC2 = "doc2.xml";

  private PropBagEx doc1;
  private PropBagEx doc2;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    doc1 = new PropBagEx(getClass().getResourceAsStream(DOC1));
    doc2 = new PropBagEx(getClass().getResourceAsStream(DOC2));
  }

  @Override
  protected void tearDown() throws Exception {
    doc1 = null;
    doc2 = null;

    super.tearDown();
  }

  public void testIteratorInForEachLoop() {
    for (final PropBagEx xml : doc1.iterator()) {
      // Do nothing!
      xml.hashCode();
    }
  }

  public void testGetNode() {
    assertEquals(doc1.getNode("result/xml/a"), "1");
    assertEquals(doc1.getNode("result[1]/xml/a"), "4");

    assertEquals(doc1.getNode("@count"), "5");
    assertEquals(doc1.getNode("result/xml/@id"), "first");
    assertEquals(doc1.getNode("result[1]/xml/@id"), "second");

    assertEquals(doc1.getNode("non/existant/node"), "");

    assertEquals(doc1.getNode("result////xml////a"), "1");
    assertEquals(doc1.getNode("result/xml/a//////"), "1");
  }

  public void testGetNodeList() {
    final List<String> results1 = doc1.getNodeList("result/xml/a");
    assertEquals(results1.size(), 3);
    assertEquals(results1.get(0), "1");
    assertEquals(results1.get(1), "2");
    assertEquals(results1.get(2), "3");

    final List<String> results2 = doc1.getNodeList("result/xml/@id");
    assertEquals(results2.size(), 1);
    assertEquals(results2.get(0), "first");

    final List<String> results3 = doc1.getNodeList("non/existant/node");
    assertEquals(results3.size(), 0);

    final List<String> results4 = doc1.getNodeList("result/xml/doesntexist");
    assertEquals(results4.size(), 0);
  }

  public void testGetIntNode() {
    assertEquals(doc1.getIntNode("result/xml/a"), 1);
    assertEquals(doc1.getIntNode("result[1]/xml/a"), 4);
    assertEquals(doc1.getIntNode("@count"), 5);

    // Check handling of non-number values
    assertEquals(doc1.getIntNode("result/xml/b", 12345), 12345);
    try {
      doc1.getIntNode("result/xml/c");
      assertTrue("NumberFormatException should have been thrown", false);
    } catch (final NumberFormatException ex) {
      // This is expected.
    }
  }

  public void testGetAttributesForNode() {
    final Map attributes = doc1.getAttributesForNode("result/xml");

    assertEquals(attributes.size(), 4);

    assertEquals(attributes.get("id"), "first");
    assertEquals(attributes.get("attr1"), "1");
    assertEquals(attributes.get("attr2"), "2");
    assertEquals(attributes.get("attr3"), "3");

    assertNull(attributes.get("non-existant"));
  }

  public void testSetNode() {
    doc1.setNode("result/xml/a", "newvalue1");
    assertEquals(doc1.getNode("result/xml/a"), "newvalue1");

    doc1.setNode("result/xml/a[2]", "newvalue2");
    assertEquals(doc1.getNode("result/xml/a[2]"), "newvalue2");

    doc1.setNode("result/xml/@id", "newvalue3");
    assertEquals(doc1.getNode("result/xml/@id"), "newvalue3");

    doc1.setNode("@newnode", "newvalue4");
    assertEquals(doc1.getNode("@newnode"), "newvalue4");

    doc1.setNode("@count", 12345);
    assertEquals(doc1.getIntNode("@count"), 12345);
  }

  public void testSetIfNotNull() {
    doc1.setIfNotNull("result/xml/a[2]", "");
    assertEquals(doc1.getNode("result/xml/a[2]", null), "");

    doc1.setIfNotNull("result/xml/a[3]", null);
    assertFalse(doc1.nodeExists("result/xml/a[3]"));
  }

  public void testSetIfNotEmpty() {
    doc1.setIfNotEmpty("result/xml/a[2]", "blah");
    assertEquals(doc1.getNode("result/xml/a[2]"), "blah");

    doc1.setIfNotEmpty("result/xml/a[3]", "");
    assertFalse(doc1.nodeExists("result/xml/a[3]"));

    doc1.setIfNotEmpty("result/xml/a[4]", null);
    assertFalse(doc1.nodeExists("result/xml/a[4]"));
  }

  public void testIterator() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<PropBagEx> docIter = doc1.iterator();
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode("xml/@id");
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIteratorWithPath() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<PropBagEx> docIter = doc1.iterator("result");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      assertEquals(subdoc.getNode("xml/@id"), expect);
    }
    checkIterators(docIter, values);
  }

  public void testIteratorWithStar() {
    final Iterator<String> values = valuesForFirstResultXmlChildren();
    final Iterator<PropBagEx> docIter = doc1.iterator("result/xml/*");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAll() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAllWithManySlash() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("//result///xml/a///");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAllWithStar() {
    final Iterator<String> values = valuesForAllResultXmlChildren();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("result/xml/*");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAllWithMoreStars() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("*/*/a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateValues() {
    final Iterator<String> values = valuesForFirstResultXmlA();
    final Iterator<String> docIter = doc1.iterateValues("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAllValues() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<String> docIter = doc1.iterateAllValues("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testIterateAllValuesForAttributes() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<String> docIter = doc1.iterateAllValues("result/xml/@id");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testNodeCount() {
    assertEquals(doc1.nodeCount("result"), 5);
    assertEquals(doc1.nodeCount("result/xml/a"), 3);
    assertEquals(doc1.nodeCount("result/xml/a/@test"), 1);
    assertEquals(doc1.nodeCount("result/xml/*"), 5);
    assertEquals(doc1.nodeCount("result/xml/@id"), 1);
    assertEquals(doc1.nodeCount("does/not/exist"), 0);
    assertEquals(doc1.nodeCount("result/@none"), 0);
    assertEquals(doc1.nodeCount(""), 1);
    assertEquals(doc1.nodeCount("/"), 1);
    assertEquals(doc1.nodeCount("*"), 5);
  }

  public void testNodeExists() {
    assertTrue(doc1.nodeExists("result"));
    assertTrue(doc1.nodeExists("result/xml/a"));
    assertTrue(doc1.nodeExists("result/xml/@id"));

    assertFalse(doc1.nodeExists("result/xml/nope"));
    assertFalse(doc1.nodeExists("result/xml/@extinct"));
    assertFalse(doc1.nodeExists("does/not/exist"));
  }

  public void testDeleteNode() {
    assertTrue(doc1.deleteNode("result"));
    assertEquals(doc1.getNode("result/xml/@id"), "second");
    assertEquals(doc1.nodeCount("result"), 4);

    assertTrue(doc1.deleteNode("result[2]/xml/a"));
    assertTrue(doc1.nodeExists("result[2]/xml"));
    assertTrue(doc1.nodeExists("result[2]/xml/a"));
    assertEquals(doc1.nodeCount("result[2]/xml/a"), 1);

    assertTrue(doc1.deleteNode("result[2]/xml/a"));
    assertFalse(doc1.nodeExists("result[2]/xml/a"));
    assertTrue(doc1.nodeExists("result[2]/xml"));
    assertEquals(doc1.nodeCount("result[2]/xml/a"), 0);

    assertTrue(doc1.deleteNode("@count"));
    assertFalse(doc1.nodeExists("@count"));
  }

  public void testDeleteAll() {
    assertTrue(doc1.deleteAll("result"));
    assertFalse(doc1.nodeExists("result"));

    assertFalse(doc1.deleteAll("result"));
  }

  public void testGetNodeName() {
    assertEquals(doc1.getNodeName(), "results");

    final PropBagEx subdoc1 = doc1.getSubtree("result/xml");
    assertEquals(subdoc1.getNodeName(), "xml");
  }

  public void testEqualsDOM() {
    assertTrue(doc1.equalsDOM(doc1));

    final PropBagEx newdoc = new PropBagEx();
    assertFalse(doc1.equalsDOM(newdoc));

    assertFalse(doc1.equalsDOM(null));
  }

  public void testGetSubtree() {
    final PropBagEx subdoc1 = doc1.getSubtree("result/xml/a");
    assertNotNull(subdoc1);
    assertEquals(subdoc1.getNode(), "1");

    final PropBagEx subdoc2 = doc1.getSubtree("result[2]/xml/a");
    assertNotNull(subdoc2);
    assertEquals(subdoc2.getNode(), "5");

    final PropBagEx subdoc3 = doc1.getSubtree("result/xml/some/non/existant/tree");
    assertNull(subdoc3);
  }

  public void testNewSubtree() {
    // Check it does not already exist.
    final PropBagEx subdoc1 = doc1.getSubtree("newtree/here");
    assertNull(subdoc1);

    final PropBagEx subdoc2 = doc1.newSubtree("newtree/here");
    assertNotNull(subdoc2);

    subdoc2.setNode("@check", "yes");
    final PropBagEx subdoc3 = doc1.getSubtree("newtree/here");
    assertNotNull(subdoc3);
    assertEquals(subdoc3.getNode("@check"), "yes");

    final PropBagEx subdoc4 = doc1.newSubtree("newtree/here");
    assertNotNull(subdoc4);
    assertEquals(subdoc4.getNode("@check"), "");

    assertEquals(doc1.nodeCount("newtree"), 1);
    assertEquals(doc1.nodeCount("newtree/here"), 2);
  }

  public void testAquireSubtree() {
    // Check it does not already exist.
    final PropBagEx subdoc1 = doc1.getSubtree("newtree/here");
    assertNull(subdoc1);

    final PropBagEx subdoc2 = doc1.aquireSubtree("newtree/here");
    assertNotNull(subdoc2);
    assertEquals(doc1.nodeCount("newtree/here"), 1);

    final PropBagEx subdoc3 = doc1.aquireSubtree("newtree/here");
    assertNotNull(subdoc3);
    assertEquals(doc1.nodeCount("newtree"), 1);
    assertEquals(doc1.nodeCount("newtree/here"), 1);
  }

  public void testAppend() {
    final PropBagEx subdoc1 = doc2.newSubtree("append1");
    subdoc1.append("", doc1);
    assertEquals(doc2.getIntNode("append1/results/@count"), 5);

    doc2.append("append2", doc1);
    assertEquals(doc2.getIntNode("append2/results/@count"), 5);
  }

  public void testAppendChildren() {
    final PropBagEx subdoc1 = doc2.newSubtree("append1");
    subdoc1.appendChildren("", doc1);
    assertEquals(doc2.getNode("append1/result/xml/@id"), "first");

    doc2.appendChildren("append2", doc1);
    assertEquals(doc2.getNode("append2/result/xml/@id"), "first");
  }

  public void testAttributeNamespaces() {
    final PropBagEx namespacedoc = new PropBagEx("<namespacetest xml:base=\"basevalue\"/>");
    assertEquals(namespacedoc.getNode("@xml:base"), "basevalue");
  }

  public void testRootNode() {
    final PropBagEx subtree = doc1.getSubtree("result[4]/xml/node[1]");
    final List<String> values = subtree.getNodeList("");
    assertEquals(1, values.size());
    assertEquals("value2", values.get(0));
    assertEquals(1, subtree.nodeCount(""));
  }

  public void testSetNodeName() {
    // Test renaming a subtree
    assertTrue(doc2.nodeExists("child"));
    assertFalse(doc2.nodeExists("renamed.child"));
    doc2.getSubtree("child").setNodeName("renamed.child");
    assertFalse(doc2.nodeExists("child"));
    assertTrue(doc2.nodeExists("renamed.child"));

    // Test renaming the document root
    assertEquals(doc2.getNodeName(), "xml");
    doc2.setNodeName("new.root.name");
    assertEquals(doc2.getNodeName(), "new.root.name");
  }

  // Redmine #2459
  public void testControlCharsReRead() {
    final PropBagEx bag = new PropBagEx("<xml/>");
    bag.setNode("/test", "\u0003\u0008\u0009");
    final String xml = bag.toString();
    final PropBagEx newBag = new PropBagEx(xml);

    // control characters are lost. this is expected
    final PropBagEx expected = new PropBagEx("<xml><test>\t</test></xml>");
    assertEquals(newBag.toString(), expected.toString());
  }

  public void testControlCharsBulkRead() {
    final PropBagEx bag = new PropBagEx("<xml><node1>\u0003&amp;&#x0B;\u0009</node1></xml>");
    assertEquals("&\t", bag.getNode("node1"));
    final String xml = bag.toString();
    assertEquals("<xml><node1>&amp;\t</node1></xml>", xml);
  }

  public void testEscapedChars() {
    final PropBagEx escp = new PropBagEx(getClass().getResourceAsStream("escaped.xml"));
    assertEquals("Escape char tab: \t", escp.getNode("/node1"));
    assertEquals("Some more text with an &", escp.getNode("/node2"));
    assertEquals("ball&shank", escp.getNode("/node3/@test"));
  }

  public void testIterateAllNodesWithName() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAllNodesWithName("a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(value, expect);
    }
    checkIterators(docIter, values);
  }

  public void testDeleteSubtree() {
    final PropBagEx sub = doc1.getSubtree("result[4]");
    assertNotNull(sub);
    doc1.deleteSubtree(sub);
    assertNull(doc1.getSubtree("result[4]"));

    final PropBagEx sub2 = doc1.getSubtree("result/xml");
    doc1.deleteSubtree(sub2);

    // now verify result[0] is empty
    final PropBagEx result0 = doc1.getSubtree("result[0]");
    assertTrue(result0.getNodeList("*").isEmpty());
  }

  // ////// HELPERS //////////////////////////////////////////////////////////

  private Iterator<String> valuesForResultXmlIdAttribute() {
    final Collection<String> values = new ArrayList<String>();
    values.add("first");
    values.add("second");
    values.add("third");
    values.add("fourth");
    values.add("fifth");
    return values.iterator();
  }

  private Iterator<String> valuesForFirstResultXmlA() {
    final Collection<String> values = new ArrayList<String>();
    values.add("1");
    values.add("2");
    values.add("3");
    return values.iterator();
  }

  private Iterator<String> valuesForAllResultXmlA() {
    final Collection<String> values = new ArrayList<String>();
    values.add("1");
    values.add("2");
    values.add("3");
    values.add("4");
    values.add("5");
    values.add("6");
    values.add("7");
    return values.iterator();
  }

  private Iterator<String> valuesForAllResultXmlChildren() {
    final Collection<String> values = new ArrayList<String>();
    values.add("1");
    values.add("2");
    values.add("xx");
    values.add("yy");
    values.add("3");
    values.add("4");
    values.add("5");
    values.add("6");
    values.add("7");
    values.add("value1");
    values.add("value2");
    values.add("value3");
    return values.iterator();
  }

  private Iterator<String> valuesForFirstResultXmlChildren() {
    final Collection<String> values = new ArrayList<String>();
    values.add("1");
    values.add("2");
    values.add("xx");
    values.add("yy");
    values.add("3");
    return values.iterator();
  }

  private void checkIterators(final Iterator<?> source, final Iterator<?> values) {
    if (source.hasNext()) {
      fail("Document has more elements to iterate");
    }

    if (values.hasNext()) {
      fail("Document should have more elements to iterate");
    }
  }
}
