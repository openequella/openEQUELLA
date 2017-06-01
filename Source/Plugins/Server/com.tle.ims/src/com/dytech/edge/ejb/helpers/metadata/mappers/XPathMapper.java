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

package com.dytech.edge.ejb.helpers.metadata.mappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.ejb.helpers.metadata.mapping.GenericPackageMapping;
import com.dytech.edge.ejb.helpers.metadata.mapping.Mapping;
import com.google.common.base.Throwables;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.mapping.IMSMapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;
import com.tle.common.Check;
import com.tle.common.Pair;

/**
 * @author aholland
 */
public class XPathMapper extends Mapper
{
	private static final long serialVersionUID = 1L;

	private final Schema schema;

	public XPathMapper(Schema schema)
	{
		this.schema = schema;
	}

	public void processMapping(Collection<IMSMapping> collection)
	{
		if( collection != null )
		{
			for( IMSMapping mapping : collection )
			{
				String itemdefNode = mapping.getItemdef();
				String data = mapping.getIms();
				boolean isRepeat = mapping.isRepeat();

				// Used for html mapper to set to lower case
				data = process(data);
				String type = mapping.getType();
				MappingType mappingType = MappingType.SIMPLE;
				if( type != null && type.trim().length() > 0 )
				{
					mappingType = Enum.valueOf(MappingType.class, type.toUpperCase());
				}

				setValue(data, itemdefNode, mappingType, isRepeat);
			}
		}
	}

