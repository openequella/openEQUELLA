/*
 * Created on Dec 21, 2004
 */
package com.dytech.devlib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropBagExTest {
  private static final String DOC1 = "doc1.xml";
  private static final String DOC2 = "doc2.xml";

  private PropBagEx doc1;
  private PropBagEx doc2;

  @BeforeEach
  void setUp() throws Exception {
    doc1 = new PropBagEx(getClass().getResourceAsStream(DOC1));
    doc2 = new PropBagEx(getClass().getResourceAsStream(DOC2));
  }

  @Test
  public void testIteratorInForEachLoop() {
    for (final PropBagEx xml : doc1.iterator()) {
      // Do nothing!
      xml.hashCode();
    }
  }

  @Test
  public void testGetNode() {
    assertEquals("1", doc1.getNode("result/xml/a"));
    assertEquals("4", doc1.getNode("result[1]/xml/a"));

    assertEquals("5", doc1.getNode("@count"));
    assertEquals("first", doc1.getNode("result/xml/@id"));
    assertEquals("second", doc1.getNode("result[1]/xml/@id"));

    assertEquals("", doc1.getNode("non/existant/node"));

    assertEquals("1", doc1.getNode("result////xml////a"));
    assertEquals("1", doc1.getNode("result/xml/a//////"));
  }

  @Test
  public void testGetNodeList() {
    final List<String> results1 = doc1.getNodeList("result/xml/a");
    assertEquals(3, results1.size());
    assertEquals("1", results1.get(0));
    assertEquals("2", results1.get(1));
    assertEquals("3", results1.get(2));

    final List<String> results2 = doc1.getNodeList("result/xml/@id");
    assertEquals(1, results2.size());
    assertEquals("first", results2.get(0));

    final List<String> results3 = doc1.getNodeList("non/existant/node");
    assertEquals(0, results3.size());

    final List<String> results4 = doc1.getNodeList("result/xml/doesntexist");
    assertEquals(0, results4.size());
  }

  @Test
  public void testGetIntNode() {
    assertEquals(1, doc1.getIntNode("result/xml/a"));
    assertEquals(4, doc1.getIntNode("result[1]/xml/a"));
    assertEquals(5, doc1.getIntNode("@count"));

    // Check handling of non-number values
    assertEquals(12345, doc1.getIntNode("result/xml/b", 12345));
    try {
      doc1.getIntNode("result/xml/c");
      fail("NumberFormatException should have been thrown");
    } catch (final NumberFormatException ex) {
      // This is expected.
    }
  }

  @Test
  public void testGetAttributesForNode() {
    final Map attributes = doc1.getAttributesForNode("result/xml");

    assertEquals(4, attributes.size());

    assertEquals("first", attributes.get("id"));
    assertEquals("1", attributes.get("attr1"));
    assertEquals("2", attributes.get("attr2"));
    assertEquals("3", attributes.get("attr3"));

    assertNull(attributes.get("non-existant"));
  }

  @Test
  public void testSetNode() {
    doc1.setNode("result/xml/a", "newvalue1");
    assertEquals("newvalue1", doc1.getNode("result/xml/a"));

    doc1.setNode("result/xml/a[2]", "newvalue2");
    assertEquals("newvalue2", doc1.getNode("result/xml/a[2]"));

    doc1.setNode("result/xml/@id", "newvalue3");
    assertEquals("newvalue3", doc1.getNode("result/xml/@id"));

    doc1.setNode("@newnode", "newvalue4");
    assertEquals("newvalue4", doc1.getNode("@newnode"));

    doc1.setNode("@count", 12345);
    assertEquals(12345, doc1.getIntNode("@count"));
  }

  @Test
  public void testSetIfNotNull() {
    doc1.setIfNotNull("result/xml/a[2]", "");
    assertEquals("", doc1.getNode("result/xml/a[2]", null));

    doc1.setIfNotNull("result/xml/a[3]", null);
    assertFalse(doc1.nodeExists("result/xml/a[3]"));
  }

  @Test
  public void testSetIfNotEmpty() {
    doc1.setIfNotEmpty("result/xml/a[2]", "blah");
    assertEquals("blah", doc1.getNode("result/xml/a[2]"));

    doc1.setIfNotEmpty("result/xml/a[3]", "");
    assertFalse(doc1.nodeExists("result/xml/a[3]"));

    doc1.setIfNotEmpty("result/xml/a[4]", null);
    assertFalse(doc1.nodeExists("result/xml/a[4]"));
  }

  @Test
  public void testIterator() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<PropBagEx> docIter = doc1.iterator();
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode("xml/@id");
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIteratorWithPath() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<PropBagEx> docIter = doc1.iterator("result");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      assertEquals(expect, subdoc.getNode("xml/@id"));
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIteratorWithStar() {
    final Iterator<String> values = valuesForFirstResultXmlChildren();
    final Iterator<PropBagEx> docIter = doc1.iterator("result/xml/*");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAll() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAllWithManySlash() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("//result///xml/a///");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAllWithStar() {
    final Iterator<String> values = valuesForAllResultXmlChildren();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("result/xml/*");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAllWithMoreStars() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAll("*/*/a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateValues() {
    final Iterator<String> values = valuesForFirstResultXmlA();
    final Iterator<String> docIter = doc1.iterateValues("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAllValues() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<String> docIter = doc1.iterateAllValues("result/xml/a");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testIterateAllValuesForAttributes() {
    final Iterator<String> values = valuesForResultXmlIdAttribute();
    final Iterator<String> docIter = doc1.iterateAllValues("result/xml/@id");
    while (docIter.hasNext() && values.hasNext()) {
      final String value = docIter.next();
      final String expect = values.next();

      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
  public void testNodeCount() {
    assertEquals(5, doc1.nodeCount("result"));
    assertEquals(3, doc1.nodeCount("result/xml/a"));
    assertEquals(1, doc1.nodeCount("result/xml/a/@test"));
    assertEquals(5, doc1.nodeCount("result/xml/*"));
    assertEquals(1, doc1.nodeCount("result/xml/@id"));
    assertEquals(0, doc1.nodeCount("does/not/exist"));
    assertEquals(0, doc1.nodeCount("result/@none"));
    assertEquals(1, doc1.nodeCount(""));
    assertEquals(1, doc1.nodeCount("/"));
    assertEquals(5, doc1.nodeCount("*"));
  }

  @Test
  public void testNodeExists() {
    assertTrue(doc1.nodeExists("result"));
    assertTrue(doc1.nodeExists("result/xml/a"));
    assertTrue(doc1.nodeExists("result/xml/@id"));

    assertFalse(doc1.nodeExists("result/xml/nope"));
    assertFalse(doc1.nodeExists("result/xml/@extinct"));
    assertFalse(doc1.nodeExists("does/not/exist"));
  }

  @Test
  public void testDeleteNode() {
    assertTrue(doc1.deleteNode("result"));
    assertEquals("second", doc1.getNode("result/xml/@id"));
    assertEquals(4, doc1.nodeCount("result"));

    assertTrue(doc1.deleteNode("result[2]/xml/a"));
    assertTrue(doc1.nodeExists("result[2]/xml"));
    assertTrue(doc1.nodeExists("result[2]/xml/a"));
    assertEquals(1, doc1.nodeCount("result[2]/xml/a"));

    assertTrue(doc1.deleteNode("result[2]/xml/a"));
    assertFalse(doc1.nodeExists("result[2]/xml/a"));
    assertTrue(doc1.nodeExists("result[2]/xml"));
    assertEquals(0, doc1.nodeCount("result[2]/xml/a"));

    assertTrue(doc1.deleteNode("@count"));
    assertFalse(doc1.nodeExists("@count"));
  }

  @Test
  public void testDeleteAll() {
    assertTrue(doc1.deleteAll("result"));
    assertFalse(doc1.nodeExists("result"));

    assertFalse(doc1.deleteAll("result"));
  }

  @Test
  public void testGetNodeName() {
    assertEquals("results", doc1.getNodeName());

    final PropBagEx subdoc1 = doc1.getSubtree("result/xml");
    assertEquals("xml", subdoc1.getNodeName());
  }

  @Test
  public void testEqualsDOM() {
    assertTrue(doc1.equalsDOM(doc1));

    final PropBagEx newdoc = new PropBagEx();
    assertFalse(doc1.equalsDOM(newdoc));

    assertFalse(doc1.equalsDOM(null));
  }

  @Test
  public void testGetSubtree() {
    final PropBagEx subdoc1 = doc1.getSubtree("result/xml/a");
    assertNotNull(subdoc1);
    assertEquals("1", subdoc1.getNode());

    final PropBagEx subdoc2 = doc1.getSubtree("result[2]/xml/a");
    assertNotNull(subdoc2);
    assertEquals("5", subdoc2.getNode());

    final PropBagEx subdoc3 = doc1.getSubtree("result/xml/some/non/existant/tree");
    assertNull(subdoc3);
  }

  @Test
  public void testNewSubtree() {
    // Check it does not already exist.
    final PropBagEx subdoc1 = doc1.getSubtree("newtree/here");
    assertNull(subdoc1);

    final PropBagEx subdoc2 = doc1.newSubtree("newtree/here");
    assertNotNull(subdoc2);

    subdoc2.setNode("@check", "yes");
    final PropBagEx subdoc3 = doc1.getSubtree("newtree/here");
    assertNotNull(subdoc3);
    assertEquals("yes", subdoc3.getNode("@check"));

    final PropBagEx subdoc4 = doc1.newSubtree("newtree/here");
    assertNotNull(subdoc4);
    assertEquals("", subdoc4.getNode("@check"));

    assertEquals(1, doc1.nodeCount("newtree"));
    assertEquals(2, doc1.nodeCount("newtree/here"));
  }

  @Test
  public void testAquireSubtree() {
    // Check it does not already exist.
    final PropBagEx subdoc1 = doc1.getSubtree("newtree/here");
    assertNull(subdoc1);

    final PropBagEx subdoc2 = doc1.aquireSubtree("newtree/here");
    assertNotNull(subdoc2);
    assertEquals(1, doc1.nodeCount("newtree/here"));

    final PropBagEx subdoc3 = doc1.aquireSubtree("newtree/here");
    assertNotNull(subdoc3);
    assertEquals(1, doc1.nodeCount("newtree"));
    assertEquals(1, doc1.nodeCount("newtree/here"));
  }

  @Test
  public void testAppend() {
    final PropBagEx subdoc1 = doc2.newSubtree("append1");
    subdoc1.append("", doc1);
    assertEquals(5, doc2.getIntNode("append1/results/@count"));

    doc2.append("append2", doc1);
    assertEquals(5, doc2.getIntNode("append2/results/@count"));
  }

  @Test
  public void testAppendChildren() {
    final PropBagEx subdoc1 = doc2.newSubtree("append1");
    subdoc1.appendChildren("", doc1);
    assertEquals("first", doc2.getNode("append1/result/xml/@id"));

    doc2.appendChildren("append2", doc1);
    assertEquals("first", doc2.getNode("append2/result/xml/@id"));
  }

  @Test
  public void testAttributeNamespaces() {
    final PropBagEx namespacedoc = new PropBagEx("<namespacetest xml:base=\"basevalue\"/>");
    assertEquals("basevalue", namespacedoc.getNode("@xml:base"));
  }

  @Test
  public void testRootNode() {
    final PropBagEx subtree = doc1.getSubtree("result[4]/xml/node[1]");
    final List<String> values = subtree.getNodeList("");
    assertEquals(1, values.size());
    assertEquals("value2", values.get(0));
    assertEquals(1, subtree.nodeCount(""));
  }

  @Test
  public void testSetNodeName() {
    // Test renaming a subtree
    assertTrue(doc2.nodeExists("child"));
    assertFalse(doc2.nodeExists("renamed.child"));
    doc2.getSubtree("child").setNodeName("renamed.child");
    assertFalse(doc2.nodeExists("child"));
    assertTrue(doc2.nodeExists("renamed.child"));

    // Test renaming the document root
    assertEquals("xml", doc2.getNodeName());
    doc2.setNodeName("new.root.name");
    assertEquals("new.root.name", doc2.getNodeName());
  }

  // Redmine #2459
  @Test
  public void testControlCharsReRead() {
    final PropBagEx bag = new PropBagEx("<xml/>");
    bag.setNode("/test", "\u0003\u0008\u0009");
    final String xml = bag.toString();
    final PropBagEx newBag = new PropBagEx(xml);

    // control characters are lost. this is expected
    final PropBagEx expected = new PropBagEx("<xml><test>\t</test></xml>");
    assertEquals(expected.toString(), newBag.toString());
  }

  @Test
  public void testControlCharsBulkRead() {
    final PropBagEx bag = new PropBagEx("<xml><node1>\u0003&amp;&#x0B;\u0009</node1></xml>");
    assertEquals("&\t", bag.getNode("node1"));
    final String xml = bag.toString();
    assertEquals("<xml><node1>&amp;\t</node1></xml>", xml);
  }

  @Test
  public void testEscapedChars() {
    final PropBagEx escp = new PropBagEx(getClass().getResourceAsStream("escaped.xml"));
    assertEquals("Escape char tab: \t", escp.getNode("/node1"));
    assertEquals("Some more text with an &", escp.getNode("/node2"));
    assertEquals("ball&shank", escp.getNode("/node3/@test"));
  }

  @Test
  public void testIterateAllNodesWithName() {
    final Iterator<String> values = valuesForAllResultXmlA();
    final Iterator<PropBagEx> docIter = doc1.iterateAllNodesWithName("a");
    while (docIter.hasNext() && values.hasNext()) {
      final PropBagEx subdoc = docIter.next();
      final String expect = values.next();

      final String value = subdoc.getNode();
      assertEquals(expect, value);
    }
    checkIterators(docIter, values);
  }

  @Test
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
