/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.devlib;

import com.dytech.common.io.UnicodeReader;
import com.google.common.base.VerifyException;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PropBagEx implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(PropBagEx.class);

  private static final int MAX_BUILDERS = 16;

  private static final String WILD = "*";
  private static final String PATH_SEP = "/";
  private static final String BLANK = "";
  private static final String ATTR = "@";

  private static final long serialVersionUID = 55378008;

  private static final DocumentBuilderFactory factory;
  private static final ConcurrentLinkedQueue<DocumentBuilder> builders =
      new ConcurrentLinkedQueue<>();

  private Element m_elRoot;

  static {
    factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);

    try {
      factory.setFeature(
          "http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // $NON-NLS-1$
    } catch (ParserConfigurationException e) {
      // nothing
    } catch (NoSuchMethodError nup) {
      // java 1.4,doesn't like it
    }
    factory.setNamespaceAware(false);
  }

  private DocumentBuilder getBuilder() {
    DocumentBuilder builder = builders.poll();
    if (builder == null) {
      synchronized (factory) {
        try {
          return factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
          throw new VerifyException(pce);
        }
      }
    }
    return builder;
  }

  /** Creates a new PropBagEx. The root element with have the name "xml". */
  public PropBagEx() {
    super();
  }

  /**
   * Creates a new PropBagEx from the given stream.
   *
   * @param reader The stream to read from.
   */
  public PropBagEx(final Reader reader) {
    setXML(reader);
  }

  /**
   * Creates a new PropBagEx rooted at the given node. This is equivalent to <code>
   * new PropBagEx(n, false)</code>
   *
   * @param n The node to root the new PropBagEx at.
   */
  public PropBagEx(final Node n) {
    this(n, false);
  }

  /**
   * Creates a new PropBagEx rooted at the given node.
   *
   * @param n The node to root the new PropBagEx at.
   * @param sameDocument Use the nodes document for the new PropBagEx.
   */
  public PropBagEx(final Node n, final boolean sameDocument) {
    if (n == null) {
      throw new IllegalArgumentException("Node cannot be null");
    }

    if (sameDocument) {
      m_elRoot = (Element) n;
    } else {
      Document doc;
      DocumentBuilder builder = null;
      try {
        builder = getBuilder();
        doc = builder.getDOMImplementation().createDocument(null, null, null);

        final Node imp = importNode(doc, n, true);
        doc.appendChild(imp);

        m_elRoot = (Element) imp;
      } finally {
        releaseBuilder(builder);
      }
    }
  }

  /**
   * Creates a new PropBagEx from the given string.
   *
   * @param szXML The string to read.
   */
  public PropBagEx(final String szXML) {
    if (szXML == null) {
      throw new IllegalArgumentException("XML cannot be null");
    }
    setXML(szXML);
  }

  /**
   * Creates a new PropBagEx from the given file's content.
   *
   * @param file The file to read in.
   */
  public PropBagEx(final File file) {
    try {
      setXML(new FileInputStream(file));
    } catch (FileNotFoundException ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  /**
   * Creates a new PropBagEx from the given stream.
   *
   * @param is The stream to read from.
   */
  public PropBagEx(final InputStream is) {
    if (is == null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    setXML(is);
  }

  /**
   * A helper method that should be called by each method before using #getNodeHelper(..) or such.
   */
  private void ensureRoot() {
    if (m_elRoot == null) {
      clear();
    }
  }

  /** Returns the document for this PropBag. */
  public Element getRootElement() {
    ensureRoot();
    return m_elRoot;
  }

  /**
   * @return true if both PropBag objects point to the same root node
   */
  public boolean equalsDOM(final PropBagEx obj) {
    if (obj == null) {
      return false;
    }
    return obj.m_elRoot == m_elRoot;
  }

  @SuppressWarnings("null")
  private Node lookupNode(
      final Node parent, String nodeName, final int index, final boolean create) {
    Node foundNode = null;

    int nNumFound = 0;
    final Document doc = parent.getOwnerDocument();

    final boolean isAttribute = nodeName.startsWith(ATTR);
    nodeName = DOMHelper.stripAttribute(nodeName);
    if (isAttribute) {
      foundNode = ((Element) parent).getAttributeNode(nodeName);
    } else {
      nodeName = DOMHelper.stripNamespace(nodeName);
      final boolean matchAny = nodeName.equals(WILD);

      final NodeList children = parent.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        final Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          final String childName = DOMHelper.stripNamespace(child.getNodeName());
          if (matchAny || nodeName.equals(childName)) {
            if (nNumFound != index) {
              nNumFound++;
            } else {
              foundNode = child;
              break;
            }
          }
        }
      }
    }

    if (foundNode == null && create) {
      // If the Index is 0 and we didn't find a node or if the number
      // found (which is not zero based) equals the index (which is)
      // then this is the same as saying index is one more that the
      // number of nodes that exist then add a new child node.
      if (index == 0 || nNumFound == index) {
        if (isAttribute) {
          ((Element) parent).setAttribute(nodeName, BLANK);
          foundNode = ((Element) parent).getAttributeNode(nodeName);
        } else {
          foundNode = doc.createElement(nodeName);
          parent.appendChild(foundNode);
        }
      } else {
        // An illegal index has been used - throw an error
        String szError = "Error creating node ";
        szError += nodeName;
        szError += " with an index of ";
        szError += index;
        throw new RuntimeException(szError);
      }
    }
    return foundNode;
  }

  private int getIndexValue(final String[] aszNodeName) {
    aszNodeName[1] = aszNodeName[0];

    final int index = aszNodeName[0].indexOf('[');
    if (index >= 0) {
      aszNodeName[1] = aszNodeName[0].substring(0, index);

      // Sanity Check
      if (index + 1 < aszNodeName[0].length()) {
        final String szIndex = aszNodeName[0].substring(index + 1, aszNodeName[0].length() - 1);
        return Integer.parseInt(szIndex);
      }
    }

    // Index is the first instance.
    return 0;
  }

  /** Gets the node for the given path. */
  protected Node getNodeHelper(final String path, final boolean bCreate, final boolean bNew) {
    if (path == null) {
      throw new IllegalArgumentException("Path must not be null");
    }

    // Split on the path identifier
    final List<String> pathComponents = DOMHelper.splitPath(path);

    Node node = m_elRoot;
    final Document doc = node.getOwnerDocument();

    final Iterator<String> iter = pathComponents.iterator();
    while (iter.hasNext() && node != null) {
      final String[] aszNodeName = new String[2];
      aszNodeName[0] = iter.next();

      // Extract the index if that exists
      final int nIndex = getIndexValue(aszNodeName);
      if (bNew && !iter.hasNext()) {
        final Node child = doc.createElement(aszNodeName[1]);
        node.appendChild(child);
        return child;
      } else {
        node = lookupNode(node, aszNodeName[1], nIndex, bCreate);
      }
    }

    return node;
  }

  @SuppressWarnings("nls")
  private Writer genXML(final Writer sbuf, final Node subRoot) {
    try {
      boolean bEndElem = false;
      final int type = subRoot.getNodeType();
      switch (type) {
        case Node.DOCUMENT_TYPE_NODE:
          final DocumentType doctype = (DocumentType) subRoot;
          sbuf.write("<!DOCTYPE ");
          sbuf.write(doctype.getName());
          // see Jira Defect TLE-1295 :
          // http://apps.dytech.com.au/jira/browse/TLE-1295
          // Tidy DOMs don't correctly support this functionality
          if (doctype.getPublicId() != null) {
            sbuf.write(" PUBLIC \"");
            sbuf.write(doctype.getPublicId());
            sbuf.write("\" \"");
            sbuf.write(doctype.getSystemId());
            sbuf.write("\"");
          }
          sbuf.write(">\n");
          // doc.getDoctype();
          // System.out.println("<?xml version=\"1.0\" encoding=\""+
          // "UTF-8" + "\"?>");
          break;
        case Node.ELEMENT_NODE:
          sbuf.write('<');
          sbuf.write(subRoot.getNodeName());
          final NamedNodeMap nnm = subRoot.getAttributes();
          if (nnm != null) {
            final int len = nnm.getLength();
            Attr attr;
            for (int i = 0; i < len; i++) {
              attr = (Attr) nnm.item(i);
              sbuf.write(' ' + attr.getNodeName() + "=\"" + ent(attr.getNodeValue()) + '"');
            }
          }
          // Check for an empty parent element
          // no children, or a single TEXT_NODE with length() == 0
          final Node child = subRoot.getFirstChild();
          if (child == null
              || (child.getNodeType() == Node.TEXT_NODE
                  && (child.getNextSibling() == null && child.getNodeValue().length() == 0))) {
            sbuf.write(PATH_SEP);
          } else {
            bEndElem = true;
          }
          sbuf.write('>');
          break;
        case Node.ENTITY_REFERENCE_NODE:
          sbuf.write('&' + subRoot.getNodeName() + ';');
          break;
        case Node.CDATA_SECTION_NODE:
          sbuf.write("<![CDATA[" + subRoot.getNodeValue() + "]]>");
          break;
        case Node.TEXT_NODE:
          sbuf.write(ent(subRoot.getNodeValue()));
          break;
        case Node.PROCESSING_INSTRUCTION_NODE:
          sbuf.write("<?" + subRoot.getNodeName());
          final String data = subRoot.getNodeValue();
          if (data != null && data.length() > 0) {
            sbuf.write(' ');
            sbuf.write(data);
          }
          sbuf.write("?>");
          break;
        case Node.COMMENT_NODE:
          sbuf.write("<!--" + subRoot.getNodeValue() + "-->");
          break;
        default:
          log.debug("Unsupported node type: " + type);
      }

      for (Node child = subRoot.getFirstChild(); child != null; child = child.getNextSibling()) {
        genXML(sbuf, child);
      }

      if (bEndElem) {
        sbuf.write("</" + subRoot.getNodeName() + ">");
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }

    return sbuf;
  }

  @SuppressWarnings("nls")
  protected String ent(final String text) {
    final StringBuilder szOut = new StringBuilder();
    final char[] chars = text.toCharArray();
    for (final char ch : chars) {
      switch (ch) {
        case '<':
          szOut.append("&lt;");
          break;

        case '>':
          szOut.append("&gt;");
          break;

        case '&':
          szOut.append("&amp;");
          break;

        case '"':
          szOut.append("&quot;");
          break;

        default:
          // http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
          // regular displayable ASCII:
          if (ch == 0xA || ch == 0xD || ch == 0x9 || (ch >= 0x20 && ch <= 0x007F)) {
            szOut.append(ch);
          } else if ((ch > 0x007F && ch <= 0xD7FF)
              || (ch >= 0xE000 && ch <= 0xFFFD)
              || (ch >= 0x10000 && ch <= 0x10FFFF)) {
            szOut.append("&#x");
            szOut.append(hex(ch));
            szOut.append(';');
          }
          // else we discard the character entirely.
          // It CANNOT be placed in XML
          break;
      }
    }
    return szOut.toString();
  }

  protected String hex(final char c) {
    final String hexed = Integer.toHexString(c);
    final int deficit = 4 - hexed.length();
    // wooo, unrolled loops
    switch (deficit) {
      case 1:
        return "0" + hexed;
      case 2:
        return "00" + hexed;
      case 3:
        return "000" + hexed;
      default:
        return hexed;
    }
  }

  /**
   * Move a node from one parent to another
   *
   * @param from The path to a node to move
   * @param to The new parent node you want to move to
   */
  public void moveNode(final String from, final String to) {
    checkNotAttribute(from);
    checkNotAttribute(to);

    final Node src = getNodeHelper(from, true, false);
    final Node dst = getNodeHelper(to, true, false);
    src.getParentNode().removeChild(src);
    dst.appendChild(src);
  }

  private Node getRootDoc() {
    if (m_elRoot.getOwnerDocument().getDocumentElement() == m_elRoot) {
      return m_elRoot.getOwnerDocument();
    } else {
      return m_elRoot;
    }
  }

  @Override
  public String toString() {
    ensureRoot();
    // genXML returns the same StringBuffer that we pass in.
    final StringWriter stringWriter = new StringWriter();
    genXML(new BadCharacterFilterWriter(stringWriter), getRootDoc());
    return stringWriter.toString();
  }

  /**
   * Now has dubious value, use toString instead
   *
   * @param buffer
   * @return
   */
  @Deprecated
  public StringBuffer toStringBuffer(final StringBuffer buffer) {
    return buffer.append(toString());
  }

  /** Iterates over all nodes matching the given parent and name. */
  private abstract static class InternalIterator<T> implements Iterator<T>, Iterable<T> {
    protected Node parent;
    protected String name;
    protected Node upto;
    protected Node last;
    protected Node root;

    public InternalIterator(
        final Node parent, final Node first, final String name, final Node root) {
      this.parent = parent;
      this.name = name;
      this.root = root;
      upto = first;
      if (parent == null) {
        upto = null;
      } else if (upto == null) {
        upto = DOMHelper.findNext(parent.getFirstChild(), name);
      }
    }

    protected void moveOn() {
      last = upto;
      if (upto == root) {
        upto = null;
      } else {
        upto = DOMHelper.findNext(last.getNextSibling(), name);
      }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
      return this;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      if (last == null) {
        throw new IllegalStateException();
      }
      parent.removeChild(last);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return upto != null;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public T next() {
      moveOn();
      return getNextValue();
    }

    protected abstract T getNextValue();
  }

  /**
   * Iterates over all nodes matching the given parent and name. The iteration will return
   * PropBagEx's rooted at each node.
   */
  public static class PropBagIterator extends InternalIterator<PropBagEx> {
    public PropBagIterator(
        final Node parent, final Node first, final String name, final Node root) {
      super(parent, first, name, root);
    }

    @Override
    protected PropBagEx getNextValue() {
      return new PropBagEx(last, true);
    }
  }

  /**
   * Iterates over all nodes matching the given parent and name. The iteration will return the
   * String values of each node.
   */
  public static class ValueIterator extends InternalIterator<String> {
    public ValueIterator(final Node parent, final Node first, final String name, final Node root) {
      super(parent, first, name, root);
    }

    @Override
    protected String getNextValue() {
      return DOMHelper.getValueForNode(last, BLANK);
    }
  }

  /**
   * Iterates over all nodes from the parent, that match the given path at any of it's components.
   */
  private abstract static class ListOfNodesIterator<T> implements Iterator<T>, Iterable<T> {
    protected final LinkedList<Node> nodes = new LinkedList<Node>();

    protected Node last;

    protected void moveOn() {
      last = nodes.removeFirst();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
      return this;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      if (last == null) {
        throw new IllegalStateException();
      }
      last.getParentNode().removeChild(last);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return !nodes.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public T next() {
      moveOn();
      return getNextValue();
    }

    protected abstract T getNextValue();
  }

  /**
   * Iterates over all nodes from the parent, that match the given path at any of it's components.
   */
  private abstract static class InternalThoroughIterator<T> extends ListOfNodesIterator<T> {
    public InternalThoroughIterator(final Node parent, final String path) {
      final List<String> names = Lists.newArrayList(path.split(PATH_SEP));
      names.removeIf(p -> p.trim().isEmpty());

      if (!names.isEmpty()) {
        findAllNodes(parent, names, 0);
      }
    }

    protected void findAllNodes(final Node parent, final List<String> names, final int index) {
      final String path = names.get(index);
      if (!path.startsWith(ATTR)) {
        Node find = DOMHelper.findNext(parent.getFirstChild(), path);
        while (find != null) {
          if (index == names.size() - 1) {
            nodes.add(find);
          } else {
            findAllNodes(find, names, index + 1);
          }
          find = DOMHelper.findNext(find.getNextSibling(), path);
        }
      } else {
        if (index == names.size() - 1) {
          final Node attr = parent.getAttributes().getNamedItem(path.substring(1));
          nodes.add(attr);
        } else {
          throw new RuntimeException("Xpath has an attribute component before the last component");
        }
      }
    }
  }

  /**
   * Iterates over all nodes from the parent, that match the given path at any of it's components.
   * The iteration will return PropBag's rooted at each of each nodes.
   */
  public static class PropBagThoroughIterator extends InternalThoroughIterator<PropBagEx> {
    public PropBagThoroughIterator(final Node parent, final String name) {
      super(parent, name);
    }

    @Override
    protected PropBagEx getNextValue() {
      return new PropBagEx(last, true);
    }
  }

  /**
   * Iterates over all nodes from the parent, that match the given path at any of it's components.
   * The iteration will return the String value of each node.
   */
  public static class ValueThoroughIterator extends InternalThoroughIterator<String> {
    public ValueThoroughIterator(final Node parent, final String name) {
      super(parent, name);
    }

    @Override
    protected String getNextValue() {
      return DOMHelper.getValueForNode(last, BLANK);
    }
  }

  /**
   * Iterates over all nodes from the parent, that match the given path at any of it's components.
   * The iteration will return each Node.
   */
  public static class NodeThoroughIterator extends InternalThoroughIterator<Node> {
    public NodeThoroughIterator(final Node parent, final String name) {
      super(parent, name);
    }

    @Override
    protected Node getNextValue() {
      return last;
    }
  }

  /** Creates an iterator which return's PropBagEx's for all children of the root element */
  public PropBagIterator iterator() {
    ensureRoot();
    return new PropBagIterator(m_elRoot, null, null, m_elRoot);
  }

  /**
   * Creates an iterator which return's PropBagEx's for each child for the path given<br>
   * E.g.<br>
   * /path/node/item<br>
   * Will iterate over all the "item" nodes.
   *
   * @param path The path to iterate over
   * @return An iterator which return's PropBagEx's for each child for the path given
   */
  public PropBagIterator iterator(final String path) {
    checkNotAttribute(path);
    ensureRoot();

    String name = null;
    Node parent = null;

    final Node node = getNodeHelper(path, false, false);
    if (node != null) {
      parent = node.getParentNode();

      // see Jira Defect TLE-1293 :
      // http://apps.dytech.com.au/jira/browse/TLE-1293
      name = DOMHelper.stripNamespace(node.getNodeName());
      if (path.endsWith(WILD)) {
        name = WILD;
      }
    }
    return new PropBagIterator(parent, node, name, m_elRoot);
  }

  /** Creates an iterator which return's the values for each path. */
  public ValueIterator iterateValues(final String path) {
    checkNotAttribute(path);
    ensureRoot();

    String name = null;
    final Node oNode = getNodeHelper(path, false, false);
    Node parent = null;
    if (oNode != null) {
      if (path.endsWith(WILD)) {
        name = WILD;
      } else {
        name = DOMHelper.stripNamespace(oNode.getNodeName());
      }
      parent = oNode.getParentNode();
    }
    return new ValueIterator(parent, oNode, name, m_elRoot);
  }

  /** Iterates over all nodes that match the given path, like an XSLT does. */
  public PropBagThoroughIterator iterateAll(final String path) {
    checkNotAttribute(path);
    ensureRoot();

    return new PropBagThoroughIterator(m_elRoot, path);
  }

  /** Iterates over all node values that match the given path, like an XSLT does. */
  public ValueThoroughIterator iterateAllValues(final String path) {
    ensureRoot();

    return new ValueThoroughIterator(m_elRoot, path);
  }

  /** Iterates over all nodes that match the given path, like an XSLT does. */
  public NodeThoroughIterator iterateAllNodes(final String path) {
    ensureRoot();

    return new NodeThoroughIterator(m_elRoot, path);
  }

  private static class AllNodesWithNameIterator extends ListOfNodesIterator<PropBagEx> {
    public AllNodesWithNameIterator(final Node root, final String name) {
      process(root, name);
    }

    private void process(final Node node, final String lookingForName) {
      String nodeName = DOMHelper.stripNamespace(node.getNodeName());
      if (lookingForName.equals(nodeName)) {
        nodes.add(node);
      }

      Node childNode = DOMHelper.findNext(node.getFirstChild(), null);
      while (childNode != null) {
        process(childNode, lookingForName);
        childNode = DOMHelper.findNext(childNode.getNextSibling(), null);
      }
    }

    @Override
    protected PropBagEx getNextValue() {
      return new PropBagEx(last, true);
    }
  }

  public AllNodesWithNameIterator iterateAllNodesWithName(String name) {
    ensureRoot();
    return new AllNodesWithNameIterator(m_elRoot, name);
  }

  /**
   * Creates a new PropBag rooted at the parent of this PropBag, sharing the same DOM nodes as the
   * creator.
   *
   * @return PropBagEx PropBag rooted at the parent, or null if the parent does not exist.
   */
  public PropBagEx getParent() {
    ensureRoot();
    final Node rootNode = m_elRoot.getParentNode();
    if (rootNode instanceof Element) {
      return new PropBagEx(rootNode, true);
    } else {
      return null;
    }
  }

  /**
   * Creates a List of new PropBags rooted at each of the children of this PropBag, sharing the same
   * DOM nodes as the creator.
   *
   * @return List<PropBagEx> A list of children or null if the node does not exist.
   */
  public List<PropBagEx> getChildren() {
    ensureRoot();
    final Element root = m_elRoot;
    if (root != null) {
      List<PropBagEx> childrenPropBagEx = new ArrayList<>();
      NodeList childrenNodes = root.getChildNodes();
      for (int i = 0; i < childrenNodes.getLength(); i++) {
        Node n = childrenNodes.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          childrenPropBagEx.add(new PropBagEx(n, true));
        }
      }
      return childrenPropBagEx;
    } else {
      return null;
    }
  }

  @Override
  public Object clone() {
    ensureRoot();
    return new PropBagEx(m_elRoot, false);
  }

  public void setXML(final Reader reader) {
    final BufferedReader filterer = new BufferedReader(new BadCharacterFilterReader(reader));
    DocumentBuilder builder = null;
    try {
      builder = getBuilder();
      Document doc = builder.parse(new InputSource(filterer));

      // Get the root element
      m_elRoot = doc.getDocumentElement();
    } catch (Exception ex) {
      throw new RuntimeException("Error parsing XML", ex);
    } finally {
      releaseBuilder(builder);
    }
  }

  public void setXML(final InputStream inp) {
    setXML(new UnicodeReader(inp, "UTF-8")); // $NON-NLS-1$
  }

  public void setXML(final String szXML) {
    setXML(new StringReader(szXML));
  }

  /**
   * Retrieves the text at the root node. This is the same as getNode("/").
   *
   * @return String content of element
   */
  public String getNode() {
    return getNode(PATH_SEP);
  }

  /**
   * retrieves the text delimited by the given node's tags
   *
   * @param path full name of the node, parents qualified by '/'
   * @return String content of element
   */
  public String getNode(final String path) {
    return getNode(path, BLANK);
  }

  /**
   * retrieves the text delimited by the given node's tags
   *
   * @param path full name of the node, parents qualified by '/'
   * @return String content of element
   */
  public String getNode(final String path, final String defaultValue) {
    ensureRoot();
    final Node oNode = getNodeHelper(path, false, false);
    return DOMHelper.getValueForNode(oNode, defaultValue);
  }

  public String getNodeEncoded(final String szFullNodeName) {
    final Node oNode = getNodeHelper(szFullNodeName, false, false);

    final StringWriter sbuf = new StringWriter();
    if (oNode != null) {
      for (Node child = oNode.getFirstChild(); child != null; child = child.getNextSibling()) {
        genXML(new BadCharacterFilterWriter(sbuf), child);
      }
      return sbuf.toString();
    }
    return BLANK;
  }

  /**
   * Retrieves a node as an int value given it's name.
   *
   * @param path full name of the node, parents qualified by '/'
   * @return int value of the node
   */
  public int getIntNode(final String path) {
    ensureRoot();
    return Integer.parseInt(getNode(path));
  }

  /**
   * Retrieves a node as an int value given it's name. If the node does not exist or its value is an
   * invalid integer, then the default value is returned.
   *
   * @param path full name of the node, parents qualified by '/'
   * @param defaultValue A default value to return if the node does not exist.
   * @return value of the node.
   */
  public int getIntNode(final String path, final int defaultValue) {
    ensureRoot();
    try {
      return getIntNode(path);
    } catch (final NumberFormatException ex) {
      return defaultValue;
    }
  }

  public boolean isNodeTrue(final String path) {
    return getNode(path).equals("true"); // $NON-NLS-1$
  }

  public boolean isNodeFalse(final String path) {
    return getNode(path).equals("false"); // $NON-NLS-1$
  }

  public void setNode(final String path, final boolean value) {
    setNode(path, Boolean.toString(value));
  }

  public void setNode(final String path, final int value) {
    setNode(path, Integer.toString(value));
  }

  public void setNode(final String path, final long value) {
    setNode(path, Long.toString(value));
  }

  public void setNode(final String path, final float value) {
    setNode(path, Float.toString(value));
  }

  public void setNode(final String path, final double value) {
    setNode(path, Double.toString(value));
  }

  public void setNode(final String path, final String value) {
    if (value == null) {
      throw new IllegalArgumentException("Element value must not be null");
    }

    setNodeImpl(path, value);
  }

  public void setIfNotEmpty(final String path, final String value) {
    if (!Check.isEmpty(value)) {
      setNodeImpl(path, value);
    }
  }

  public void setIfNotNull(final String path, final String value) {
    if (value != null) {
      setNodeImpl(path, value);
    }
  }

  private void setNodeImpl(final String path, final String value) {
    ensureRoot();
    final Node node = getNodeHelper(path, true, false);
    DOMHelper.setValueForNode(node, value);
  }

  /** Counts the number of matching nodes. */
  public int nodeCount(final String path) {
    ensureRoot();

    int count = 0;

    final Node node = getNodeHelper(path, false, false);
    if (node != null) {
      if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
        count = 1;
      } else {
        for (final Iterator<String> iter = iterateValues(path); iter.hasNext(); count++) {
          iter.next();
        }
      }
    }
    return count;
  }

  /**
   * Checks to see if a node exists
   *
   * @param path The path to the node.
   * @return true if one or more nodes for this path exist
   */
  public boolean nodeExists(final String path) {
    ensureRoot();
    return findNode(path) != null;
  }

  /**
   * Removes a subtree from this tree.
   *
   * @param subtree The tree to remove
   */
  public void deleteSubtree(PropBagEx subtree) {
    ensureRoot();
    Element otherRoot = subtree.getRootElement();
    otherRoot.getParentNode().removeChild(otherRoot);
  }

  /**
   * Creates a new PropBag rooted at the given path, yet sharing the same DOM nodes as the creator.
   *
   * @param path Xpath to the root of the new tree.
   * @return PropBagEx PropBag rooted at the subtree, or null if the path does not exist.
   */
  public PropBagEx getSubtree(final String path) {
    checkNotAttribute(path);

    ensureRoot();
    final Element root = (Element) getNodeHelper(path, false, false);

    if (root == null) {
      return null;
    } else {
      return new PropBagEx(root, true);
    }
  }

  /**
   * Creates a new subtree with the path given
   *
   * @param szNodeName The path for the new Subtree being created
   * @return PropBagEx the new subtree (or null if the path was invalid)
   */
  public PropBagEx newSubtree(final String szNodeName) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    final Node oNode = getNodeHelper(szNodeName, true, true);
    if (oNode != null) {
      oNode.appendChild(oNode.getOwnerDocument().createTextNode(BLANK));
      return new PropBagEx(oNode, true);
    }
    return null;
  }

  /**
   * Get the existing subtree, or creates a new subtree if it does not exist.
   *
   * @param path The path for the subtree being retrieved/created.
   * @return PropBagEx the subtree (or null if the path was invalid).
   */
  public PropBagEx aquireSubtree(final String path) {
    checkNotAttribute(path);

    PropBagEx subtree = getSubtree(path);
    if (subtree == null) {
      subtree = newSubtree(path);
    }
    return subtree;
  }

  /**
   * Sets the name of the root element. Warning this can be expensive operation until we use DOM
   * Level 3
   */
  public void setNodeName(final String newname) {
    ensureRoot();
    checkNotAttribute(newname);

    final NamedNodeMap attributes = m_elRoot.getAttributes();
    final Document doc = m_elRoot.getOwnerDocument();
    final Element newelem = doc.createElement(newname);

    Node child;
    while ((child = m_elRoot.getFirstChild()) != null) {
      m_elRoot.removeChild(child);
      newelem.appendChild(child);
    }

    for (int i = 0; i < attributes.getLength(); i++) {
      final Attr attr = (Attr) attributes.item(i);
      newelem.setAttribute(attr.getName(), attr.getValue());
    }

    m_elRoot.getParentNode().replaceChild(newelem, m_elRoot);
    m_elRoot = newelem;
  }

  /**
   * Returns the name of the root element.
   *
   * @return the name of the node.
   */
  public String getNodeName() {
    ensureRoot();
    return getNodeHelper(BLANK, false, false).getNodeName();
  }

  private Node importNode(final Document doc, final Node n, final boolean bDeep) {
    Node dest;
    final int type = n.getNodeType();
    switch (type) {
      case Node.ELEMENT_NODE:
        dest = doc.createElement(n.getNodeName());
        final NamedNodeMap nnm = n.getAttributes();
        final int nnmCount = nnm.getLength();
        for (int i = 0; i < nnmCount; i++) {
          final Attr attr = (Attr) nnm.item(i);
          ((Element) dest).setAttribute(attr.getName(), attr.getValue());
        }
        break;

      case Node.TEXT_NODE:
        dest = doc.createTextNode(n.getNodeValue());
        break;

      case Node.CDATA_SECTION_NODE:
        dest = doc.createCDATASection(n.getNodeValue());
        break;

      case Node.ENTITY_REFERENCE_NODE:
        dest = doc.createEntityReference(n.getNodeValue());
        break;

      // see Jira Defect TLE-1832 :
      // http://apps.dytech.com.au/jira/browse/TLE-1832
      case Node.COMMENT_NODE:
        dest = doc.createComment(n.getNodeValue());
        break;

      default:
        throw new RuntimeException("Unsupported DOM Node: " + type);
    }
    if (bDeep) {
      for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
        dest.appendChild(importNode(doc, child, true));
      }
    }
    return dest;
  }

  /**
   * Appends nodes from another propbag into the node located by szNodeName
   *
   * @param szNodeName The path to insert into
   * @param xml The propbag to insert
   */
  public void append(final String szNodeName, final PropBagEx xml) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    xml.ensureRoot();
    Node oNode = getNodeHelper(szNodeName, false, false);
    if (oNode == null) {
      createNode(szNodeName, BLANK);
      oNode = getNodeHelper(szNodeName, false, false);
    }
    oNode.appendChild(importNode(oNode.getOwnerDocument(), xml.m_elRoot, true));
  }

  /**
   * Appends the child nodes of another propbag into the node located by szNodeName
   *
   * @param szNodeName The path to insert into
   * @param xml The propbag that we want the children of
   */
  public void appendChildren(final String szNodeName, final PropBagEx xml) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    xml.ensureRoot();
    Node oNode = getNodeHelper(szNodeName, false, false);
    if (oNode == null) {
      createNode(szNodeName, BLANK);
      oNode = getNodeHelper(szNodeName, false, false);
    }

    final Document doc = oNode.getOwnerDocument();
    Node child = xml.m_elRoot.getFirstChild();
    while (child != null) {
      oNode.appendChild(importNode(doc, child, true));
      child = child.getNextSibling();
    }

    // we might have pushed two text nodes together, normalise it
    oNode.normalize();
  }

  /**
   * Inserts nodes from another PropBag into the position located by szNodeName.
   *
   * @param szNodeName The path to insert into
   * @param xml The PropBag that we want to insert
   */
  public void insertAt(final String szNodeName, final PropBagEx xml) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    xml.ensureRoot();
    final Node oNode = getNodeHelper(szNodeName, false, false);
    if (oNode == null) {
      final int index = szNodeName.lastIndexOf(PATH_SEP);
      final String parent = index < 0 ? PATH_SEP : szNodeName.substring(0, index);

      append(parent, xml);
    } else {
      oNode
          .getParentNode()
          .insertBefore(importNode(oNode.getOwnerDocument(), xml.m_elRoot, true), oNode);
    }
  }

  /**
   * Inserts the child nodes of another PropBag into the position located by szNodeName.
   *
   * @param szNodeName The path to insert into
   * @param xml The PropBag that we want the children of
   */
  public void insertChildrenAt(final String szNodeName, final PropBagEx xml) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    xml.ensureRoot();
    final Node oNode = getNodeHelper(szNodeName, false, false);
    if (oNode == null) {
      final int index = szNodeName.lastIndexOf(PATH_SEP);
      final String parent = index < 0 ? PATH_SEP : szNodeName.substring(0, index);
      appendChildren(parent, xml);
    } else {
      final Document doc = oNode.getOwnerDocument();
      Node child = xml.m_elRoot.getFirstChild();
      while (child != null) {
        oNode.getParentNode().insertBefore(importNode(doc, child, true), oNode);
        child = child.getNextSibling();
      }
    }
  }

  public void createNode(final String szNodeName, final String szValue) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    final Node oNode = getNodeHelper(szNodeName, true, true);
    if (oNode != null) {
      oNode.appendChild(oNode.getOwnerDocument().createTextNode(szValue));
    }
  }

  public void createCDATANode(final String szNodeName, final String szValue) {
    checkNotAttribute(szNodeName);

    ensureRoot();
    final Node oNode = getNodeHelper(szNodeName, true, true);
    if (oNode != null) {
      oNode.appendChild(oNode.getOwnerDocument().createCDATASection(szValue));
    }
  }

  /**
   * Deletes a node at the given path.
   *
   * @return true if the node was deleted.
   */
  public boolean deleteNode(final String path) {
    ensureRoot();
    final Node node = getNodeHelper(path, false, false);
    return DOMHelper.removeNode(node);
  }

  /**
   * Deletes all nodes matching the last component of the given path.
   *
   * @return true if one or more nodes were deleted.
   */
  public boolean deleteAll(final String path) {
    boolean removed = false;
    while (deleteNode(path)) {
      removed = true;
    }
    return removed;
  }

  /**
   * Deletes all nodes matching the last component of the given path with the given value.
   *
   * @return true if one or more nodes were deleted.
   */
  public boolean deleteAllWithValue(final String path, final String value) {
    ensureRoot();
    boolean removed = false;

    final Iterator<PropBagEx> i = iterator(path);
    while (i.hasNext()) {
      final PropBagEx xml = i.next();
      if (xml.getNode().equals(value)) {
        i.remove();
        removed = true;
      }
    }

    return removed;
  }

  public Map<String, String> getAttributesForNode(final String path) {
    ensureRoot();
    checkNotAttribute(path);

    final Node node = getNodeHelper(path, false, false);
    if (node != null) {
      final NamedNodeMap map = node.getAttributes();

      final Map<String, String> attributes = new HashMap<String, String>();
      if (map != null) {
        final int count = map.getLength();
        for (int i = 0; i < count; i++) {
          final Attr attribute = (Attr) map.item(i);
          attributes.put(attribute.getName(), attribute.getValue());
        }
      }
      return attributes;
    } else {
      return Collections.emptyMap();
    }
  }

  /**
   * Sets an attribute on the XML node.
   *
   * <p>For example, if the XML is: {@code <xml> <item/> </xml> } And with `setAttribute("/item",
   * "name", "value")`, the result is: {@code <xml> <item name="value"/> </xml> }
   *
   * @param path The path to the node.
   * @param attribute The name of the attribute.
   * @param value The value of the attribute.
   */
  public void setAttribute(String path, String attribute, String value) {
    ensureRoot();
    checkNotAttribute(path);

    final Node node = getNodeHelper(path, false, false);
    Element element = (Element) node;
    element.setAttribute(attribute, value);
  }

  private void mergeHelper(final Element dst, final Element src) {
    final NodeList oChildNodes = src.getChildNodes();
    boolean bHasElem = false;
    String value = BLANK;

    // Go through all the source nodes
    final int length = oChildNodes.getLength();
    for (int i = 0; i < length; i++) {
      final Node oItem = oChildNodes.item(i);
      final short nType = oItem.getNodeType();

      if (nType == Node.ELEMENT_NODE) {
        // look for a node of the same name in the
        // destination
        final String name = oItem.getNodeName();
        Node oOldNode = lookupNode(dst, name, 0, false);

        // If none found, or the source node has child elements
        // create a new node in the destination
        if (oOldNode == null || !DOMHelper.hasChildElement(oItem)) {
          oOldNode = dst.getOwnerDocument().createElement(name);
          dst.appendChild(oOldNode);
        }

        // recurse into the newly matched up children
        mergeHelper((Element) oOldNode, (Element) oItem);
        bHasElem = true;
      }

      if (nType == Node.CDATA_SECTION_NODE || nType == Node.TEXT_NODE) {
        value = oItem.getNodeValue();
      }
    }
    final NamedNodeMap nnm = src.getAttributes();
    final int nnmCount = nnm.getLength();
    for (int i = 0; i < nnmCount; i++) {
      final Attr attr = (Attr) nnm.item(i);
      dst.setAttribute(attr.getName(), attr.getValue());
    }

    if (!bHasElem) {
      final Node oItem = dst.getFirstChild();
      if (oItem == null) {
        dst.appendChild(dst.getOwnerDocument().createTextNode(value));
        return;
      }
      if (oItem.getNodeType() == Node.ELEMENT_NODE) {
        return;
      } else {
        oItem.setNodeValue(value);
      }
    }
  }

  public void mergeTree(final PropBagEx mergeBag) {
    ensureRoot();
    mergeBag.ensureRoot();
    try {
      mergeHelper(m_elRoot, mergeBag.m_elRoot);
    } catch (final DOMException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  public void clear() {
    DocumentBuilder builder = null;
    try {

      builder = getBuilder();
      Document doc = builder.newDocument();

      // Create the empty Propbag
      final Element root = doc.createElement("xml"); // $NON-NLS-1$
      doc.appendChild(root);

      // Get the root element
      m_elRoot = doc.getDocumentElement();
    } finally {
      releaseBuilder(builder);
    }
  }

  /** You should really use getSubtree() and getRootElement() instead. */
  @Deprecated
  public Node findNode(final String szFullNodeName) {
    ensureRoot();
    return getNodeHelper(szFullNodeName, false, false);
  }

  /** Returns all the values for a given path. */
  public List<String> getNodeList(final String path) {
    ensureRoot();

    final List<String> results = new ArrayList<>();
    if (!path.contains(ATTR)) {
      final Iterator<String> iter = iterateValues(path);
      while (iter.hasNext()) {
        results.add(iter.next());
      }
    } else if (nodeExists(path)) {
      results.add(getNode(path));
    }
    return results;
  }

  public String[] getAttributes(final String szPath, final String szAtt) {
    ensureRoot();
    Node oNode = getNodeHelper(szPath, false, false);
    if (oNode != null) {
      final Collection<String> vals = new ArrayList<>();
      final String szNodeName = oNode.getNodeName();

      for (; oNode != null; oNode = oNode.getNextSibling()) {
        if (oNode.getNodeType() == Node.ELEMENT_NODE && szNodeName.equals(oNode.getNodeName())) {
          String val = ((Element) oNode).getAttribute(szAtt);
          vals.add(val);
        }
      }
      return vals.toArray(new String[vals.size()]);
    } else {
      return new String[0];
    }
  }

  public boolean hasAttributes(final String szPath) {
    ensureRoot();
    Node oNode = getNodeHelper(szPath, false, false);
    if (oNode == null) {
      return false;
    }
    return oNode.hasAttributes();
  }

  /**
   * Helper method to indicates whether the path is pointing at an attribute.
   *
   * @param path the path to check.
   * @throws IllegalArgumentException if the path is to an attribute.
   */
  private void checkNotAttribute(final String path) throws IllegalArgumentException {
    if (path.contains(ATTR)) {
      throw new IllegalArgumentException("Path must not point to an attribute");
    }
  }

  public List<Element> getNodesByAttribute(
      final String attributeName, final String attributeValue) {
    final Element element = getRootElement();

    final List<Element> list = new ArrayList<>();
    if (element.hasAttribute(attributeName)
        && element.getAttribute(attributeName).equals(attributeValue)) {
      list.add(element);
    }

    getNodesByAttribute(element.getChildNodes(), list, attributeName, attributeValue);

    return list;
  }

  private void getNodesByAttribute(
      final NodeList nodeList,
      final List<Element> resultList,
      final String attributeName,
      final String attributeValue) {
    if (nodeList != null && resultList != null) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
          final Element childElement = (Element) nodeList.item(i);
          if (childElement.hasAttribute(attributeName)
              && childElement.getAttribute(attributeName).equals(attributeValue)) {
            resultList.add(childElement);
          }

          if (childElement.hasChildNodes()) {
            getNodesByAttribute(
                childElement.getChildNodes(), resultList, attributeName, attributeValue);
          }
        }
      }
    }
  }

  // ------------------------------------------------------------------------------
  // the following methods needed to support serializable interface
  // ------------------------------------------------------------------------------

  private void writeObject(final ObjectOutputStream out) throws IOException {
    final String xmlString = this.toString();
    out.writeObject(xmlString);
  }

  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    final String xmlString = (String) in.readObject();
    try {
      this.setXML(xmlString);
    } catch (final Exception e) {
      throw new IOException("Error parsing xml");
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof PropBagEx)) {
      return false;
    }

    try {
      final PropBagEx xml2 = (PropBagEx) obj;

      // Check node name:
      if (!m_elRoot.getNodeName().equals(xml2.m_elRoot.getNodeName())) {
        return false;
      }

      // Check value:
      final Node nChild1 = m_elRoot.getFirstChild();
      final Node nChild2 = xml2.m_elRoot.getFirstChild();
      String val1 = BLANK;
      String val2 = BLANK;
      if (nChild1 != null && nChild1.getNodeType() == Node.TEXT_NODE) {
        val1 = nChild1.getNodeValue();
      }

      if (nChild2 != null && nChild2.getNodeType() == Node.TEXT_NODE) {
        val2 = nChild2.getNodeValue();
      }

      if (DOMHelper.hasChildElement(m_elRoot)) {
        if (!val1.trim().equals(val2.trim())) {
          return false;
        }
      } else if (!val1.equals(val2)) {
        return false;
      }

      // Compare attributes:
      final NamedNodeMap attribs1 = m_elRoot.getAttributes();
      final NamedNodeMap attribs2 = xml2.m_elRoot.getAttributes();

      final int nAttribs = attribs1.getLength();
      if (attribs2.getLength() != nAttribs) {
        return false;
      }

      for (int i = 0; i < nAttribs; ++i) {
        final String name1 = attribs1.item(i).getNodeName();
        val1 = attribs1.item(i).getNodeValue();
        final String name2 = attribs2.item(i).getNodeName();
        val2 = attribs2.item(i).getNodeValue();

        if (!val1.equals(val2) || !name1.equals(name2)) {
          return false;
        }
      }

      // Compare all nodes at this level:
      final Iterator<PropBagEx> iter1 = iterator();
      final Iterator<PropBagEx> iter2 = xml2.iterator();
      while (iter1.hasNext()) {
        if (!iter2.hasNext()) {
          return false;
        }

        final PropBagEx child1 = iter1.next();
        final PropBagEx child2 = iter2.next();
        if (!child1.equals(child2)) {
          return false;
        }
      }
    } catch (final Exception ex) {
      log.error("Error comparing " + m_elRoot.getNodeName(), ex);
      return false;
    }
    return true;
  }

  private static void releaseBuilder(DocumentBuilder builder) {
    if (builder != null && builders.size() < MAX_BUILDERS) {
      builder.reset();
      builders.offer(builder);
    }
  }
}
