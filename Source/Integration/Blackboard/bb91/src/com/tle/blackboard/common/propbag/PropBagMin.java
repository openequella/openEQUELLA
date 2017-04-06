package com.tle.blackboard.common.propbag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * A subset of the actual PropBagEx class. Blackboard makes me cry, they still
 * haven't fixed the upload webservice problem.
 */
@SuppressWarnings("nls")
public class PropBagMin
{
	private static final String WILD = "*";
	private static final String PATH_SEP = "/";
	private static final String BLANK = "";
	private static final String ATTR = "@";

	private static DocumentBuilderFactory factory;
	private Element m_elRoot;

	static
	{
		if( factory == null )
		{
			factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try
			{
				factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			}
			catch( ParserConfigurationException e )
			{
				// nothing
			}
			catch( NoSuchMethodError nup )
			{
				// java 1.4,doesn't like it
			}
			factory.setNamespaceAware(false);
		}
	}

	/**
	 * Creates a new PropBagEx rooted at the given node.
	 * 
	 * @param node The node to root the new PropBagEx at.
	 * @param sameDocument Use the nodes document for the new PropBagEx.
	 */
	private PropBagMin(final Node n, final boolean sameDocument)
	{
		if( sameDocument )
		{
			m_elRoot = (Element) n;
		}
		else
		{
			Document doc;
			try
			{
				DocumentBuilder builder;
				synchronized( factory )
				{
					builder = factory.newDocumentBuilder();
				}
				doc = builder.getDOMImplementation().createDocument(null, null, null);

				final Node imp = importNode(doc, n, true);
				doc.appendChild(imp);

				m_elRoot = (Element) imp;
			}
			catch( final ParserConfigurationException pce )
			{
				throw propagate(pce);
			}
		}
	}

	/**
	 * Creates a new PropBagEx from the given string.
	 * 
	 * @param szXML The string to read.
	 * @throws PropBagExException if the XML being read is invalid.
	 */
	public PropBagMin(final String szXML)
	{
		setXML(new StringReader(szXML));
	}

	/**
	 * Creates a new PropBagEx from the given stream.
	 * 
	 * @param is The stream to read from.
	 * @throws PropBagExException if the XML being read is invalid.
	 */
	public PropBagMin(final InputStream is)
	{
		setXML(new UnicodeReader(is, "UTF-8"));
	}

	/**
	 * A helper method that should be called by each method before using
	 * #getNodeHelper(..) or such.
	 */
	private void ensureRoot()
	{
		if( m_elRoot == null )
		{
			clear();
		}
	}