	public Collection<Mapping> map(InputStream in)
	{
		Reader mets = new UnicodeReader(in, Constants.UTF8);

		XmlPullParser xpp = new MXParser();
		try
		{
			xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			xpp.setInput(mets);
		}
		catch( XmlPullParserException e )
		{
			throw new RuntimeException(e);
		}
		RewindableParser parser = new RewindableParser(xpp);

		List<Mapping> list = new ArrayList<Mapping>();
		boolean firstNode = true;
		try
		{
			NodeEvent event = parser.current();

			List<String> nodes = new ArrayList<String>();
			while( event == null || event.status != XmlPullParser.END_DOCUMENT )
			{
				if( event == null )
				{
					event = parser.next();
					continue;
				}

				if( event.status == XmlPullParser.START_TAG )
				{
					String name = event.name;
					if( firstNode )
					{
						firstNode = false;
					}
					else
					{
						nodes.add(name);
					}

					String nodePath = getNodePath(nodes);
					if( processCompoundMappings(parser, list, nodePath) )
					{
						nodes.remove(nodes.size() - 1);
					}
				}
				else if( event.status == XmlPullParser.END_TAG )
				{
					if( nodes.size() > 0 )
					{
						nodes.remove(nodes.size() - 1);
					}
				}
				else if( event.status == XmlPullParser.TEXT && !event.whitespace )
				{
					String nodePath = getNodePath(nodes);
					processMappings(parser, list, nodePath);
				}

				event = parser.next();
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}

		return list;
	}

	private String getNodePath(List<String> list)
	{
		Iterator<String> i = list.iterator();
		StringBuilder buffer = new StringBuilder();

		while( i.hasNext() )
		{
			if( buffer.length() != 0 )
			{
				buffer.append("/"); //$NON-NLS-1$
			}
			buffer.append(i.next());
		}
		return buffer.toString();
	}

	private void processMappings(RewindableParser parser, List<Mapping> list, String node)
	{
		Collection<Mapping> col = get(node);
		if( col != null )
		{
			for( Mapping pair : col )
			{
				// See #543. Compound should never be mapped as a non compound!
				if( !pair.isCompound() )
				{
					list.add(new Mapping(pair.getData(), parser.current().text, pair.getType(), pair.isRepeat()));
				}
			}
		}
	}

	private boolean processCompoundMappings(RewindableParser parser, List<Mapping> list, String node)
		throws XmlPullParserException, IOException
	{
		Collection<Mapping> col = get(node);
		boolean found = false;
		if( col != null )
		{
			for( Mapping pair : col )
			{
				String itemdefpath = pair.getData();
				if( pair.isCompound() )
				{
					if( found )
					{
						parser.rewind();
					}
					else
					{
						parser.startRecording();
					}

					found = true;
					Mapping mapping = generateCompoundMappings(parser, list, node, itemdefpath);
					if( mapping != null )
					{
						list.add(mapping);
					}
				}
			}
		}
		parser.clear();

		return found;
	}

	private Mapping createMapping(String itemdefpath, PropBagEx xml)
	{
		return new GenericPackageMapping(itemdefpath, xml);
	}

	private Mapping generateCompoundMappings(RewindableParser parser, List<Mapping> list, String rootPath,
		String itemdefpath) throws XmlPullParserException, IOException
	{
		final Map<String, String> attrs = getAttributeMap(parser, schema, itemdefpath);

		final PropBagEx xml = recurseCompoundMappings(parser, list, rootPath, itemdefpath);

		if( xml != null )
		{
			if( attrs != null )
			{
				setAttributes(xml, attrs);
			}

			return createMapping(itemdefpath, xml);
		}
		return null;
	}

	@SuppressWarnings("nls")
	private PropBagEx recurseCompoundMappings(RewindableParser parser, List<Mapping> list, String rootPath,
		String itemdefpath) throws XmlPullParserException, IOException
	{
		final PropBagEx xml = new PropBagEx();

		NodeEvent event = parser.next();
		while( event.status != XmlPullParser.END_DOCUMENT && event.status != XmlPullParser.END_TAG )
		{
			if( event.status == XmlPullParser.START_TAG )
			{
				final String name = event.name;
				final String fullpath = itemdefpath + "/" + name;
				final Map<String, String> attrs = getAttributeMap(parser, schema, fullpath);
				final PropBagEx childXml = recurseCompoundMappings(parser, list, rootPath + "/" + name, fullpath);

				if( childXml != null )
				{
					boolean isLangstring = name.equalsIgnoreCase("langstring");
					boolean isNotInSchema = !schemaContainsNode(fullpath);
					if( isLangstring && isNotInSchema )
					{
						xml.setNode("", childXml.getNode(""));
					}
					else
					{
						PropBagEx temp = xml.newSubtree(name);
						String value = childXml.getNode("");
						if( value.length() > 0 )
						{
							temp.setNode("", value);
						}
						else
						{
							temp.appendChildren("", childXml);
						}

						if( attrs != null )
						{
							setAttributes(temp, attrs);
						}
					}
				}
				else if( attrs != null )
				{
					setAttributes(xml.newSubtree(name), attrs);
				}
			}
			else if( event.status == XmlPullParser.TEXT )
			{
				if( !event.whitespace )
				{
					boolean inSchema = schemaContainsNode(itemdefpath);
					int index = itemdefpath.indexOf("/langstring");

					if( !inSchema && index > 0 )
					{
						itemdefpath = itemdefpath.substring(0, index);
						inSchema = schemaContainsNode(itemdefpath);
					}

					if( inSchema )
					{
						xml.setNode("", event.text);
					}

					processMappings(parser, list, rootPath);
				}
			}

			event = parser.next();
		}

		return xml.nodeExists("*") || !Check.isEmpty(xml.getNode()) ? xml : null;
	}

	private Map<String, String> getAttributeMap(RewindableParser parser, Schema schema, String itemdefpath)
	{
		Map<String, String> rv = null;

		NodeEvent node = parser.current;
		final int count = node.attributes.length;
		for( int i = 0; i < count; i++ )
		{
			Pair<String, String> attr = node.attributes[i];
			String aname = attr.getFirst();
			if( schema.withDefinition(new IsNodeTrue(itemdefpath + "/" + aname + "/@attribute")) ) //$NON-NLS-1$ //$NON-NLS-2$
			{
				String avalue = attr.getSecond();
				if( rv == null )
				{
					rv = new HashMap<String, String>();
				}
				rv.put(aname, avalue);
			}
		}

		return rv;
	}

	private void setAttributes(PropBagEx xml, Map<String, String> attrs)
	{
		for( Map.Entry<String, String> attr : attrs.entrySet() )
		{
			xml.setNode("@" + attr.getKey(), attr.getValue()); //$NON-NLS-1$
		}
	}

	private boolean schemaContainsNode(String node)
	{
		return schema.withDefinition(new ContainsNode(node));
	}

	private static class IsNodeTrue implements Schema.WithDefinition<Boolean>
	{
		private final String path;

		public IsNodeTrue(String path)
		{
			this.path = path;
		}

		@Override
		public Boolean execute(PropBagEx schema)
		{
			return schema.isNodeTrue(path);
		}
	}

	private static class ContainsNode implements Schema.WithDefinition<Boolean>
	{
		private final String path;

		public ContainsNode(String path)
		{
			this.path = path;
		}

		@Override
		public Boolean execute(PropBagEx schema)
		{
			return schema.nodeExists(path);
		}
	}

	private static class RewindableParser
	{
		private final XmlPullParser parser;
		private final List<NodeEvent> history;
		private NodeEvent current;
		// when rewind called we need to move back to this node (it was never
		// recorded)
		private NodeEvent rewindPoint;

		private boolean recording;
		private boolean playback;
		private int playbackIndex;

		protected RewindableParser(XmlPullParser parser)
		{
			this.parser = parser;
			history = new ArrayList<NodeEvent>();
			playbackIndex = -1;
		}

		protected void startRecording()
		{
			recording = true;
			playback = false;
			playbackIndex = -1;
			rewindPoint = current;
		}

		protected void rewind()
		{
			recording = false;
			playback = true;
			playbackIndex = -1;
			current = rewindPoint;
		}

		protected void clear()
		{
			recording = false;
			playback = false;
			playbackIndex = -1;
			rewindPoint = null;
			history.clear();
		}

		protected NodeEvent next() throws XmlPullParserException, IOException
		{
			if( playback )
			{
				playbackIndex++;
				current = history.get(playbackIndex);
			}
			else
			{
				int code = parser.next();
				NodeEvent event = new NodeEvent();
				event.status = code;
				if( code == XmlPullParser.START_TAG )
				{
					event.name = parser.getName();
					@SuppressWarnings("unchecked")
					Pair<String, String>[] convenientDevice = new Pair[parser.getAttributeCount()];
					event.attributes = convenientDevice;
					for( int i = 0; i < event.attributes.length; i++ )
					{
						Pair<String, String> attr = new Pair<String, String>(parser.getAttributeName(i),
							parser.getAttributeValue(i));
						event.attributes[i] = attr;
					}
				}
				else if( code == XmlPullParser.TEXT )
				{
					event.whitespace = parser.isWhitespace();
					event.text = parser.getText();
				}
				if( recording )
				{
					history.add(event);
				}
				current = event;
			}
			return current;
		}

		protected NodeEvent current()
		{
			return current;
		}
	}

	private static class NodeEvent
	{
		int status;
		String name;
		String text;
		boolean whitespace;
		Pair<String, String>[] attributes;
	}
}