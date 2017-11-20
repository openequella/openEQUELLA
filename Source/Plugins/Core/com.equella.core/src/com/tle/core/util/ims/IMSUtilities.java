/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.util.ims;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.common.Utils;
import com.tle.core.util.ims.beans.IMSManifest;

/**
 * Provides various methods for dealing with IMS manifests.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public final class IMSUtilities
{
	public static final String KEY_EXPAND_IMS_PACKAGE = "EXPAND_IMS_PACKAGE";
	public static final String IMS_MANIFEST = "imsmanifest.xml";
	public static final String IMS_MANIFEST_COMBINED = "imsmanifest-combined.xml";

	private IMSUtilities()
	{
		throw new Error("Do not invoke");
	}

	/**
	 * Combines a split-up IMS manifest into a single entity.
	 */
	public static void combine(ManifestResolver resolver, Writer output) throws IOException, XmlPullParserException
	{
		@SuppressWarnings("unused")
		Combiner c = new Combiner(resolver, output);
	}

	/**
	 * Retrieves the title from an IMS manifest.
	 */
	public static String getTitleFromManifest(Reader xml) throws XmlPullParserException, IOException
	{
		StringWriter sr = new StringWriter();
		CharStreams.copy(xml, sr);

		String bufXml = sr.getBuffer().toString();

		String v = getValueForPath("manifest/metadata/lom/general/title/string|langstring", new StringReader(bufXml));
		if( v == null )
		{
			v = getValueForPath(
				"manifest/organizations/organization/item/metadata/lom/general/title/string|langstring",
				new StringReader(bufXml));
		}
		return v;
	}

	/**
	 * Retrieves the description from an IMS manifest.
	 */
	public static String getDescriptionFromManifest(Reader xml) throws XmlPullParserException, IOException
	{
		StringWriter sr = new StringWriter();
		CharStreams.copy(xml, sr);

		String bufXml = sr.getBuffer().toString();

		String v = getValueForPath("manifest/metadata/lom/general/description/string|langstring", new StringReader(
			bufXml));
		if( v == null )
		{
			v = getValueForPath(
				"manifest/organizations/organization/item/metadata/lom/general/description/string|langstring",
				new StringReader(bufXml));
		}
		return v;
	}

	/**
	 * Retrieves the title from an IMS manifest.
	 */
	public static String getRightsDescriptionFromManifest(Reader xml) throws XmlPullParserException, IOException
	{
		return getValueForPath("manifest/metadata/lom/rights/description/string|langstring", xml);
	}

	/**
	 * @param manifestStream Will not be closed.
	 * @return
	 */
	public static String getScormVersionFromStream(InputStream manifestStream)
	{
		String wellWhatWasIt = null;
		try
		{
			wellWhatWasIt = getScormVersion(new UnicodeReader(manifestStream, "UTF-8"), false);
		}
		catch( IOException ioe )
		{
			throw new RuntimeException(ioe);
		}
		catch( XmlPullParserException parsimonious )
		{
			throw new RuntimeException(parsimonious);
		}
		return wellWhatWasIt;
	}

	public static String getScormVersion(Reader xml) throws XmlPullParserException, IOException
	{
		return getScormVersion(xml, true);
	}

	public static String getScormVersion(Reader xml, boolean alwaysReturnDefaultVersion) throws XmlPullParserException,
		IOException
	{
		XmlPullParser parser = new MXParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		parser.setInput(xml);

		parser.next();
		String namespace = parser.getNamespace("adlcp");
		String version = null;
		if( namespace != null )
		{
			if( namespace.equals("http://www.adlnet.org/xsd/adlcp_rootv1p2") )
			{
				version = "1.2";
			}
			else if( namespace.equals("http://www.adlnet.org/xsd/adlcp_v1p3") )
			{
				version = "1.3";
			}
			else if( alwaysReturnDefaultVersion )
			{
				version = "1.2";
			}
		}
		return version;
	}

	/**
	 * Retrieves the value of an XPath from an XML stream.
	 */
	private static String getValueForPath(String xpath, Reader xml) throws XmlPullParserException, IOException
	{
		XmlPullParser parser = new MXParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		parser.setInput(xml);

		int depth = 1;
		String[] splits = xpath.split("/");

		int event = parser.getEventType();
		while( event != XmlPullParser.END_DOCUMENT )
		{
			if( event == XmlPullParser.START_TAG && parser.getDepth() == depth )
			{
				String[] elemNames = splits[depth - 1].split("\\|");
				for( int i = 0; i < elemNames.length; i++ )
				{
					if( parser.getName().equals(elemNames[i]) )
					{
						if( depth == splits.length )
						{
							if( parser.next() == XmlPullParser.TEXT )
							{
								return parser.getText();
							}
							else
							{
								return null;
							}
						}
						else
						{
							depth++;
						}
					}
				}
			}
			event = parser.next();
		}

		return null;
	}

	public static PropBagEx shrinkXML(IMSManifest ims)
	{
		StringBuilder sbuf = new StringBuilder("<xml>");
		ims.addToXMLString(sbuf);
		sbuf.append("</xml>");
		return new PropBagEx(sbuf.toString());
	}

	// ////////////////// HELPER CLASSES
	// /////////////////////////////////////////////////////////

	private static class CollectionHashMap<K, V> extends HashMap<K, Collection<V>>
	{
		private static final long serialVersionUID = 1L;

		public void add(K key, V value)
		{
			Collection<V> col = get(key);
			if( col == null )
			{
				col = new ArrayList<V>();
				super.put(key, col);
			}
			col.add(value);
		}
	}

	/**
	 * Helper class for combining manifests in to a single entity.
	 * 
	 * @author Nicholas Read
	 */
	private static class Combiner
	{
		private static final String ADLCP_LOCATION = "location";
		private static final Set<String> ADLCP_NAMESPACES = new HashSet<String>();

		static
		{
			ADLCP_NAMESPACES.add("http://www.adlnet.org/xsd/adlcp_rootv1p2");
			ADLCP_NAMESPACES.add("http://www.adlnet.org/xsd/adlcp_v1p3");
		}

		private final Set<String> declaredNamespaces = new HashSet<String>();
		private final CollectionHashMap<Integer, String> namespaceDepths = new CollectionHashMap<Integer, String>();
		private XmlPullParser parser;
		private final Writer output;
		private final ManifestResolver resolver;

		public Combiner(ManifestResolver resolver, Writer output) throws IOException, XmlPullParserException
		{
			this.output = output;
			this.resolver = resolver;

			Reader reader = null;
			try
			{
				reader = resolver.getStream();

				parser = new MXParser();
				parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
				parser.setInput(reader);

				parseXml();
			}
			finally
			{
				Closeables.close(reader, true); // Quietly
			}
		}

		private void parseXml() throws IOException, XmlPullParserException
		{
			// Indicates whether the element being written has an open start
			// tag.
			boolean openElement = false;

			int eventType = parser.getEventType();
			while( eventType != XmlPullParser.END_DOCUMENT )
			{
				if( eventType == XmlPullParser.START_TAG )
				{
					// Jira TLE-2307
					if( openElement )
					{
						output.write(">");
						openElement = false;
					}

					String namespace = parser.getNamespace();
					String name = parser.getName();

					if( ADLCP_NAMESPACES.contains(namespace) && name.equals(ADLCP_LOCATION) )
					{
						includeDocument();
					}
					else
					{
						output.write('<');
						addElementName();
						copyAttributes();
						openElement = true;
					}
				}
				else if( eventType == XmlPullParser.END_TAG )
				{
					if( openElement )
					{
						output.write(" />");
						openElement = false;
					}
					else
					{
						output.write("</");
						addElementName();
						output.write('>');
					}
					removeNamespaces();
				}
				else if( eventType == XmlPullParser.TEXT )
				{
					if( openElement )
					{
						output.write(">");
						openElement = false;
					}

					output.write(Utils.ent(parser.getText()));
				}
				eventType = parser.next();
			}
		}

		/**
		 * Add the current elements name (and possible namespace prefix) to the
		 * current output stream.
		 */
		private void addElementName() throws IOException
		{
			String prefix = parser.getPrefix();
			if( prefix != null )
			{
				output.write(prefix);
				output.write(':');
			}
			output.write(parser.getName());
		}

		private void removeNamespaces()
		{
			Collection<String> col = namespaceDepths.remove(parser.getDepth());
			if( col != null )
			{
				for( String ns : col )
				{
					declaredNamespaces.remove(ns);
				}
			}
		}

		/**
		 * Copies over attributes and namespace declarations.
		 */
		private void copyAttributes() throws XmlPullParserException, IOException
		{
			// Add any required namespace declarations
			final int nsCount = parser.getNamespaceCount(parser.getDepth());
			if( nsCount > 0 )
			{
				for( int i = 0; i < nsCount; i++ )
				{
					String uri = parser.getNamespaceUri(i);
					String prefix = parser.getNamespacePrefix(i);
					String uriKey = prefix + "_" + uri;
					if( !declaredNamespaces.contains(uriKey) )
					{
						output.write(" xmlns");
						if( prefix != null )
						{
							output.write(':');
							output.write(prefix);
						}
						output.write("=\"");
						output.write(uri);
						output.write('"');

						declaredNamespaces.add(uriKey);

						namespaceDepths.add(parser.getDepth(), uriKey);
					}
				}
			}

			// Add any attributes
			final int attrCount = parser.getAttributeCount();
			for( int i = 0; i < attrCount; i++ )
			{
				output.write(' ');

				String prefix = parser.getAttributePrefix(i);
				if( prefix != null && prefix.length() > 0 )
				{
					output.write(prefix);
					output.write(':');
				}

				output.write(parser.getAttributeName(i));
				output.write("=\"");
				output.write(Utils.ent(parser.getAttributeValue(i)));
				output.write('"');
			}
		}

		/**
		 * Includes another XML document relative to the current manifest.
		 */
		private void includeDocument() throws IOException, XmlPullParserException
		{
			int event = -1;
			while( event != XmlPullParser.TEXT )
			{
				event = parser.next();
			}

			ManifestResolver newResolver = resolver.getResolverForPath(parser.getText());
			if( newResolver != null )
			{
				@SuppressWarnings("unused")
				Combiner c = new Combiner(newResolver, output);
			}

			while( event != XmlPullParser.END_TAG )
			{
				event = parser.next();
			}
		}
	}

	/**
	 * An interface for resolving relative paths in manifests.
	 * 
	 * @author Nicholas Read
	 */
	public interface ManifestResolver
	{
		/**
		 * Returns the stream for the current manifest.
		 */
		Reader getStream() throws IOException;

		/**
		 * Gets a new manifest resolver based on a path relative to the current
		 * one.
		 */
		ManifestResolver getResolverForPath(String path);
	}

	/**
	 * Implements the manifest resolver for files.
	 * 
	 * @author Nicholas Read
	 */
	public static class FileManifestResolver implements ManifestResolver
	{
		private final File baseFile;

		public FileManifestResolver(File baseFile)
		{
			this.baseFile = baseFile;
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.IMS.IMSUtilities.ManifestResolver#getStream()
		 */
		@Override
		public Reader getStream() throws IOException
		{
			return new UnicodeReader(new FileInputStream(baseFile), "UTF-8");
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.dytech.IMS.IMSUtilities.ManifestResolver#getResolverForPath(java
		 * .lang.String)
		 */
		@Override
		public ManifestResolver getResolverForPath(String path)
		{
			File f = new File(baseFile.getParentFile(), path);
			if( f.exists() )
			{
				return new FileManifestResolver(f);
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * Implements the manifest resolver for a zipped up IMS package.
	 * 
	 * @author Nicholas Read
	 */
	public static class ZipManifestResolver implements ManifestResolver
	{
		private final ZipFile zip;
		private String base;
		private String file;

		public ZipManifestResolver(ZipFile zip)
		{
			this(zip, "imsmanifest.xml");
		}

		private ZipManifestResolver(ZipFile zip, String file)
		{
			this.zip = zip;

			int i = file.lastIndexOf('/');
			if( i < 0 )
			{
				i = file.lastIndexOf('\\');
			}

			if( i < 0 )
			{
				this.base = "";
				this.file = file;
			}
			else
			{
				this.base = file.substring(0, i + 1);
				this.file = file.substring(i + 1);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see com.dytech.IMS.IMSUtilities.ManifestResolver#getStream()
		 */
		@Override
		public Reader getStream() throws IOException
		{
			ZipEntry entry = zip.getEntry(base + file);
			if( entry != null )
			{
				System.out.println("Returning " + entry.getName());
				return new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
			}
			else
			{
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * com.dytech.IMS.IMSUtilities.ManifestResolver#getResolverForPath(java
		 * .lang.String)
		 */
		@Override
		public ManifestResolver getResolverForPath(String path)
		{
			return new ZipManifestResolver(zip, base + path);
		}
	}
}