	@SuppressWarnings("null")
	private Node lookupNode(final Node parent, String nodeName, final int index, final boolean create)
	{
		Node foundNode = null;

		int nNumFound = 0;
		final Document doc = parent.getOwnerDocument();

		final boolean isAttribute = nodeName.startsWith(ATTR);
		nodeName = DOMHelper.stripAttribute(nodeName);
		if( isAttribute )
		{
			foundNode = ((Element) parent).getAttributeNode(nodeName);
		}
		else
		{
			nodeName = DOMHelper.stripNamespace(nodeName);
			final boolean matchAny = nodeName.equals(WILD);

			final NodeList children = parent.getChildNodes();
			for( int i = 0; i < children.getLength() && foundNode == null; i++ )
			{
				final Node child = children.item(i);
				if( child.getNodeType() == Node.ELEMENT_NODE )
				{
					final String childName = DOMHelper.stripNamespace(child.getNodeName());
					if( matchAny || nodeName.equals(childName) )
					{
						if( nNumFound != index )
						{
							nNumFound++;
						}
						else
						{
							foundNode = child;
							break;
						}
					}
				}
			}
		}

		if( foundNode == null && create == true )
		{
			// If the Index is 0 and we didn't find a node or if the number
			// found (which is not zero based) equals the index (which is)
			// then this is the same as saying index is one more that the
			// number of nodes that exist then add a new child node.
			if( index == 0 || nNumFound == index )
			{
				if( isAttribute )
				{
					((Element) parent).setAttribute(nodeName, BLANK);
					foundNode = ((Element) parent).getAttributeNode(nodeName);
				}
				else
				{
					foundNode = doc.createElement(nodeName);
					parent.appendChild(foundNode);
				}
			}
			else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		return foundNode;
	}

	private int getIndexValue(final String[] aszNodeName)
	{
		aszNodeName[1] = aszNodeName[0];

		final int index = aszNodeName[0].indexOf('[');
		if( index >= 0 )
		{
			aszNodeName[1] = aszNodeName[0].substring(0, index);

			// Sanity Check
			if( index + 1 < aszNodeName[0].length() )
			{
				final String szIndex = aszNodeName[0].substring(index + 1, aszNodeName[0].length() - 1);
				return Integer.parseInt(szIndex);
			}
		}

		// Index is the first instance.
		return 0;
	}

	/**
	 * Gets the node for the given path.
	 */
	private Node getNodeHelper(final String path, final boolean bCreate, final boolean bNew)
	{
		// Split on the path identifier
		final List<String> pathComponents = DOMHelper.splitPath(path);

		Node node = m_elRoot;
		final Document doc = node.getOwnerDocument();

		final Iterator<String> iter = pathComponents.iterator();
		while( iter.hasNext() && node != null )
		{
			final String[] aszNodeName = new String[2];
			aszNodeName[0] = iter.next();

			// Extract the index if that exists
			final int nIndex = getIndexValue(aszNodeName);
			if( bNew && !iter.hasNext() )
			{
				final Node child = doc.createElement(aszNodeName[1]);
				node.appendChild(child);
				return child;
			}
			else
			{
				node = lookupNode(node, aszNodeName[1], nIndex, bCreate);
			}
		}

		return node;
	}

	private Writer genXML(final Writer sbuf, final Node subRoot)
	{
		try
		{
			boolean bEndElem = false;
			final int type = subRoot.getNodeType();
			switch( type )
			{
				case Node.DOCUMENT_TYPE_NODE: {
					final DocumentType doctype = (DocumentType) subRoot;
					sbuf.write("<!DOCTYPE ");
					sbuf.write(doctype.getName());
					// see Jira Defect TLE-1295 :
					// http://apps.dytech.com.au/jira/browse/TLE-1295
					// Tidy DOMs don't correctly support this functionality
					if( doctype.getPublicId() != null )
					{
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
				}
				case Node.ELEMENT_NODE: {
					sbuf.write('<');
					sbuf.write(subRoot.getNodeName());
					final NamedNodeMap nnm = subRoot.getAttributes();
					if( nnm != null )
					{
						final int len = nnm.getLength();
						Attr attr;
						for( int i = 0; i < len; i++ )
						{
							attr = (Attr) nnm.item(i);
							sbuf.write(' ' + attr.getNodeName() + "=\"" + ent(attr.getNodeValue()) + '"');
						}
					}
					// Check for an empty parent element
					// no children, or a single TEXT_NODE with length() == 0
					final Node child = subRoot.getFirstChild();
					if( child == null
						|| (child.getNodeType() == Node.TEXT_NODE && (child.getNextSibling() == null && child
							.getNodeValue().length() == 0)) )
					{
						sbuf.write(PATH_SEP);
					}
					else
					{
						bEndElem = true;
					}
					sbuf.write('>');
					break;
				}
				case Node.ENTITY_REFERENCE_NODE: {

					sbuf.write('&' + subRoot.getNodeName() + ';');
					break;
				}
				case Node.CDATA_SECTION_NODE: {
					sbuf.write("<![CDATA[" + subRoot.getNodeValue() + "]]>");
					break;
				}
				case Node.TEXT_NODE: {
					sbuf.write(ent(subRoot.getNodeValue()));
					break;
				}
				case Node.PROCESSING_INSTRUCTION_NODE: {
					sbuf.write("<?" + subRoot.getNodeName());
					final String data = subRoot.getNodeValue();
					if( data != null && data.length() > 0 )
					{
						sbuf.write(' ');
						sbuf.write(data);
					}
					sbuf.write("?>");
					break;
				}
				case Node.COMMENT_NODE: {
					sbuf.write("<!--" + subRoot.getNodeValue() + "-->");
					break;
				}
			}

			for( Node child = subRoot.getFirstChild(); child != null; child = child.getNextSibling() )
			{
				genXML(sbuf, child);
			}

			if( bEndElem )
			{
				sbuf.write("</" + subRoot.getNodeName() + ">");
			}
		}
		catch( final IOException e )
		{
			throw propagate(e);
		}

		return sbuf;
	}

	private String ent(final String text)
	{
		final StringBuilder szOut = new StringBuilder();
		final char[] chars = text.toCharArray();
		for( final char ch : chars )
		{
			switch( ch )
			{
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
					if( ch == 0xA || ch == 0xD || ch == 0x9 || (ch >= 0x20 && ch <= 0x007F) )
					{
						szOut.append(ch);
					}
					else if( (ch > 0x007F && ch <= 0xD7FF) || (ch >= 0xE000 && ch <= 0xFFFD)
						|| (ch >= 0x10000 && ch <= 0x10FFFF) )
					{
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

	private String hex(final char c)
	{
		final String hexed = Integer.toHexString(c);
		final int deficit = 4 - hexed.length();
		// wooo, unrolled loops
		switch( deficit )
		{
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

	private Node getRootDoc()
	{
		if( m_elRoot.getOwnerDocument().getDocumentElement() == m_elRoot )
		{
			return m_elRoot.getOwnerDocument();
		}
		else
		{
			return m_elRoot;
		}
	}

	@Override
	public String toString()
	{
		ensureRoot();
		// genXML returns the same StringBuffer that we pass in.
		final StringWriter stringWriter = new StringWriter();
		genXML(new BadCharacterFilterWriter(stringWriter), getRootDoc());
		return stringWriter.toString();
	}

	/**
	 * Iterates over all nodes matching the given parent and name.
	 */
	private abstract static class InternalIterator<T> implements Iterator<T>, Iterable<T>
	{
		protected Node parent;
		protected String name;
		protected Node upto;
		protected Node last;
		protected Node root;

		public InternalIterator(final Node parent, final Node first, final String name, final Node root)
		{
			this.parent = parent;
			this.name = name;
			this.root = root;
			upto = first;
			if( parent == null )
			{
				upto = null;
			}
			else if( upto == null )
			{
				upto = DOMHelper.findNext(parent.getFirstChild(), name);
			}
		}

		protected void moveOn()
		{
			last = upto;
			if( upto == root )
			{
				upto = null;
			}
			else
			{
				upto = DOMHelper.findNext(last.getNextSibling(), name);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<T> iterator()
		{
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			if( last == null )
			{
				throw new IllegalStateException();
			}
			parent.removeChild(last);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return upto != null;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next()
		{
			moveOn();
			return getNextValue();
		}

		protected abstract T getNextValue();
	}

	/**
	 * Iterates over all nodes matching the given parent and name. The iteration
	 * will return PropBagEx's rooted at each node.
	 */
	public static class PropBagIterator extends InternalIterator<PropBagMin>
	{
		public PropBagIterator(final Node parent, final Node first, final String name, final Node root)
		{
			super(parent, first, name, root);
		}

		@Override
		protected PropBagMin getNextValue()
		{
			return new PropBagMin(last, true);
		}
	}

	/**
	 * Iterates over all nodes from the parent, that match the given path at any
	 * of it's components.
	 */
	private abstract static class ListOfNodesIterator<T> implements Iterator<T>, Iterable<T>
	{
		protected final LinkedList<Node> nodes = new LinkedList<Node>();

		protected Node last;

		protected void moveOn()
		{
			last = nodes.removeFirst();
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<T> iterator()
		{
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			if( last == null )
			{
				throw new IllegalStateException();
			}
			last.getParentNode().removeChild(last);
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return !nodes.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next()
		{
			moveOn();
			return getNextValue();
		}

		protected abstract T getNextValue();
	}

	/**
	 * Iterates over all nodes from the parent, that match the given path at any
	 * of it's components.
	 */
	private abstract static class InternalThoroughIterator<T> extends ListOfNodesIterator<T>
	{
		public InternalThoroughIterator(final Node parent, final String path)
		{
			final List<String> names = Lists.newArrayList(path.split(PATH_SEP));
			for( final Iterator<String> iter = names.iterator(); iter.hasNext(); )
			{
				final String p = iter.next();
				if( p.trim().length() == 0 )
				{
					iter.remove();
				}
			}

			if( !names.isEmpty() )
			{
				findAllNodes(parent, names, 0);
			}
		}

		protected void findAllNodes(final Node parent, final List<String> names, final int index)
		{
			final String path = names.get(index);
			if( !path.startsWith(ATTR) )
			{
				Node find = DOMHelper.findNext(parent.getFirstChild(), path);
				while( find != null )
				{
					if( index == names.size() - 1 )
					{
						nodes.add(find);
					}
					else
					{
						findAllNodes(find, names, index + 1);
					}
					find = DOMHelper.findNext(find.getNextSibling(), path);
				}
			}
			else
			{
				if( index == names.size() - 1 )
				{
					final Node attr = parent.getAttributes().getNamedItem(path.substring(1));
					nodes.add(attr);
				}
				else
				{
					throw new RuntimeException("Xpath has an attribute component before the last component");
				}
			}
		}
	}

	/**
	 * Iterates over all nodes from the parent, that match the given path at any
	 * of it's components. The iteration will return PropBag's rooted at each of
	 * each nodes.
	 */
	public static class PropBagMinThoroughIterator extends InternalThoroughIterator<PropBagMin>
	{
		public PropBagMinThoroughIterator(final Node parent, final String name)
		{
			super(parent, name);
		}

		@Override
		protected PropBagMin getNextValue()
		{
			return new PropBagMin(last, true);
		}
	}

	/**
	 * Iterates over all nodes from the parent, that match the given path at any
	 * of it's components. The iteration will return each Node.
	 */
	public static class NodeThoroughIterator extends InternalThoroughIterator<Node>
	{
		public NodeThoroughIterator(final Node parent, final String name)
		{
			super(parent, name);
		}

		@Override
		protected Node getNextValue()
		{
			return last;
		}
	}

	/**
	 * Creates an iterator which return's PropBagEx's for all children of the
	 * root element
	 */
	public PropBagIterator iterator()
	{
		ensureRoot();
		return new PropBagIterator(m_elRoot, null, null, m_elRoot);
	}

	/**
	 * Creates an iterator which return's PropBagEx's for each child for the
	 * path given<br>
	 * E.g.<br>
	 * /path/node/item<br>
	 * Will iterate over all the "item" nodes.
	 * 
	 * @param path
	 * @return
	 */
	public PropBagIterator iterator(final String path)
	{
		ensureRoot();

		String name = null;
		Node parent = null;

		final Node node = getNodeHelper(path, false, false);
		if( node != null )
		{
			parent = node.getParentNode();

			// see Jira Defect TLE-1293 :
			// http://apps.dytech.com.au/jira/browse/TLE-1293
			name = DOMHelper.stripNamespace(node.getNodeName());
			if( path.endsWith(WILD) )
			{
				name = WILD;
			}

		}
		return new PropBagIterator(parent, node, name, m_elRoot);
	}

	/**
	 * Iterates over all nodes that match the given path, like an XSLT does.
	 */
	public PropBagMinThoroughIterator iterateAll(final String path)
	{
		ensureRoot();
		return new PropBagMinThoroughIterator(m_elRoot, path);
	}

	private void setXML(final Reader reader)
	{
		BufferedReader filterer = null;
		try
		{
			filterer = new BufferedReader(new BadCharacterFilterReader(reader));
			DocumentBuilder builder;

			synchronized( factory )
			{
				builder = factory.newDocumentBuilder();
			}
			Document doc = builder.parse(new InputSource(filterer));

			// Get the root element
			m_elRoot = doc.getDocumentElement();
		}
		catch( Exception ex )
		{
			throw propagate(ex);
		}
		finally
		{
			if( filterer != null )
			{
				try
				{
					Closeables.close(filterer, false);
				}
				catch( IOException e )
				{
					// Whatever
				}
			}
		}
	}

	/**
	 * retrieves the text delimited by the given node's tags
	 * 
	 * @param path full name of the node, parents qualified by '/'
	 * @return String content of element
	 */
	public String getNode(final String path)
	{
		return getNode(path, BLANK);
	}

	/**
	 * retrieves the text delimited by the given node's tags
	 * 
	 * @param path full name of the node, parents qualified by '/'
	 * @return String content of element
	 */
	public String getNode(final String path, final String defaultValue)
	{
		ensureRoot();
		final Node oNode = getNodeHelper(path, false, false);
		return DOMHelper.getValueForNode(oNode, defaultValue);
	}

	/**
	 * Retrieves a node as an int value given it's name. If the node does not
	 * exist or its value is an invalid integer, then the default value is
	 * returned.
	 * 
	 * @param szFullNodeName full name of the node, parents qualified by '/'
	 * @param defaultValue A default value to return if the node does not exist.
	 * @return value of the node.
	 */
	public int getIntNode(final String path, final int defaultValue)
	{
		ensureRoot();
		try
		{
			return Integer.parseInt(getNode(path));
		}
		catch( final NumberFormatException ex )
		{
			return defaultValue;
		}
	}

	public void setNode(final String path, final String value)
	{
		ensureRoot();
		final Node node = getNodeHelper(path, true, false);
		DOMHelper.setValueForNode(node, value);
	}

	/**
	 * Checks to see if a node exists
	 * 
	 * @param path The path to the node.
	 * @return true if one or more nodes for this path exist
	 */
	public boolean nodeExists(final String path)
	{
		ensureRoot();
		Node node = getNodeHelper(path, false, false);
		return node != null;
	}

	/**
	 * Creates a new PropBag rooted at the given path, yet sharing the same DOM
	 * nodes as the creator.
	 * 
	 * @param path Xpath to the root of the new tree.
	 * @return PropBagEx PropBag rooted at the subtree, or null if the path does
	 *         not exist.
	 */
	public PropBagMin getSubtree(final String path)
	{
		ensureRoot();
		final Element root = (Element) getNodeHelper(path, false, false);

		if( root == null )
		{
			return null;
		}
		else
		{
			return new PropBagMin(root, true);
		}
	}

	/**
	 * Returns the name of the root element.
	 * 
	 * @return the name of the node.
	 */
	public String getNodeName()
	{
		ensureRoot();
		return getNodeHelper(BLANK, false, false).getNodeName();
	}

	private Node importNode(final Document doc, final Node n, final boolean bDeep)
	{
		Node dest;
		final int type = n.getNodeType();
		switch( type )
		{
			case Node.ELEMENT_NODE:
				dest = doc.createElement(n.getNodeName());
				final NamedNodeMap nnm = n.getAttributes();
				final int nnmCount = nnm.getLength();
				for( int i = 0; i < nnmCount; i++ )
				{
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
		if( bDeep )
		{
			for( Node child = n.getFirstChild(); child != null; child = child.getNextSibling() )
			{
				dest.appendChild(importNode(doc, child, true));
			}
		}
		return dest;
	}

	private void clear()
	{
		try
		{
			Document doc;
			synchronized( factory )
			{
				doc = factory.newDocumentBuilder().newDocument();
			}

			// Create the empty Propbag
			final Element root = doc.createElement("xml");
			doc.appendChild(root);

			// Get the root element
			m_elRoot = doc.getDocumentElement();
		}
		catch( final ParserConfigurationException pce )
		{
			throw propagate(pce);
		}
	}

	private RuntimeException propagate(Throwable t)
	{
		return Throwables.propagate(t);
	}
}