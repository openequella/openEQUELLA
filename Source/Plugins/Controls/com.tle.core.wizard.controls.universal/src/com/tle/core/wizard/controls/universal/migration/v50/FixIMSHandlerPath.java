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

package com.tle.core.wizard.controls.universal.migration.v50;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;

import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;
import org.w3c.dom.Node;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemXml;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsItemDatabaseMigration.FakeAttachment;
import com.tle.core.wizard.controls.universal.migration.v50.ReplaceDeletedControlsItemDatabaseMigration.FakeItem;
import com.tle.core.xml.XmlDocument;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class FixIMSHandlerPath extends AbstractHibernateDataMigration
{
	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.wizard.controls.universal.migration.associateattachments.imsfix.title",
			"");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final ScrollableResults iter = session
			.createQuery("from Item i inner join i.attachments as a where a.type in ('ims','item')").scroll();
		while( iter.next() )
		{
			final FakeItem item = (FakeItem) iter.get()[0];
			final ItemXml itemXml = item.getItemXml();
			final XmlDocument xml = new XmlDocument(itemXml.getXml());
			final Map<ItemId, String> itemAttachments = new HashMap<ItemId, String>();
			boolean modified = false;

			Iterator<FakeAttachment> attiter = item.getAttachments().iterator();
			while( attiter.hasNext() )
			{
				final FakeAttachment attachment = attiter.next();
				final String uuid = attachment.uuid;
				if( attachment.type.equals("ims") )
				{
					for( Node attachSchemaNode : xml.nodeList("xml/item/temp_fileHandler") )
					{
						if( XmlDocument.getTextContent(attachSchemaNode).equals(uuid) )
						{
							XmlDocument.renameNode(attachSchemaNode, "temp_fileHandler_pkg");
							modified = true;
						}
					}
				}
				else if( attachment.type.equals("item") )
				{
					String itemIdString = attachment.value1;
					final ItemId itemId;

					if( Check.isEmpty(itemIdString) )
					{
						String file = attachment.url;
						itemId = new ItemId(xml.nodeValue("xml" + file + "/@uuid"),
							Integer.valueOf(xml.nodeValue("xml" + file + "/@version")));
					}
					else
					{
						itemId = new ItemId(itemIdString);
					}

					itemAttachments.put(itemId, attachment.uuid);

					// convert to resource attachment:
					attachment.type = "custom";
					attachment.value1 = "resource";
					attachment.url = "";
					attachment.setData("uuid", itemId.getUuid());
					attachment.setData("version", itemId.getVersion());
					attachment.setData("type", "p");

					// old migration added item attachments incorrectly under
					// temp_resourceHandler. Remove these nodes.
					Node oldPath = xml.node("xml/item/temp_resourceHandler[.='" + attachment.uuid + "']");
					if( oldPath != null )
					{
						oldPath.getParentNode().removeChild(oldPath);
						modified = true;
					}

					continue;
				}
			}

			if( !itemAttachments.isEmpty() )
			{
				for( Entry<ItemId, String> entry : itemAttachments.entrySet() )
				{
					final String uuid = entry.getValue();
					final ItemId itemId = entry.getKey();

					// There must be an item finder control. We need to look for
					// ALL nodes with
					// [@uuid=UUID and @version=version] and add the attachment
					// UUID underneath them
					for( Node node : xml
						.nodeList("//*[@uuid='" + itemId.getUuid() + "' and @version='" + itemId.getVersion() + "']") )
					{
						// Don't modify attachment nodes! (actually, these
						// probably don't exist in ItemXml anyway)
						if( !node.getNodeName().equals("attachment") )
						{
							node.setTextContent(uuid);
							modified = true;
						}
					}
				}
			}

			if( modified )
			{
				itemXml.setXml(xml.toString());
				session.save(itemXml);
			}
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from Item i inner join i.attachments as a where a.type in ('ims','item')");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItem.class, FakeAttachment.class, ItemXml.class};
	}
}
