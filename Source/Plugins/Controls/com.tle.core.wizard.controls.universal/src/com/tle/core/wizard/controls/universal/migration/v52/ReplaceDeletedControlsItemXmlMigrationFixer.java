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

package com.tle.core.wizard.controls.universal.migration.v52;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.google.inject.Inject;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.XmlHelper;
import com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsItemXmlMigration;
import com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsXmlMigration;
import com.tle.core.xml.XmlDocument;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReplaceDeletedControlsItemXmlMigrationFixer extends ReplaceDeletedControlsItemXmlMigration
{
	@Inject
	private XmlHelper xmlHelper;

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile itemsHandle, String filename)
	{
		boolean changed = false;

		PropBagEx metadata = null;

		PropBagIterator iter = xml.iterator("attachments/*");
		final Map<ItemId, String> itemAttachments = new HashMap<ItemId, String>();

		while( iter.hasNext() )
		{
			PropBagEx axml = iter.next();

			String urlVal = axml.getNode("url");
			if( axml.getNodeName().equals("com.tle.beans.item.attachments.FileAttachment")
				&& urlVal.startsWith("_zips/") )
			{
				iter.remove();
				changed = true;
				continue;
			}
			else
				if( axml.getNodeName().equals("com.tle.beans.item.attachments.ZipAttachment")
					&& !urlVal.startsWith("_zips/") )
			{
				axml.setNode("description", urlVal);
				axml.setNode("url", "_zips/" + urlVal);
				changed = true;
			}
			else if( axml.getNodeName().equals("com.tle.beans.item.attachments.ItemAttachment") )
			{
				String itemIdString = axml.getNode("value1");
				final ItemId itemId;

				if( Check.isEmpty(itemIdString) )
				{
					String file = axml.getNode("url");

					if( metadata == null )
					{
						metadata = xmlHelper.readToPropBagEx(getMetadataXml(itemsHandle, filename), null);
					}
					itemId = new ItemId(metadata.getNode(file + "/@uuid"), metadata.getIntNode(file + "/@version"));
				}
				else
				{
					itemId = new ItemId(itemIdString);
				}
				itemAttachments.put(itemId, axml.getNode("uuid"));

				// convert it to a CustomAttachment
				axml.setNodeName("com.tle.beans.item.attachments.CustomAttachment");
				axml.setNode("value1", "resource");
				axml.deleteNode("url");

				axml.deleteNode("data");
				PropBagEx data = axml.newSubtree("data");
				PropBagEx entry = data.newSubtree("entry");
				entry.createNode("string", "uuid");
				entry.createNode("string", itemId.getUuid());

				entry = data.newSubtree("entry");
				entry.createNode("string", "type");
				entry.createNode("string", "p");

				entry = data.newSubtree("entry");
				entry.createNode("string", "version");
				entry.createNode("int", Integer.toString(itemId.getVersion()));

				changed = true;
				continue;
			}

			// Create a value for the control to map to the attachment
			final String handler = getHandler(axml);
			if( handler != null )
			{
				if( metadata == null )
				{
					metadata = xmlHelper.readToPropBagEx(getMetadataXml(itemsHandle, filename), null);
				}
				final XmlDocument xMetadata = new XmlDocument(metadata.toString());

				String uuid = axml.getNode("uuid");
				if( xMetadata.node("xml/" + ReplaceDeletedControlsXmlMigration.getXpathForHandler(handler) + "[text()='"
					+ uuid + "']") == null )
				{
					metadata.createNode(ReplaceDeletedControlsXmlMigration.getXpathForHandler(handler), uuid);
				}
			}
		}

		if( !itemAttachments.isEmpty() )
		{
			if( metadata == null )
			{
				metadata = xmlHelper.readToPropBagEx(getMetadataXml(itemsHandle, filename), null);
			}
			final XmlDocument xMetadata = new XmlDocument(metadata.toString());

			for( Entry<ItemId, String> entry : itemAttachments.entrySet() )
			{
				final String uuid = entry.getValue();
				final ItemId itemId = entry.getKey();

				// There must be an item finder control. We need to look for ALL
				// nodes with
				// [@uuid=UUID and @version=version] and add the attachment UUID
				// underneath them
				for( Node node : xMetadata
					.nodeList("//*[@uuid='" + itemId.getUuid() + "' and @version='" + itemId.getVersion() + "']") )
				{
					// Don't modify attachment nodes! (actually, these probably
					// don't exist in ItemXml anyway)
					if( !node.getNodeName().equals("attachment") )
					{
						node.setTextContent(uuid);
					}
				}
			}

			// Unique to the Fixer! Need to remove ItemAttachments from the
			// Navigation Nodes
			XmlDocument xXml = new XmlDocument(xml.getRootElement().getOwnerDocument());
			for( Node node : xXml.nodeList("//com.tle.beans.item.attachments.ItemNavigationTab"
				+ "/attachment[@class='com.tle.beans.item.attachments.ItemAttachment']") )
			{
				node.getParentNode().removeChild(node);
				changed = true;
			}

			metadata = new PropBagEx(xMetadata.toString());
		}

		if( metadata != null )
		{
			xmlHelper.writeFromPropBagEx(getMetadataXml(itemsHandle, filename), null, metadata);
		}

		return changed;
	}

	private static String getHandler(PropBagEx axml)
	{
		return getHandler(axml.getNodeName(), axml.getNode("value1"));
	}
}
