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

package com.tle.freetext;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Field;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.entity.Schema.SchemaNode;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.freetext.htmlfilter.HTMLFilter;

public class XmlSchemaIndexer
{
	private static final String XPATH_EXT = "/$XPATH$"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger(XmlSchemaIndexer.class);

	private final Map<String, StringBuilder> pathValuesMap = new HashMap<String, StringBuilder>();
	private final Set<String> pathsIndexed = new HashSet<String>();
	private final List<Field> indexedFields = new ArrayList<Field>();

	public void indexOneValue(SchemaNode schemaNode, String itemNamePath, String fullPath, String xpath, String value)
	{
		if( schemaNode.isFieldIndexed() )
		{
			try
			{
				// See if it is a date
				UtcDate d = new UtcDate(value, Dates.ISO);
				value = d.format(Dates.ISO);
			}
			catch( ParseException ex )
			{
				// Ignore
			}
			catch( NumberFormatException nfe )
			{
				// Ignore
			}
			pathsIndexed.add(fullPath);
			indexedFields.add(AbstractIndexingExtension.indexed(xpath + XPATH_EXT, value));
			indexedFields.add(AbstractIndexingExtension.indexed(fullPath, value));
			StringBuilder builder = pathValuesMap.get(fullPath);
			if( builder == null )
			{
				builder = new StringBuilder(value);
				pathValuesMap.put(fullPath, builder);
			}
			else
			{
				builder.append(' ');
				builder.append(value);
			}
		}

		if( schemaNode.isIndexedInBody() )
		{
			String type = schemaNode.getType();
			if( type.equals("html") && value.length() > 0 ) //$NON-NLS-1$
			{
				try
				{
					ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes("UTF-8")); //$NON-NLS-1$
					value = new HTMLFilter(bais).getSummary(16384);
				}
				catch( Exception e )
				{
					LOGGER.warn(e);
					value = ""; //$NON-NLS-1$
				}
			}
			else if( !type.equals("text") ) //$NON-NLS-1$
			{
				value = ""; //$NON-NLS-1$
			}

			// don't index item name in FIELD_BODY
			if( value.length() > 0 && !itemNamePath.trim().equals(fullPath) )
			{
				indexedFields.add(AbstractIndexingExtension.unstoredAndVectored(FreeTextQuery.FIELD_BODY, value));
				indexedFields
					.add(AbstractIndexingExtension.unstoredAndVectored(FreeTextQuery.FIELD_BODY_NOSTEM, value));
			}
		}
	}

	public void indexChildNodes(SchemaNode parentNode, String itemNamePath, String fullPath, String xpath, PropBagEx xml)
	{
		List<SchemaNode> childNodes = parentNode.getChildNodes();
		for( SchemaNode schemaNode : childNodes )
		{
			String thisFullPath = fullPath + '/' + schemaNode.getName();
			String thisXPath = xpath + '/' + schemaNode.getName();
			if( schemaNode.isAttribute() )
			{
				indexOneValue(schemaNode, itemNamePath, thisFullPath, thisXPath, xml.getNode(schemaNode.getName()));
			}
			else
			{
				PropBagIterator iter = xml.iterator(schemaNode.getName());
				int index = 0;
				while( iter.hasNext() )
				{
					PropBagEx childXml = iter.next();
					String indexedPath = thisXPath;

					if( schemaNode.isXpathIndexed() && index > 0 )
					{
						indexedPath += "[" + index + "]"; //$NON-NLS-1$//$NON-NLS-2$
					}

					if( schemaNode.hasChildren() )
					{
						indexChildNodes(schemaNode, itemNamePath, thisFullPath, indexedPath, childXml);
					}

					// Even if the schemaNode has children, they may just be
					// attributes, so we need to see if the node needs indexing.
					indexOneValue(schemaNode, itemNamePath, thisFullPath, indexedPath, childXml.getNode());

					index++;
				}
			}
		}
	}

	public Map<String, StringBuilder> getPathValuesMap()
	{
		return pathValuesMap;
	}

	public Set<String> getPathsIndexed()
	{
		return pathsIndexed;
	}

	public List<Field> getIndexedFields()
	{
		return indexedFields;
	}
}
